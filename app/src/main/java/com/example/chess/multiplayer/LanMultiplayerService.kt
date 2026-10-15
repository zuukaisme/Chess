package com.example.chess.multiplayer

import android.content.Context
import android.net.wifi.WifiManager
import com.example.chess.model.ChessColor
import com.example.chess.model.Move
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import com.example.chess.model.TimeControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections

data class LanHostInfo(
    val ip: String,
    val port: Int,
    val hostName: String,
    val hostColor: ChessColor,
    val timeControl: TimeControl,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class LanConnectionState {
    object IDLE : LanConnectionState()
    data class HOSTING(val hostIp: String, val port: Int) : LanConnectionState()
    object SEARCHING : LanConnectionState()
    data class CONNECTING(val ip: String) : LanConnectionState()
    data class CONNECTED(
        val opponentName: String,
        val myColor: ChessColor,
        val timeControl: TimeControl
    ) : LanConnectionState()
    data class DISCONNECTED(val reason: String) : LanConnectionState()
}

class LanMultiplayerService(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<LanConnectionState>(LanConnectionState.IDLE)
    val connectionState: StateFlow<LanConnectionState> = _connectionState.asStateFlow()

    private val _discoveredHosts = MutableStateFlow<List<LanHostInfo>>(emptyList())
    val discoveredHosts: StateFlow<List<LanHostInfo>> = _discoveredHosts.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var socketWriter: PrintWriter? = null
    private var socketReader: BufferedReader? = null

    private var broadcastJob: Job? = null
    private var discoveryJob: Job? = null
    private var readJob: Job? = null

    var onMoveReceived: ((Move) -> Unit)? = null
    var onResignReceived: ((ChessColor) -> Unit)? = null
    var onRematchRequested: (() -> Unit)? = null

    companion object {
        const val DEFAULT_PORT = 8888
        const val DISCOVERY_PORT = 8889
        const val BEACON_PREFIX = "CHESS_LAN_BEACON"
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        val hostAddr = addr.hostAddress ?: continue
                        if (hostAddr.startsWith("192.168.") || hostAddr.startsWith("10.") || hostAddr.startsWith("172.")) {
                            return hostAddr
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }

    /**
     * Start hosting a LAN match.
     * Binds a ServerSocket and continuously broadcasts a UDP beacon so peers on the same Wi-Fi can find it.
     */
    fun startHosting(
        playerName: String,
        hostColor: ChessColor,
        timeControl: TimeControl,
        port: Int = DEFAULT_PORT
    ) {
        stopAll()
        scope.launch {
            try {
                val localIp = getLocalIpAddress()
                val server = ServerSocket()
                server.reuseAddress = true
                server.bind(InetSocketAddress(port))
                serverSocket = server

                _connectionState.value = LanConnectionState.HOSTING(localIp, port)

                // Start UDP beacon broadcaster
                startBeaconBroadcast(localIp, port, playerName, hostColor, timeControl)

                // Await incoming client connection
                val socket = server.accept()
                clientSocket = socket

                // Connection established! Stop beacon
                stopBeaconBroadcast()

                setupStreams(socket)

                // Handshake exchange
                // Host sends configuration: HANDSHAKE|<hostName>|<hostColor>|<timeControl>
                sendRaw("HANDSHAKE|$playerName|${hostColor.name}|${timeControl.name}")

                // Await client greeting: GREETING|<clientName>
                val clientGreeting = socketReader?.readLine() ?: ""
                val clientParts = clientGreeting.split("|")
                val opponentName = if (clientParts.size >= 2 && clientParts[0] == "GREETING") clientParts[1] else "Opponent"

                val myColor = hostColor
                _connectionState.value = LanConnectionState.CONNECTED(
                    opponentName = opponentName,
                    myColor = myColor,
                    timeControl = timeControl
                )

                startMessageLoop()
            } catch (e: Exception) {
                if (serverSocket != null && !serverSocket!!.isClosed) {
                    _connectionState.value = LanConnectionState.DISCONNECTED("Host error: ${e.message}")
                }
            }
        }
    }

    /**
     * Start discovery for games hosted on the same Wi-Fi network.
     */
    fun startDiscovery() {
        stopDiscovery()
        _discoveredHosts.value = emptyList()
        _connectionState.value = LanConnectionState.SEARCHING

        discoveryJob = scope.launch {
            var datagramSocket: DatagramSocket? = null
            try {
                datagramSocket = DatagramSocket(null)
                datagramSocket.reuseAddress = true
                datagramSocket.bind(InetSocketAddress(DISCOVERY_PORT))
                datagramSocket.soTimeout = 2000

                val buffer = ByteArray(1024)
                while (isActive) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        datagramSocket.receive(packet)
                        val text = String(packet.data, 0, packet.length).trim()
                        val parts = text.split("|")
                        if (parts.size >= 6 && parts[0] == BEACON_PREFIX) {
                            val hostIp = parts[1]
                            val hostPort = parts[2].toIntOrNull() ?: DEFAULT_PORT
                            val hostName = parts[3]
                            val hostColor = try { ChessColor.valueOf(parts[4]) } catch (_: Exception) { ChessColor.WHITE }
                            val timeControl = try { TimeControl.valueOf(parts[5]) } catch (_: Exception) { TimeControl.BLITZ_5 }

                            val myIp = getLocalIpAddress()
                            // Do not show our own host in discovery list
                            if (hostIp != myIp) {
                                val hostInfo = LanHostInfo(
                                    ip = hostIp,
                                    port = hostPort,
                                    hostName = hostName,
                                    hostColor = hostColor,
                                    timeControl = timeControl
                                )
                                val current = _discoveredHosts.value.filter { it.ip != hostIp && System.currentTimeMillis() - it.timestamp < 6000 }
                                _discoveredHosts.value = current + hostInfo
                            }
                        }
                    } catch (_: Exception) {}
                    delay(100)
                }
            } catch (_: Exception) {
            } finally {
                datagramSocket?.close()
            }
        }
    }

    fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
    }

    /**
     * Connect to a discovered or manually specified LAN host.
     */
    fun connectToHost(
        hostIp: String,
        port: Int = DEFAULT_PORT,
        playerName: String
    ) {
        stopAll()
        _connectionState.value = LanConnectionState.CONNECTING(hostIp)

        scope.launch {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostIp, port), 5000)
                clientSocket = socket

                setupStreams(socket)

                // Read handshake from host: HANDSHAKE|<hostName>|<hostColor>|<timeControl>
                val hostHandshake = socketReader?.readLine() ?: ""
                val parts = hostHandshake.split("|")
                if (parts.size < 4 || parts[0] != "HANDSHAKE") {
                    throw IllegalStateException("Invalid handshake from host")
                }

                val hostName = parts[1]
                val hostColor = try { ChessColor.valueOf(parts[2]) } catch (_: Exception) { ChessColor.WHITE }
                val timeControl = try { TimeControl.valueOf(parts[3]) } catch (_: Exception) { TimeControl.BLITZ_5 }

                // Client plays opposite color of host
                val myColor = hostColor.opposite()

                // Send greeting: GREETING|<playerName>
                sendRaw("GREETING|$playerName")

                _connectionState.value = LanConnectionState.CONNECTED(
                    opponentName = hostName,
                    myColor = myColor,
                    timeControl = timeControl
                )

                startMessageLoop()
            } catch (e: Exception) {
                _connectionState.value = LanConnectionState.DISCONNECTED("Connection failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    private fun setupStreams(socket: Socket) {
        socketWriter = PrintWriter(OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        socketReader = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
    }

    private fun startBeaconBroadcast(
        ip: String,
        port: Int,
        hostName: String,
        color: ChessColor,
        timeControl: TimeControl
    ) {
        stopBeaconBroadcast()
        broadcastJob = scope.launch {
            var datagramSocket: DatagramSocket? = null
            try {
                datagramSocket = DatagramSocket()
                datagramSocket.broadcast = true

                val message = "$BEACON_PREFIX|$ip|$port|$hostName|${color.name}|${timeControl.name}"
                val bytes = message.toByteArray()
                val targetAddr = InetAddress.getByName("255.255.255.255")

                while (isActive) {
                    try {
                        val packet = DatagramPacket(bytes, bytes.size, targetAddr, DISCOVERY_PORT)
                        datagramSocket.send(packet)
                    } catch (_: Exception) {}
                    delay(1500)
                }
            } catch (_: Exception) {
            } finally {
                datagramSocket?.close()
            }
        }
    }

    private fun stopBeaconBroadcast() {
        broadcastJob?.cancel()
        broadcastJob = null
    }

    private fun startMessageLoop() {
        readJob?.cancel()
        readJob = scope.launch {
            val reader = socketReader ?: return@launch
            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.isEmpty()) continue
                    parseIncomingMessage(line)
                }
            } catch (_: Exception) {
            } finally {
                if (_connectionState.value is LanConnectionState.CONNECTED) {
                    _connectionState.value = LanConnectionState.DISCONNECTED("Opponent disconnected")
                }
            }
        }
    }

    private fun parseIncomingMessage(msg: String) {
        val parts = msg.split("|")
        when (parts[0]) {
            "MOVE" -> {
                // MOVE|<fromR>|<fromC>|<toR>|<toC>|<promo>|<san>
                if (parts.size >= 5) {
                    val fromR = parts[1].toIntOrNull() ?: return
                    val fromC = parts[2].toIntOrNull() ?: return
                    val toR = parts[3].toIntOrNull() ?: return
                    val toC = parts[4].toIntOrNull() ?: return
                    val promo = if (parts.size >= 6 && parts[5].isNotEmpty()) {
                        try { PieceType.valueOf(parts[5]) } catch (_: Exception) { null }
                    } else null
                    val san = if (parts.size >= 7) parts[6] else ""

                    val move = Move(
                        from = Square(fromR, fromC),
                        to = Square(toR, toC),
                        piece = com.example.chess.model.ChessPiece(PieceType.PAWN, ChessColor.WHITE), // Placeholder, board resolves actual piece
                        promotionType = promo,
                        san = san
                    )
                    scope.launch(Dispatchers.Main) {
                        onMoveReceived?.invoke(move)
                    }
                }
            }
            "RESIGN" -> {
                val color = if (parts.size >= 2) {
                    try { ChessColor.valueOf(parts[1]) } catch (_: Exception) { ChessColor.WHITE }
                } else ChessColor.WHITE
                scope.launch(Dispatchers.Main) {
                    onResignReceived?.invoke(color)
                }
            }
            "REMATCH" -> {
                scope.launch(Dispatchers.Main) {
                    onRematchRequested?.invoke()
                }
            }
        }
    }

    fun sendMove(move: Move) {
        val promoStr = move.promotionType?.name ?: ""
        val payload = "MOVE|${move.from.row}|${move.from.col}|${move.to.row}|${move.to.col}|$promoStr|${move.san}"
        sendRaw(payload)
    }

    fun sendResign(color: ChessColor) {
        sendRaw("RESIGN|${color.name}")
    }

    fun sendRematch() {
        sendRaw("REMATCH")
    }

    private fun sendRaw(text: String) {
        scope.launch {
            try {
                socketWriter?.println(text)
            } catch (_: Exception) {}
        }
    }

    fun stopAll() {
        stopBeaconBroadcast()
        stopDiscovery()
        readJob?.cancel()
        readJob = null
        try {
            socketWriter?.close()
            socketReader?.close()
            clientSocket?.close()
            serverSocket?.close()
        } catch (_: Exception) {}
        clientSocket = null
        serverSocket = null
        socketWriter = null
        socketReader = null
        _connectionState.value = LanConnectionState.IDLE
    }
}
