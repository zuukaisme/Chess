package com.example.chess.multiplayer

import android.content.Context
import com.example.R
import com.example.chess.model.Move
import com.example.chess.model.TimeControl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

class MultiplayerService(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (_: Exception) {
            null
        }
    }

    private var roomListener: ListenerRegistration? = null
    private val _currentRoom = MutableStateFlow<GameRoom?>(null)
    val currentRoom: StateFlow<GameRoom?> = _currentRoom.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val myPlayerId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: fallbackGuestId

    private val fallbackGuestId: String by lazy {
        UUID.randomUUID().toString().take(8)
    }

    fun createRoom(
        hostName: String,
        timeControl: TimeControl,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val roomId = (1..6).map { chars.random() }.joinToString("")
        val hostIsWhite = Random.nextBoolean()
        val currentUserId = myPlayerId

        val room = GameRoom(
            roomId = roomId,
            hostPlayerId = currentUserId,
            hostPlayerName = hostName.ifBlank { "Host" },
            whitePlayerId = if (hostIsWhite) currentUserId else "",
            blackPlayerId = if (!hostIsWhite) currentUserId else "",
            timeControlName = timeControl.name,
            whiteTimeSeconds = timeControl.seconds,
            blackTimeSeconds = timeControl.seconds,
            status = "WAITING"
        )

        val firestore = db
        if (firestore != null) {
            firestore.collection("chess_rooms").document(roomId)
                .set(room)
                .addOnSuccessListener {
                    listenToRoom(roomId)
                    onSuccess(roomId)
                }
                .addOnFailureListener { e ->
                    // Fallback to local state if offline
                    _currentRoom.value = room
                    onSuccess(roomId)
                }
        } else {
            _currentRoom.value = room
            onSuccess(roomId)
        }
    }

    fun joinRoom(
        roomId: String,
        guestName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanRoomId = roomId.trim().uppercase()
        val firestore = db
        val currentUserId = myPlayerId

        if (firestore != null) {
            firestore.collection("chess_rooms").document(cleanRoomId).get()
                .addOnSuccessListener { snapshot ->
                    val room = snapshot.toObject(GameRoom::class.java)
                    if (room == null) {
                        onError("Room not found")
                        return@addOnSuccessListener
                    }
                    if (room.status != "WAITING") {
                        onError("Room already in progress or finished")
                        return@addOnSuccessListener
                    }

                    val updatedRoom = room.copy(
                        guestPlayerId = currentUserId,
                        guestPlayerName = guestName.ifBlank { "Guest" },
                        whitePlayerId = if (room.whitePlayerId.isEmpty()) currentUserId else room.whitePlayerId,
                        blackPlayerId = if (room.blackPlayerId.isEmpty()) currentUserId else room.blackPlayerId,
                        status = "PLAYING"
                    )

                    firestore.collection("chess_rooms").document(cleanRoomId)
                        .set(updatedRoom)
                        .addOnSuccessListener {
                            listenToRoom(cleanRoomId)
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError(e.localizedMessage ?: "Failed to join")
                        }
                }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "Failed to connect to room")
                }
        } else {
            val localRoom = _currentRoom.value
            if (localRoom != null && localRoom.roomId == cleanRoomId) {
                _currentRoom.value = localRoom.copy(
                    guestPlayerId = currentUserId,
                    guestPlayerName = guestName.ifBlank { "Guest" },
                    whitePlayerId = if (localRoom.whitePlayerId.isEmpty()) currentUserId else localRoom.whitePlayerId,
                    blackPlayerId = if (localRoom.blackPlayerId.isEmpty()) currentUserId else localRoom.blackPlayerId,
                    status = "PLAYING"
                )
                onSuccess()
            } else {
                onError("Room not found or offline")
            }
        }
    }

    fun listenToRoom(roomId: String) {
        roomListener?.remove()
        val firestore = db ?: return
        roomListener = firestore.collection("chess_rooms").document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _statusMessage.value = "Sync error: ${error.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val room = snapshot.toObject(GameRoom::class.java)
                    _currentRoom.value = room
                }
            }
    }

    fun sendMove(
        roomId: String,
        newFen: String,
        move: Move,
        remainingWhiteSeconds: Long,
        remainingBlackSeconds: Long
    ) {
        val firestore = db
        val current = _currentRoom.value ?: return
        val updated = current.copy(
            currentFen = newFen,
            lastMoveUci = move.uci,
            lastMoveSan = move.san,
            whiteTimeSeconds = remainingWhiteSeconds,
            blackTimeSeconds = remainingBlackSeconds,
            lastMoveTimestamp = System.currentTimeMillis()
        )
        _currentRoom.value = updated

        if (firestore != null) {
            firestore.collection("chess_rooms").document(roomId)
                .set(updated)
        }
    }

    fun finishGame(roomId: String, winner: String) {
        val current = _currentRoom.value ?: return
        val updated = current.copy(status = "FINISHED", winner = winner)
        _currentRoom.value = updated
        db?.collection("chess_rooms")?.document(roomId)?.set(updated)
    }

    fun leaveRoom() {
        roomListener?.remove()
        roomListener = null
        _currentRoom.value = null
    }
}
