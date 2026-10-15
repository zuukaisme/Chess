package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.ChessColor
import com.example.chess.model.TimeControl
import com.example.chess.multiplayer.LanConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanMultiplayerDialog(
    viewModel: ChessViewModel,
    onDismiss: () -> Unit
) {
    val lanService = viewModel.lanMultiplayerService
    val connectionState by lanService.connectionState.collectAsState()
    val discoveredHosts by lanService.discoveredHosts.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Host, 1: Join
    var playerName by remember { mutableStateOf("Player") }
    var selectedColor by remember { mutableStateOf(ChessColor.WHITE) }
    var selectedTimeControl by remember { mutableStateOf(TimeControl.BLITZ_5) }

    var manualIp by remember { mutableStateOf("") }
    var manualPort by remember { mutableStateOf("8888") }

    val myLocalIp = remember { lanService.getLocalIpAddress() }

    // Start auto-discovery when on Join tab
    LaunchedEffect(activeTab) {
        if (activeTab == 1 && connectionState is LanConnectionState.IDLE) {
            lanService.startDiscovery()
        }
    }

    // Auto-dismiss dialog once match connected
    LaunchedEffect(connectionState) {
        if (connectionState is LanConnectionState.CONNECTED) {
            onDismiss()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (connectionState !is LanConnectionState.CONNECTED) {
                lanService.stopDiscovery()
            }
        }
    }

    BasicAlertDialog(onDismissRequest = {
        if (connectionState !is LanConnectionState.CONNECTED) {
            lanService.stopAll()
        }
        onDismiss()
    }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp))
                .background(Color(0xFF090D15))
                .padding(16.dp)
                .testTag("lan_multiplayer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LOCAL NETWORK (LAN)",
                                color = Color(0xFFF1F5F9),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "My Wi-Fi IP: $myLocalIp",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            lanService.stopAll()
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp).testTag("close_lan_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Tabs: Host vs Join
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .background(Color(0xFF0D121B))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (activeTab == 0) Color(0xFF0284C7) else Color.Transparent)
                            .clickable {
                                activeTab = 0
                                lanService.stopDiscovery()
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_host_lan"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HOST MATCH",
                            color = if (activeTab == 0) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (activeTab == 1) Color(0xFF0284C7) else Color.Transparent)
                            .clickable {
                                activeTab = 1
                                lanService.startDiscovery()
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_join_lan"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DISCOVER & JOIN",
                            color = if (activeTab == 1) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Player Handle Field
                Text(
                    text = "YOUR HANDLE",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1522))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = playerName,
                        onValueChange = { playerName = it.take(16) },
                        textStyle = TextStyle(
                            color = Color(0xFFF1F5F9),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("lan_player_name_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeTab == 0) {
                    // HOST MATCH CONFIGURATION
                    Text(
                        text = "PLAY AS",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(ChessColor.WHITE to "White", ChessColor.BLACK to "Black").forEach { (col, label) ->
                            val isSel = selectedColor == col
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSel) Color(0xFF0284C7) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0F1522))
                                    .clickable { selectedColor = col }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "TIME CONTROL",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TimeControl.entries.forEach { tc ->
                            val isSel = selectedTimeControl == tc
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSel) Color(0xFF0284C7) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0F1522))
                                    .clickable { selectedTimeControl = tc }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tc.label.substringBefore(" "),
                                    color = if (isSel) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (val state = connectionState) {
                        is LanConnectionState.HOSTING -> {
                            // Pulsing radar state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                    .background(Color(0xFF064E3B).copy(alpha = 0.3f))
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF10B981),
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "BROADCASTING ON LOCAL WI-FI",
                                            color = Color(0xFF34D399),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Listening at ${state.hostIp}:${state.port}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Tell your friend on this Wi-Fi to tap Discover or enter this IP",
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                            .clickable { lanService.stopAll() }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "CANCEL HOST",
                                            color = Color(0xFFF87171),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            // Start Hosting Action
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0284C7))
                                    .clickable {
                                        viewModel.hostLanMatch(playerName, selectedColor, selectedTimeControl)
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("start_host_lan_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Router,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "START LAN SERVER",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // JOIN MATCH TAB
                    Text(
                        text = "DISCOVERED GAMES ON THIS WI-FI",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (discoveredHosts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .background(Color(0xFF0A0F19))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scanning subnet for LAN hosts...",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            discoveredHosts.forEach { host ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF2C394F), RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0F1522))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = host.hostName,
                                                color = Color(0xFFF1F5F9),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${host.ip}:${host.port} • ${host.timeControl.label}",
                                                color = Color(0xFF64748B),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF0284C7))
                                                .clickable {
                                                    viewModel.connectToLanHost(host.ip, host.port, playerName)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                .testTag("connect_discovered_lan_host"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "CONNECT",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Direct IP manual fallback
                    Text(
                        text = "MANUAL IP CONNECT",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F1522))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            BasicTextField(
                                value = manualIp,
                                onValueChange = { manualIp = it },
                                textStyle = TextStyle(
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (manualIp.isEmpty()) {
                                        Text("Host IP (e.g. 192.168.1.50)", color = Color(0xFF475569), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    inner()
                                },
                                modifier = Modifier.fillMaxWidth().testTag("lan_manual_ip_input")
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (manualIp.isNotEmpty()) Color(0xFF0284C7) else Color(0xFF1E293B))
                                .clickable(enabled = manualIp.isNotEmpty()) {
                                    val port = manualPort.toIntOrNull() ?: 8888
                                    viewModel.connectToLanHost(manualIp.trim(), port, playerName)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("lan_manual_connect_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CONNECT",
                                color = if (manualIp.isNotEmpty()) Color.White else Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Error / Disconnect feedback
                if (connectionState is LanConnectionState.DISCONNECTED) {
                    val reason = (connectionState as LanConnectionState.DISCONNECTED).reason
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = reason,
                        color = Color(0xFFF87171),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
