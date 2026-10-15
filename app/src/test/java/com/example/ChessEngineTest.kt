package com.example

import com.example.chess.engine.ChessBoard
import com.example.chess.engine.ChessEngine
import com.example.chess.model.AiDifficulty
import com.example.chess.model.ChessColor
import com.example.chess.model.GameStatus
import com.example.chess.model.Move
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessEngineTest {

    @Test
    fun testInitialPositionHas20LegalMoves() {
        val board = ChessBoard()
        val legalMoves = board.generateLegalMoves(ChessColor.WHITE)
        assertEquals(20, legalMoves.size)
    }

    @Test
    fun testFoolsMate() {
        val board = ChessBoard()
        // 1. f3
        val move1 = board.generateLegalMoves().find { it.uci == "f2f3" }
        assertNotNull(move1)
        board.makeMove(move1!!)

        // 1... e5
        val move2 = board.generateLegalMoves().find { it.uci == "e7e5" }
        assertNotNull(move2)
        board.makeMove(move2!!)

        // 2. g4
        val move3 = board.generateLegalMoves().find { it.uci == "g2g4" }
        assertNotNull(move3)
        board.makeMove(move3!!)

        // 2... Qh4#
        val move4 = board.generateLegalMoves().find { it.uci == "d8h4" }
        assertNotNull(move4)
        board.makeMove(move4!!)

        // White is checkmated
        val status = board.checkGameStatus()
        assertEquals(GameStatus.CHECKMATE, status)
        assertTrue(board.isInCheck(ChessColor.WHITE))
    }

    @Test
    fun testEngineFindsMateInOne() {
        val board = ChessBoard()
        // Position where White can deliver mate in 1: 6k1/5ppp/8/8/8/8/4R3/4R1K1 w - - 0 1 (e2e8#)
        board.loadFen("6k1/5ppp/8/8/8/8/4R3/4R1K1 w - - 0 1")
        val engine = ChessEngine()
        val bestMove = engine.findBestMove(board, AiDifficulty.MASTER)
        assertNotNull(bestMove)
        assertEquals("e2e8", bestMove?.uci)
    }

    @Test
    fun testFenRoundtrip() {
        val board = ChessBoard()
        val initialFen = board.toFen()
        val restoredBoard = ChessBoard()
        val success = restoredBoard.loadFen(initialFen)
        assertTrue(success)
        assertEquals(board.turn, restoredBoard.turn)
        assertEquals(board.generateLegalMoves().size, restoredBoard.generateLegalMoves().size)
    }
}
