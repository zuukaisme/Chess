package com.example.chess.multiplayer

import com.example.chess.model.ChessColor
import com.example.chess.model.TimeControl

data class GameRoom(
    val roomId: String = "",
    val hostPlayerId: String = "",
    val hostPlayerName: String = "Host",
    val guestPlayerId: String = "",
    val guestPlayerName: String = "Guest",
    val whitePlayerId: String = "",
    val blackPlayerId: String = "",
    val currentFen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    val lastMoveUci: String = "",
    val lastMoveSan: String = "",
    val status: String = "WAITING", // WAITING, PLAYING, FINISHED
    val winner: String = "", // WHITE, BLACK, DRAW
    val timeControlName: String = TimeControl.BLITZ_5.name,
    val whiteTimeSeconds: Long = 300,
    val blackTimeSeconds: Long = 300,
    val lastMoveTimestamp: Long = System.currentTimeMillis()
)

data class RoomChatMessage(
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
