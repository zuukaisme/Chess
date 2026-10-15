package com.example.chess.ui.theme

import androidx.compose.ui.graphics.Color

enum class BoardTheme(
    val title: String,
    val lightSquare: Color,
    val darkSquare: Color,
    val highlightSelected: Color,
    val highlightLastMove: Color,
    val highlightLegalMove: Color,
    val checkAlert: Color
) {
    EMERALD(
        title = "Emerald (Chess.com)",
        lightSquare = Color(0xFFEBECD0),
        darkSquare = Color(0xFF739552),
        highlightSelected = Color(0x99F6F669),
        highlightLastMove = Color(0x66BACA44),
        highlightLegalMove = Color(0x55000000),
        checkAlert = Color(0xCCEB4D4B)
    ),
    WOOD(
        title = "Walnut Wood",
        lightSquare = Color(0xFFE8D3B3),
        darkSquare = Color(0xFFA67A53),
        highlightSelected = Color(0x99F6D061),
        highlightLastMove = Color(0x66D4A373),
        highlightLegalMove = Color(0x552C1810),
        checkAlert = Color(0xCCE74C3C)
    ),
    MIDNIGHT(
        title = "Midnight Slate",
        lightSquare = Color(0xFF4A5568),
        darkSquare = Color(0xFF2D3748),
        highlightSelected = Color(0x9963B3ED),
        highlightLastMove = Color(0x664FD1C5),
        highlightLegalMove = Color(0x55E2E8F0),
        checkAlert = Color(0xCCE53E3E)
    ),
    CYBERPUNK(
        title = "Cyber Neon",
        lightSquare = Color(0xFF1E293B),
        darkSquare = Color(0xFF0F172A),
        highlightSelected = Color(0x9906B6D4),
        highlightLastMove = Color(0x668B5CF6),
        highlightLegalMove = Color(0x5538BDF8),
        checkAlert = Color(0xCCF43F5E)
    )
}
