package com.example.chess.puzzle

import com.example.chess.model.ChessColor

data class ChessPuzzle(
    val id: String,
    val title: String,
    val description: String,
    val initialFen: String,
    val playerColor: ChessColor,
    val solutionUci: List<String>, // Sequence of moves: player move, computer response, player move...
    val rating: Int,
    val theme: String
)

object PuzzleRepository {
    val puzzles: List<ChessPuzzle> = listOf(
        ChessPuzzle(
            id = "puz_1",
            title = "Back Rank Mate",
            description = "White to move. Exploit the trapped enemy king on the 8th rank.",
            initialFen = "6k1/5ppp/8/8/8/8/4R3/4R1K1 w - - 0 1",
            playerColor = ChessColor.WHITE,
            solutionUci = listOf("e2e8"),
            rating = 1050,
            theme = "Back Rank Mate"
        ),
        ChessPuzzle(
            id = "puz_2",
            title = "Smothered Mate",
            description = "White to move. Classical Philidor smothered mate tactic.",
            initialFen = "6k1/5ppp/8/8/8/5N2/5PPP/4Q1K1 w - - 0 1",
            playerColor = ChessColor.WHITE,
            solutionUci = listOf("e1e8"),
            rating = 1100,
            theme = "Mate in One"
        ),
        ChessPuzzle(
            id = "puz_3",
            title = "Royal Knight Fork",
            description = "White to move. Attack both King and Queen simultaneously.",
            initialFen = "r1b1k2r/pp3ppp/2n1p3/3q4/3N4/8/PPP2PPP/R1BQKB1R w KQkq - 0 1",
            playerColor = ChessColor.WHITE,
            solutionUci = listOf("d4b5"),
            rating = 1350,
            theme = "Knight Outpost & Fork"
        ),
        ChessPuzzle(
            id = "puz_4",
            title = "Devastating Skewer",
            description = "White to move. Win the Black Rook through the exposed King.",
            initialFen = "8/8/4k3/8/8/8/3R4/r3K3 w - - 0 1",
            playerColor = ChessColor.WHITE,
            solutionUci = listOf("e1e2"),
            rating = 1200,
            theme = "Endgame Defense"
        ),
        ChessPuzzle(
            id = "puz_5",
            title = "Anastasia's Mate Motif",
            description = "White to move. Deliver checkmate with Rook and Knight coordination.",
            initialFen = "5rk1/5ppp/8/4N3/8/8/5PPP/3R2K1 w - - 0 1",
            playerColor = ChessColor.WHITE,
            solutionUci = listOf("d1d8"),
            rating = 1250,
            theme = "Pin Exploitation"
        ),
        ChessPuzzle(
            id = "puz_6",
            title = "Queen Deflection",
            description = "White to move. Deflect the defender to secure victory.",
            initialFen = "r1b2rk1/pp3ppp/2n5/1B1p4/3N4/2Q5/PPP2PPP/R3R1K1 w - - 0 1",
            playerColor = ChessColor.WHITE,
            solutionUci = listOf("d4c6", "b7c6", "b5c6"),
            rating = 1450,
            theme = "Tactical Deflection"
        ),
        ChessPuzzle(
            id = "puz_7",
            title = "Grandmaster Pin",
            description = "White to move. Pin the piece to win material.",
            initialFen = "r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5N2/PPPP1PPP/RNBQK2R w KQkq - 0 1",
            playerColor = ChessColor.WHITE,
            solutionUci = listOf("d1e2"),
            rating = 1300,
            theme = "Absolute Pin"
        )
    )
}
