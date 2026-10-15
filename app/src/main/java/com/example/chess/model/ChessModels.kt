package com.example.chess.model

enum class ChessColor {
    WHITE,
    BLACK;

    fun opposite(): ChessColor = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(val symbol: String, val value: Int) {
    PAWN("P", 100),
    KNIGHT("N", 320),
    BISHOP("B", 330),
    ROOK("R", 500),
    QUEEN("Q", 900),
    KING("K", 20000);
}

data class ChessPiece(
    val type: PieceType,
    val color: ChessColor
)

data class Square(val row: Int, val col: Int) {
    init {
        require(row in 0..7 && col in 0..7) { "Square out of bounds: ($row, $col)" }
    }

    val isLight: Boolean get() = (row + col) % 2 == 0

    val notation: String get() {
        val file = ('a'.code + col).toChar()
        val rank = 8 - row
        return "$file$rank"
    }

    companion object {
        fun fromNotation(notation: String): Square? {
            if (notation.length != 2) return null
            val col = notation[0].lowercaseChar() - 'a'
            val rank = notation[1].digitToIntOrNull() ?: return null
            val row = 8 - rank
            if (row !in 0..7 || col !in 0..7) return null
            return Square(row, col)
        }
    }
}

data class Move(
    val from: Square,
    val to: Square,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null,
    val promotionType: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    var san: String = ""
) {
    val uci: String get() {
        val promo = promotionType?.symbol?.lowercase() ?: ""
        return "${from.notation}${to.notation}$promo"
    }
}
