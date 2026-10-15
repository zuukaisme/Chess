package com.example.chess.engine

import com.example.chess.model.AiDifficulty
import com.example.chess.model.ChessColor
import com.example.chess.model.ChessPiece
import com.example.chess.model.Move
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ChessEngine {

    // Piece-Square Tables (from White's perspective, row 0 = rank 8, row 7 = rank 1)
    private val pawnTable = intArrayOf(
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    )

    private val knightTable = intArrayOf(
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    )

    private val bishopTable = intArrayOf(
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    )

    private val rookTable = intArrayOf(
          0,  0,  0,  0,  0,  0,  0,  0,
          5, 10, 10, 10, 10, 10, 10,  5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
          0,  0,  0,  5,  5,  0,  0,  0
    )

    private val queenTable = intArrayOf(
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    )

    private val kingMiddleTable = intArrayOf(
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    )

    // Opening book moves map: FEN prefix or move sequence -> list of moves in UCI notation
    private val openingBook = mapOf(
        "" to listOf("e2e4", "d2d4", "c2c4", "g1f3"),
        "e2e4" to listOf("e7e5", "c7c5", "e7e6", "c7c6"),
        "d2d4" to listOf("d7d5", "g8f6", "e7e6"),
        "e2e4 e7e5" to listOf("g1f3", "f1c4", "b1c3"),
        "e2e4 e7e5 g1f3" to listOf("b8c6", "g8f6"),
        "e2e4 e7e5 g1f3 b8c6" to listOf("f1b5", "f1c4", "d2d4"), // Ruy Lopez, Italian, Scotch
        "e2e4 c7c5" to listOf("g1f3", "b1c3", "c2c3"),
        "e2e4 c7c5 g1f3" to listOf("d7d6", "e7e6", "b8c6"),
        "d2d4 d7d5" to listOf("c2c4", "g1f3"),
        "d2d4 g8f6" to listOf("c2c4", "g1f3")
    )

    fun evaluatePosition(board: ChessBoard): Int {
        var whiteScore = 0
        var blackScore = 0

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board.getPiece(r, c) ?: continue
                val baseVal = piece.type.value
                val index = if (piece.color == ChessColor.WHITE) {
                    r * 8 + c
                } else {
                    (7 - r) * 8 + c
                }

                val pstVal = when (piece.type) {
                    PieceType.PAWN -> pawnTable[index]
                    PieceType.KNIGHT -> knightTable[index]
                    PieceType.BISHOP -> bishopTable[index]
                    PieceType.ROOK -> rookTable[index]
                    PieceType.QUEEN -> queenTable[index]
                    PieceType.KING -> kingMiddleTable[index]
                }

                val total = baseVal + pstVal
                if (piece.color == ChessColor.WHITE) {
                    whiteScore += total
                } else {
                    blackScore += total
                }
            }
        }

        return whiteScore - blackScore
    }

    fun findBestMove(
        board: ChessBoard,
        difficulty: AiDifficulty,
        moveSequence: String = ""
    ): Move? {
        val legalMoves = board.generateLegalMoves()
        if (legalMoves.isEmpty()) return null

        // Check opening book for first few moves
        if (moveSequence.isNotEmpty() && openingBook.containsKey(moveSequence.trim())) {
            val bookMoves = openingBook[moveSequence.trim()]!!
            for (bookUci in bookMoves.shuffled()) {
                val match = legalMoves.find { it.uci == bookUci }
                if (match != null) return match
            }
        }

        // Beginner blunders occasionally
        if (difficulty == AiDifficulty.BEGINNER && Random.nextFloat() < 0.35f) {
            // Pick a random move that at least doesn't immediately hang the queen if avoidable
            val nonQueenHangs = legalMoves.filter { it.piece.type != PieceType.QUEEN || it.capturedPiece != null }
            if (nonQueenHangs.isNotEmpty()) return nonQueenHangs.random()
            return legalMoves.random()
        }

        val isWhite = board.turn == ChessColor.WHITE
        var bestScore = if (isWhite) Int.MIN_VALUE else Int.MAX_VALUE
        var bestMove = legalMoves.first()

        val depth = difficulty.searchDepth

        // Sort moves with MVV-LVA
        val sortedMoves = orderMoves(legalMoves)

        for (move in sortedMoves) {
            val clone = board.copy()
            clone.makeMove(move)

            val score = minimax(
                clone,
                depth - 1,
                Int.MIN_VALUE,
                Int.MAX_VALUE,
                !isWhite,
                difficulty >= AiDifficulty.ADVANCED
            )

            if (isWhite) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
            }
        }

        return bestMove
    }

    private fun minimax(
        board: ChessBoard,
        depth: Int,
        alphaInit: Int,
        betaInit: Int,
        isMaximizing: Boolean,
        useQuiescence: Boolean
    ): Int {
        var alpha = alphaInit
        var beta = betaInit

        if (depth <= 0) {
            return if (useQuiescence) {
                quiescence(board, alpha, beta, isMaximizing, 3)
            } else {
                evaluatePosition(board)
            }
        }

        val legalMoves = board.generateLegalMoves()
        if (legalMoves.isEmpty()) {
            return if (board.isInCheck(board.turn)) {
                if (isMaximizing) -20000 - depth else 20000 + depth
            } else {
                0 // Stalemate
            }
        }

        val sortedMoves = orderMoves(legalMoves)

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in sortedMoves) {
                val clone = board.copy()
                clone.makeMove(move)
                val eval = minimax(clone, depth - 1, alpha, beta, false, useQuiescence)
                maxEval = max(maxEval, eval)
                alpha = max(alpha, eval)
                if (beta <= alpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in sortedMoves) {
                val clone = board.copy()
                clone.makeMove(move)
                val eval = minimax(clone, depth - 1, alpha, beta, true, useQuiescence)
                minEval = min(minEval, eval)
                beta = min(beta, eval)
                if (beta <= alpha) break
            }
            return minEval
        }
    }

    private fun quiescence(
        board: ChessBoard,
        alphaInit: Int,
        betaInit: Int,
        isMaximizing: Boolean,
        qDepth: Int
    ): Int {
        var alpha = alphaInit
        var beta = betaInit
        val standPat = evaluatePosition(board)

        if (qDepth <= 0) return standPat

        if (isMaximizing) {
            if (standPat >= beta) return beta
            if (standPat > alpha) alpha = standPat

            val captures = board.generateLegalMoves().filter { it.capturedPiece != null }
            val sortedCaptures = orderMoves(captures)

            for (move in sortedCaptures) {
                val clone = board.copy()
                clone.makeMove(move)
                val score = quiescence(clone, alpha, beta, false, qDepth - 1)
                if (score >= beta) return beta
                if (score > alpha) alpha = score
            }
            return alpha
        } else {
            if (standPat <= alpha) return alpha
            if (standPat < beta) beta = standPat

            val captures = board.generateLegalMoves().filter { it.capturedPiece != null }
            val sortedCaptures = orderMoves(captures)

            for (move in sortedCaptures) {
                val clone = board.copy()
                clone.makeMove(move)
                val score = quiescence(clone, alpha, beta, true, qDepth - 1)
                if (score <= alpha) return alpha
                if (score < beta) beta = score
            }
            return beta
        }
    }

    private fun orderMoves(moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            var score = 0
            if (move.capturedPiece != null) {
                // MVV-LVA: Most Valuable Victim - Least Valuable Attacker
                score += 10 * move.capturedPiece.type.value - move.piece.type.value
            }
            if (move.promotionType != null) {
                score += move.promotionType.value
            }
            if (move.isCastling) {
                score += 50
            }
            score
        }
    }
}
