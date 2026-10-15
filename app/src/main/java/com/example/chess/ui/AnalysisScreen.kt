package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.GameMode
import com.example.chess.model.GameStatus

@Composable
fun AnalysisScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var fenInput by remember { mutableStateOf("") }
    var fenError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C))
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Analysis & Sandbox",
                    color = Color(0xFFF1F5F9),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stockfish engine evaluation: ${if (uiState.evalCentipawns > 0) "+${uiState.evalCentipawns / 100.0}" else "${uiState.evalCentipawns / 100.0}"} pawns",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row {
                IconButton(onClick = { viewModel.flipBoard() }) {
                    Icon(imageVector = Icons.Default.Cached, contentDescription = "Flip", tint = Color(0xFF94A3B8))
                }
                IconButton(onClick = { viewModel.resetGame(mode = GameMode.PVP) }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Board & Eval Indicator (Height-matched)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val evalBarWidth = 26.dp
            val spacing = 8.dp
            val boardDim = maxWidth - evalBarWidth - spacing

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EvaluationProgressIndicator(
                    centipawns = uiState.evalCentipawns,
                    flipped = uiState.isFlipped,
                    modifier = Modifier
                        .width(evalBarWidth)
                        .height(boardDim)
                )

                ChessBoardView(
                    board = uiState.board,
                    boardTheme = uiState.boardTheme,
                    flipped = uiState.isFlipped,
                    pieceStyle = uiState.pieceStyle,
                    isCheckmate = uiState.gameStatus == GameStatus.CHECKMATE,
                    selectedSquare = uiState.selectedSquare,
                    legalMoves = uiState.legalMoves,
                    lastMove = uiState.lastMove,
                    hintMove = uiState.hintMove,
                    showLegalMoveHints = uiState.showLegalMoveHints,
                    onSquareClick = { viewModel.onSquareClicked(it) },
                    modifier = Modifier.size(boardDim)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Move History
        MoveHistoryComponent(moves = uiState.moveHistory)

        Spacer(modifier = Modifier.height(10.dp))

        // Engine Recommendation Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F2E)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Engine Top Move",
                        color = Color(0xFFF1F5F9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = uiState.hintMove?.let { "Recommended: ${it.piece.type.name} to ${it.to.notation} (${it.san})" }
                            ?: "Tap 'Engine Calc' below to see the optimal move arrow",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { viewModel.requestHint() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.testTag("analysis_hint_button")
                ) {
                    Text("Engine Calc", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FEN Loader
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F2E)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Load Custom FEN Position",
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fenInput,
                        onValueChange = {
                            fenInput = it
                            fenError = null
                        },
                        placeholder = { Text("Paste FEN string...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        isError = fenError != null,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (fenInput.isNotBlank()) {
                                val ok = uiState.board.loadFen(fenInput)
                                if (ok) {
                                    viewModel.resetGame(mode = GameMode.PVP)
                                    uiState.board.loadFen(fenInput)
                                    fenError = null
                                } else {
                                    fenError = "Invalid FEN"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.testTag("load_fen_button")
                    ) {
                        Text("Load", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }
                }
                if (fenError != null) {
                    Text(
                        text = fenError!!,
                        color = Color(0xFFF87171),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
