package com.example.chess.engine

import com.example.chess.model.ChessColor
import com.example.chess.model.ChessPiece
import com.example.chess.model.GameStatus
import com.example.chess.model.Move
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlin.math.abs

class ChessBoard {
    private val squares: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } }

    var turn: ChessColor = ChessColor.WHITE
        private set

    var whiteCastleKingSide: Boolean = true
        private set
    var whiteCastleQueenSide: Boolean = true
        private set
    var blackCastleKingSide: Boolean = true
        private set
    var blackCastleQueenSide: Boolean = true
        private set

    var enPassantSquare: Square? = null
        private set

    var halfMoveClock: Int = 0
        private set
    var fullMoveNumber: Int = 1
        private set

    // Move history for undo and threefold repetition
    private val moveHistory = mutableListOf<MoveRecord>()
    private val positionHistory = mutableMapOf<String, Int>()

    private data class MoveRecord(
        val move: Move,
        val capturedPiece: ChessPiece?,
        val prevEnPassant: Square?,
        val prevWhiteCastleK: Boolean,
        val prevWhiteCastleQ: Boolean,
        val prevBlackCastleK: Boolean,
        val prevBlackCastleQ: Boolean,
        val prevHalfMoveClock: Int
    )

    init {
        setupStandardBoard()
    }

    fun setupStandardBoard() {
        for (r in 0..7) {
            for (c in 0..7) {
                squares[r][c] = null
            }
        }
        // Black pieces
        squares[0][0] = ChessPiece(PieceType.ROOK, ChessColor.BLACK)
        squares[0][1] = ChessPiece(PieceType.KNIGHT, ChessColor.BLACK)
        squares[0][2] = ChessPiece(PieceType.BISHOP, ChessColor.BLACK)
        squares[0][3] = ChessPiece(PieceType.QUEEN, ChessColor.BLACK)
        squares[0][4] = ChessPiece(PieceType.KING, ChessColor.BLACK)
        squares[0][5] = ChessPiece(PieceType.BISHOP, ChessColor.BLACK)
        squares[0][6] = ChessPiece(PieceType.KNIGHT, ChessColor.BLACK)
        squares[0][7] = ChessPiece(PieceType.ROOK, ChessColor.BLACK)
        for (c in 0..7) {
            squares[1][c] = ChessPiece(PieceType.PAWN, ChessColor.BLACK)
        }

        // White pieces
        for (c in 0..7) {
            squares[6][c] = ChessPiece(PieceType.PAWN, ChessColor.WHITE)
        }
        squares[7][0] = ChessPiece(PieceType.ROOK, ChessColor.WHITE)
        squares[7][1] = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE)
        squares[7][2] = ChessPiece(PieceType.BISHOP, ChessColor.WHITE)
        squares[7][3] = ChessPiece(PieceType.QUEEN, ChessColor.WHITE)
        squares[7][4] = ChessPiece(PieceType.KING, ChessColor.WHITE)
        squares[7][5] = ChessPiece(PieceType.BISHOP, ChessColor.WHITE)
        squares[7][6] = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE)
        squares[7][7] = ChessPiece(PieceType.ROOK, ChessColor.WHITE)

        turn = ChessColor.WHITE
        whiteCastleKingSide = true
        whiteCastleQueenSide = true
        blackCastleKingSide = true
        blackCastleQueenSide = true
        enPassantSquare = null
        halfMoveClock = 0
        fullMoveNumber = 1

        moveHistory.clear()
        positionHistory.clear()
        recordPosition()
    }

    fun getPiece(square: Square): ChessPiece? = squares[square.row][square.col]
    fun getPiece(row: Int, col: Int): ChessPiece? = squares[row][col]

    fun setPiece(square: Square, piece: ChessPiece?) {
        squares[square.row][square.col] = piece
    }

    fun copy(): ChessBoard {
        val copy = ChessBoard()
        for (r in 0..7) {
            for (c in 0..7) {
                copy.squares[r][c] = this.squares[r][c]
            }
        }
        copy.turn = this.turn
        copy.whiteCastleKingSide = this.whiteCastleKingSide
        copy.whiteCastleQueenSide = this.whiteCastleQueenSide
        copy.blackCastleKingSide = this.blackCastleKingSide
        copy.blackCastleQueenSide = this.blackCastleQueenSide
        copy.enPassantSquare = this.enPassantSquare
        copy.halfMoveClock = this.halfMoveClock
        copy.fullMoveNumber = this.fullMoveNumber
        copy.positionHistory.putAll(this.positionHistory)
        return copy
    }

    private fun positionKey(): String {
        val sb = StringBuilder()
        for (r in 0..7) {
            for (c in 0..7) {
                val p = squares[r][c]
                if (p == null) {
                    sb.append('.')
                } else {
                    val ch = when (p.type) {
                        PieceType.PAWN -> 'p'
                        PieceType.KNIGHT -> 'n'
                        PieceType.BISHOP -> 'b'
                        PieceType.ROOK -> 'r'
                        PieceType.QUEEN -> 'q'
                        PieceType.KING -> 'k'
                    }
                    sb.append(if (p.color == ChessColor.WHITE) ch.uppercaseChar() else ch)
                }
            }
        }
        sb.append(' ')
        sb.append(if (turn == ChessColor.WHITE) 'w' else 'b')
        sb.append(' ')
        if (whiteCastleKingSide) sb.append('K')
        if (whiteCastleQueenSide) sb.append('Q')
        if (blackCastleKingSide) sb.append('k')
        if (blackCastleQueenSide) sb.append('q')
        sb.append(' ')
        sb.append(enPassantSquare?.notation ?: "-")
        return sb.toString()
    }

    private fun recordPosition() {
        val key = positionKey()
        positionHistory[key] = (positionHistory[key] ?: 0) + 1
    }

    fun findKing(color: ChessColor): Square? {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = squares[r][c]
                if (p != null && p.color == color && p.type == PieceType.KING) {
                    return Square(r, c)
                }
            }
        }
        return null
    }

    fun isSquareAttacked(target: Square, byColor: ChessColor): Boolean {
        // Pawns
        val pawnRow = if (byColor == ChessColor.WHITE) target.row + 1 else target.row - 1
        if (pawnRow in 0..7) {
            if (target.col - 1 in 0..7) {
                val p = squares[pawnRow][target.col - 1]
                if (p?.color == byColor && p.type == PieceType.PAWN) return true
            }
            if (target.col + 1 in 0..7) {
                val p = squares[pawnRow][target.col + 1]
                if (p?.color == byColor && p.type == PieceType.PAWN) return true
            }
        }

        // Knights
        val knightOffsets = arrayOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for (offset in knightOffsets) {
            val r = target.row + offset.first
            val c = target.col + offset.second
            if (r in 0..7 && c in 0..7) {
                val p = squares[r][c]
                if (p?.color == byColor && p.type == PieceType.KNIGHT) return true
            }
        }

        // King
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = target.row + dr
                val c = target.col + dc
                if (r in 0..7 && c in 0..7) {
                    val p = squares[r][c]
                    if (p?.color == byColor && p.type == PieceType.KING) return true
                }
            }
        }

        // Orthogonal (Rooks & Queens)
        val orthoDirs = arrayOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        for (dir in orthoDirs) {
            var r = target.row + dir.first
            var c = target.col + dir.second
            while (r in 0..7 && c in 0..7) {
                val p = squares[r][c]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dir.first
                c += dir.second
            }
        }

        // Diagonal (Bishops & Queens)
        val diagDirs = arrayOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
        for (dir in diagDirs) {
            var r = target.row + dir.first
            var c = target.col + dir.second
            while (r in 0..7 && c in 0..7) {
                val p = squares[r][c]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dir.first
                c += dir.second
            }
        }

        return false
    }

    fun isInCheck(color: ChessColor): Boolean {
        val kingSquare = findKing(color) ?: return false
        return isSquareAttacked(kingSquare, color.opposite())
    }

    fun generateLegalMoves(color: ChessColor = turn): List<Move> {
        val pseudoMoves = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val p = squares[r][c]
                if (p != null && p.color == color) {
                    generatePseudoMovesForPiece(Square(r, c), p, pseudoMoves)
                }
            }
        }

        // Filter out moves that leave king in check
        val legalMoves = mutableListOf<Move>()
        for (move in pseudoMoves) {
            if (isMoveLegal(move)) {
                legalMoves.add(move)
            }
        }
        return legalMoves
    }

    fun hasAnyLegalMove(color: ChessColor = turn): Boolean {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = squares[r][c]
                if (p != null && p.color == color) {
                    val pseudo = mutableListOf<Move>()
                    generatePseudoMovesForPiece(Square(r, c), p, pseudo)
                    for (m in pseudo) {
                        if (isMoveLegal(m)) return true
                    }
                }
            }
        }
        return false
    }

    private fun isMoveLegal(move: Move): Boolean {
        makeMoveRaw(move)
        val inCheck = isInCheck(move.piece.color)
        undoMoveRaw()
        return !inCheck
    }

    private fun generatePseudoMovesForPiece(from: Square, piece: ChessPiece, moves: MutableList<Move>) {
        when (piece.type) {
            PieceType.PAWN -> generatePawnMoves(from, piece, moves)
            PieceType.KNIGHT -> generateKnightMoves(from, piece, moves)
            PieceType.BISHOP -> generateRayMoves(from, piece, arrayOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)), moves)
            PieceType.ROOK -> generateRayMoves(from, piece, arrayOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)), moves)
            PieceType.QUEEN -> {
                generateRayMoves(from, piece, arrayOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)), moves)
                generateRayMoves(from, piece, arrayOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)), moves)
            }
            PieceType.KING -> generateKingMoves(from, piece, moves)
        }
    }

    private fun generatePawnMoves(from: Square, piece: ChessPiece, moves: MutableList<Move>) {
        val dir = if (piece.color == ChessColor.WHITE) -1 else 1
        val startRow = if (piece.color == ChessColor.WHITE) 6 else 1
        val promoRow = if (piece.color == ChessColor.WHITE) 0 else 7

        // 1 step forward
        val forwardRow = from.row + dir
        if (forwardRow in 0..7 && squares[forwardRow][from.col] == null) {
            val to = Square(forwardRow, from.col)
            if (forwardRow == promoRow) {
                for (promo in arrayOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                    moves.add(Move(from, to, piece, promotionType = promo))
                }
            } else {
                moves.add(Move(from, to, piece))
            }

            // 2 steps forward
            val doubleRow = from.row + 2 * dir
            if (from.row == startRow && squares[doubleRow][from.col] == null) {
                moves.add(Move(from, Square(doubleRow, from.col), piece))
            }
        }

        // Captures
        for (dc in arrayOf(-1, 1)) {
            val capCol = from.col + dc
            if (capCol in 0..7 && forwardRow in 0..7) {
                val target = squares[forwardRow][capCol]
                val to = Square(forwardRow, capCol)
                if (target != null && target.color != piece.color) {
                    if (forwardRow == promoRow) {
                        for (promo in arrayOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                            moves.add(Move(from, to, piece, capturedPiece = target, promotionType = promo))
                        }
                    } else {
                        moves.add(Move(from, to, piece, capturedPiece = target))
                    }
                } else if (to == enPassantSquare) {
                    // En Passant capture
                    val epCaptured = squares[from.row][capCol]
                    moves.add(Move(from, to, piece, capturedPiece = epCaptured, isEnPassant = true))
                }
            }
        }
    }

    private fun generateKnightMoves(from: Square, piece: ChessPiece, moves: MutableList<Move>) {
        val offsets = arrayOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for (offset in offsets) {
            val r = from.row + offset.first
            val c = from.col + offset.second
            if (r in 0..7 && c in 0..7) {
                val target = squares[r][c]
                if (target == null || target.color != piece.color) {
                    moves.add(Move(from, Square(r, c), piece, capturedPiece = target))
                }
            }
        }
    }

    private fun generateRayMoves(from: Square, piece: ChessPiece, directions: Array<Pair<Int, Int>>, moves: MutableList<Move>) {
        for (dir in directions) {
            var r = from.row + dir.first
            var c = from.col + dir.second
            while (r in 0..7 && c in 0..7) {
                val target = squares[r][c]
                if (target == null) {
                    moves.add(Move(from, Square(r, c), piece))
                } else {
                    if (target.color != piece.color) {
                        moves.add(Move(from, Square(r, c), piece, capturedPiece = target))
                    }
                    break
                }
                r += dir.first
                c += dir.second
            }
        }
    }

    private fun generateKingMoves(from: Square, piece: ChessPiece, moves: MutableList<Move>) {
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = from.row + dr
                val c = from.col + dc
                if (r in 0..7 && c in 0..7) {
                    val target = squares[r][c]
                    if (target == null || target.color != piece.color) {
                        moves.add(Move(from, Square(r, c), piece, capturedPiece = target))
                    }
                }
            }
        }

        // Castling
        val enemyColor = piece.color.opposite()
        if (piece.color == ChessColor.WHITE && from.row == 7 && from.col == 4 && !isInCheck(ChessColor.WHITE)) {
            // Kingside (e1 -> g1)
            if (whiteCastleKingSide && squares[7][5] == null && squares[7][6] == null) {
                if (!isSquareAttacked(Square(7, 5), enemyColor) && !isSquareAttacked(Square(7, 6), enemyColor)) {
                    val rook = squares[7][7]
                    if (rook?.type == PieceType.ROOK && rook.color == ChessColor.WHITE) {
                        moves.add(Move(from, Square(7, 6), piece, isCastling = true))
                    }
                }
            }
            // Queenside (e1 -> c1)
            if (whiteCastleQueenSide && squares[7][1] == null && squares[7][2] == null && squares[7][3] == null) {
                if (!isSquareAttacked(Square(7, 3), enemyColor) && !isSquareAttacked(Square(7, 2), enemyColor)) {
                    val rook = squares[7][0]
                    if (rook?.type == PieceType.ROOK && rook.color == ChessColor.WHITE) {
                        moves.add(Move(from, Square(7, 2), piece, isCastling = true))
                    }
                }
            }
        } else if (piece.color == ChessColor.BLACK && from.row == 0 && from.col == 4 && !isInCheck(ChessColor.BLACK)) {
            // Kingside (e8 -> g8)
            if (blackCastleKingSide && squares[0][5] == null && squares[0][6] == null) {
                if (!isSquareAttacked(Square(0, 5), enemyColor) && !isSquareAttacked(Square(0, 6), enemyColor)) {
                    val rook = squares[0][7]
                    if (rook?.type == PieceType.ROOK && rook.color == ChessColor.BLACK) {
                        moves.add(Move(from, Square(0, 6), piece, isCastling = true))
                    }
                }
            }
            // Queenside (e8 -> c8)
            if (blackCastleQueenSide && squares[0][1] == null && squares[0][2] == null && squares[0][3] == null) {
                if (!isSquareAttacked(Square(0, 3), enemyColor) && !isSquareAttacked(Square(0, 2), enemyColor)) {
                    val rook = squares[0][0]
                    if (rook?.type == PieceType.ROOK && rook.color == ChessColor.BLACK) {
                        moves.add(Move(from, Square(0, 2), piece, isCastling = true))
                    }
                }
            }
        }
    }

    private fun makeMoveRaw(move: Move) {
        val record = MoveRecord(
            move = move,
            capturedPiece = move.capturedPiece,
            prevEnPassant = enPassantSquare,
            prevWhiteCastleK = whiteCastleKingSide,
            prevWhiteCastleQ = whiteCastleQueenSide,
            prevBlackCastleK = blackCastleKingSide,
            prevBlackCastleQ = blackCastleQueenSide,
            prevHalfMoveClock = halfMoveClock
        )
        moveHistory.add(record)

        squares[move.from.row][move.from.col] = null

        if (move.isEnPassant) {
            // Remove pawn behind target
            val capRow = if (move.piece.color == ChessColor.WHITE) move.to.row + 1 else move.to.row - 1
            squares[capRow][move.to.col] = null
            squares[move.to.row][move.to.col] = move.piece
        } else if (move.isCastling) {
            squares[move.to.row][move.to.col] = move.piece
            if (move.to.col == 6) { // Kingside
                val rook = squares[move.to.row][7]
                squares[move.to.row][7] = null
                squares[move.to.row][5] = rook
            } else if (move.to.col == 2) { // Queenside
                val rook = squares[move.to.row][0]
                squares[move.to.row][0] = null
                squares[move.to.row][3] = rook
            }
        } else {
            val finalPiece = if (move.promotionType != null) {
                ChessPiece(move.promotionType, move.piece.color)
            } else {
                move.piece
            }
            squares[move.to.row][move.to.col] = finalPiece
        }

        // En Passant update
        if (move.piece.type == PieceType.PAWN && abs(move.to.row - move.from.row) == 2) {
            val epRow = (move.from.row + move.to.row) / 2
            enPassantSquare = Square(epRow, move.from.col)
        } else {
            enPassantSquare = null
        }

        // Castling rights update
        if (move.piece.type == PieceType.KING) {
            if (move.piece.color == ChessColor.WHITE) {
                whiteCastleKingSide = false
                whiteCastleQueenSide = false
            } else {
                blackCastleKingSide = false
                blackCastleQueenSide = false
            }
        }
        if (move.piece.type == PieceType.ROOK) {
            if (move.from.row == 7 && move.from.col == 7) whiteCastleKingSide = false
            if (move.from.row == 7 && move.from.col == 0) whiteCastleQueenSide = false
            if (move.from.row == 0 && move.from.col == 7) blackCastleKingSide = false
            if (move.from.row == 0 && move.from.col == 0) blackCastleQueenSide = false
        }
        if (move.capturedPiece?.type == PieceType.ROOK) {
            if (move.to.row == 7 && move.to.col == 7) whiteCastleKingSide = false
            if (move.to.row == 7 && move.to.col == 0) whiteCastleQueenSide = false
            if (move.to.row == 0 && move.to.col == 7) blackCastleKingSide = false
            if (move.to.row == 0 && move.to.col == 0) blackCastleQueenSide = false
        }

        // Halfmove clock
        if (move.piece.type == PieceType.PAWN || move.capturedPiece != null) {
            halfMoveClock = 0
        } else {
            halfMoveClock++
        }

        if (turn == ChessColor.BLACK) fullMoveNumber++
        turn = turn.opposite()
    }

    private fun undoMoveRaw(): Move? {
        if (moveHistory.isEmpty()) return null
        val record = moveHistory.removeAt(moveHistory.size - 1)
        val move = record.move

        turn = move.piece.color
        if (turn == ChessColor.BLACK) fullMoveNumber--

        squares[move.from.row][move.from.col] = move.piece
        squares[move.to.row][move.to.col] = move.capturedPiece

        if (move.isEnPassant) {
            squares[move.to.row][move.to.col] = null
            val capRow = if (move.piece.color == ChessColor.WHITE) move.to.row + 1 else move.to.row - 1
            squares[capRow][move.to.col] = record.capturedPiece
        } else if (move.isCastling) {
            if (move.to.col == 6) { // Kingside
                val rook = squares[move.to.row][5]
                squares[move.to.row][5] = null
                squares[move.to.row][7] = rook
            } else if (move.to.col == 2) { // Queenside
                val rook = squares[move.to.row][3]
                squares[move.to.row][3] = null
                squares[move.to.row][0] = rook
            }
        }

        enPassantSquare = record.prevEnPassant
        whiteCastleKingSide = record.prevWhiteCastleK
        whiteCastleQueenSide = record.prevWhiteCastleQ
        blackCastleKingSide = record.prevBlackCastleK
        blackCastleQueenSide = record.prevBlackCastleQ
        halfMoveClock = record.prevHalfMoveClock

        return move
    }

    fun makeMove(move: Move): Boolean {
        val legalMoves = generateLegalMoves(turn)
        val matchedMove = legalMoves.find {
            it.from == move.from && it.to == move.to &&
                    (move.promotionType == null || it.promotionType == move.promotionType)
        } ?: return false

        matchedMove.san = formatSan(matchedMove)
        move.san = matchedMove.san
        makeMoveRaw(matchedMove)
        recordPosition()
        return true
    }

    fun undoMove(): Move? {
        val move = undoMoveRaw()
        if (move != null) {
            // Decrement position count
            val key = positionKey()
            val count = (positionHistory[key] ?: 1) - 1
            if (count <= 0) positionHistory.remove(key) else positionHistory[key] = count
        }
        return move
    }

    fun checkGameStatus(): GameStatus {
        val inCheck = isInCheck(turn)
        val hasLegal = hasAnyLegalMove(turn)

        if (!hasLegal) {
            return if (inCheck) GameStatus.CHECKMATE else GameStatus.STALEMATE
        }

        // Threefold repetition
        val curKey = positionKey()
        if ((positionHistory[curKey] ?: 0) >= 3) {
            return GameStatus.DRAW_REPETITION
        }

        // 50-move rule
        if (halfMoveClock >= 100) {
            return GameStatus.DRAW_50_MOVES
        }

        // Insufficient material
        if (isInsufficientMaterial()) {
            return GameStatus.DRAW_INSUFFICIENT_MATERIAL
        }

        return GameStatus.ONGOING
    }

    private fun isInsufficientMaterial(): Boolean {
        val pieces = mutableListOf<ChessPiece>()
        for (r in 0..7) {
            for (c in 0..7) {
                squares[r][c]?.let { pieces.add(it) }
            }
        }
        // K vs K
        if (pieces.size == 2) return true
        // K+B vs K or K+N vs K
        if (pieces.size == 3) {
            val nonKing = pieces.find { it.type != PieceType.KING }
            if (nonKing?.type == PieceType.BISHOP || nonKing?.type == PieceType.KNIGHT) return true
        }
        // K+B vs K+B (same color square bishops)
        if (pieces.size == 4) {
            val bishops = pieces.filter { it.type == PieceType.BISHOP }
            if (bishops.size == 2 && bishops[0].color != bishops[1].color) {
                // Find squares
                var b1Square: Square? = null
                var b2Square: Square? = null
                for (r in 0..7) {
                    for (c in 0..7) {
                        val p = squares[r][c]
                        if (p?.type == PieceType.BISHOP) {
                            if (b1Square == null) b1Square = Square(r, c)
                            else b2Square = Square(r, c)
                        }
                    }
                }
                if (b1Square != null && b2Square != null && b1Square.isLight == b2Square.isLight) {
                    return true
                }
            }
        }
        return false
    }

    fun formatSan(move: Move): String {
        if (move.isCastling) {
            return if (move.to.col == 6) "O-O" else "O-O-O"
        }

        val sb = StringBuilder()
        if (move.piece.type != PieceType.PAWN) {
            sb.append(move.piece.type.symbol)
        } else if (move.capturedPiece != null || move.isEnPassant) {
            sb.append(('a'.code + move.from.col).toChar())
        }

        if (move.capturedPiece != null || move.isEnPassant) {
            sb.append('x')
        }

        sb.append(move.to.notation)

        if (move.promotionType != null) {
            sb.append("=").append(move.promotionType.symbol)
        }

        // Test if this move delivers check or checkmate
        makeMoveRaw(move)
        val opponentInCheck = isInCheck(turn)
        if (opponentInCheck) {
            val opponentHasLegal = hasAnyLegalMove(turn)
            if (!opponentHasLegal) {
                sb.append('#')
            } else {
                sb.append('+')
            }
        }
        undoMoveRaw()

        return sb.toString()
    }

    fun toFen(): String {
        val sb = StringBuilder()
        for (r in 0..7) {
            var emptyCount = 0
            for (c in 0..7) {
                val p = squares[r][c]
                if (p == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    val ch = when (p.type) {
                        PieceType.PAWN -> 'p'
                        PieceType.KNIGHT -> 'n'
                        PieceType.BISHOP -> 'b'
                        PieceType.ROOK -> 'r'
                        PieceType.QUEEN -> 'q'
                        PieceType.KING -> 'k'
                    }
                    sb.append(if (p.color == ChessColor.WHITE) ch.uppercaseChar() else ch)
                }
            }
            if (emptyCount > 0) sb.append(emptyCount)
            if (r < 7) sb.append('/')
        }

        sb.append(' ')
        sb.append(if (turn == ChessColor.WHITE) 'w' else 'b')
        sb.append(' ')
        val castling = StringBuilder()
        if (whiteCastleKingSide) castling.append('K')
        if (whiteCastleQueenSide) castling.append('Q')
        if (blackCastleKingSide) castling.append('k')
        if (blackCastleQueenSide) castling.append('q')
        sb.append(if (castling.isEmpty()) "-" else castling.toString())
        sb.append(' ')
        sb.append(enPassantSquare?.notation ?: "-")
        sb.append(' ')
        sb.append(halfMoveClock)
        sb.append(' ')
        sb.append(fullMoveNumber)
        return sb.toString()
    }

    fun loadFen(fen: String): Boolean {
        try {
            val parts = fen.trim().split("\\s+".toRegex())
            if (parts.size < 4) return false

            val rows = parts[0].split('/')
            if (rows.size != 8) return false

            for (r in 0..7) {
                var c = 0
                for (ch in rows[r]) {
                    if (ch.isDigit()) {
                        val count = ch.digitToInt()
                        for (i in 0 until count) {
                            if (c in 0..7) squares[r][c] = null
                            c++
                        }
                    } else {
                        val color = if (ch.isUpperCase()) ChessColor.WHITE else ChessColor.BLACK
                        val type = when (ch.lowercaseChar()) {
                            'p' -> PieceType.PAWN
                            'n' -> PieceType.KNIGHT
                            'b' -> PieceType.BISHOP
                            'r' -> PieceType.ROOK
                            'q' -> PieceType.QUEEN
                            'k' -> PieceType.KING
                            else -> return false
                        }
                        if (c in 0..7) squares[r][c] = ChessPiece(type, color)
                        c++
                    }
                }
                if (c != 8) return false
            }

            turn = if (parts[1] == "w") ChessColor.WHITE else ChessColor.BLACK
            val castling = parts[2]
            whiteCastleKingSide = castling.contains('K')
            whiteCastleQueenSide = castling.contains('Q')
            blackCastleKingSide = castling.contains('k')
            blackCastleQueenSide = castling.contains('q')

            enPassantSquare = if (parts[3] != "-") Square.fromNotation(parts[3]) else null
            halfMoveClock = if (parts.size >= 5) parts[4].toIntOrNull() ?: 0 else 0
            fullMoveNumber = if (parts.size >= 6) parts[5].toIntOrNull() ?: 1 else 1

            moveHistory.clear()
            positionHistory.clear()
            recordPosition()
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun calculateMaterialScore(color: ChessColor): Int {
        var score = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = squares[r][c]
                if (piece?.color == color) {
                    score += when (piece.type) {
                        PieceType.PAWN -> 1
                        PieceType.KNIGHT -> 3
                        PieceType.BISHOP -> 3
                        PieceType.ROOK -> 5
                        PieceType.QUEEN -> 9
                        PieceType.KING -> 0
                    }
                }
            }
        }
        return score
    }
}
