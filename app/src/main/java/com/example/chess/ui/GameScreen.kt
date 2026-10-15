package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.AiDifficulty
import com.example.chess.model.ChessColor
import com.example.chess.model.GameMode
import com.example.chess.model.GameStatus
import com.example.chess.model.PieceStyle
import com.example.chess.model.TimeControl

@Composable
fun GameScreen(
    viewModel: ChessViewModel,
    onOpenMultiplayer: () -> Unit,
    onOpenLan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNewGameModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
            .padding(horizontal = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Match Info Bar with Custom Status Plate
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E2837), RoundedCornerShape(8.dp))
                .background(Color(0xFF0F1522))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (uiState.gameStatus == GameStatus.ONGOING) Color(0xFF10B981) else Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (uiState.gameMode) {
                            GameMode.PV_AI -> "vs STOCKFISH [${uiState.aiDifficulty.label.uppercase()}]"
                            GameMode.PVP -> "PASS & PLAY"
                            GameMode.AI_VS_AI -> "AI vs AI MATCH"
                            GameMode.ONLINE -> "ONLINE MULTIPLAYER"
                            GameMode.LAN -> "LAN P2P [${uiState.lanOpponentName ?: "PEER"}]"
                        },
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = when (uiState.gameStatus) {
                        GameStatus.ONGOING -> {
                            if (uiState.isAiThinking) "Engine searching best line..."
                            else "${if (uiState.board.turn == ChessColor.WHITE) "White" else "Black"} to move"
                        }
                        GameStatus.CHECKMATE -> "CHECKMATE - ${if (uiState.board.turn == ChessColor.WHITE) "Black" else "White"} wins"
                        GameStatus.STALEMATE -> "Stalemate - Draw"
                        GameStatus.DRAW_REPETITION -> "Draw by repetition"
                        GameStatus.DRAW_50_MOVES -> "Draw by 50-move rule"
                        GameStatus.DRAW_INSUFFICIENT_MATERIAL -> "Draw by insufficient material"
                        GameStatus.RESIGNED -> "Resignation"
                        GameStatus.TIME_EXPIRED -> "Time forfeit"
                    },
                    color = if (uiState.board.isInCheck(uiState.board.turn)) Color(0xFFF87171) else Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.gameStatus != GameStatus.ONGOING) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                            .background(Color(0xFF7F1D1D).copy(alpha = 0.35f))
                            .clickable { viewModel.reopenGameOver() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("reopen_recap_button")
                    ) {
                        Text(
                            text = "RECAP",
                            color = Color(0xFFF87171),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(
                    onClick = { viewModel.flipBoard() },
                    modifier = Modifier.size(32.dp).testTag("flip_board_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cached,
                        contentDescription = "Flip Board",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(6.dp))
                        .background(Color(0xFF0369A1).copy(alpha = 0.25f))
                        .clickable { showNewGameModal = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("new_game_button")
                ) {
                    Text(
                        text = "NEW GAME",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Top Player Plate (Opponent / Black if not flipped)
        val topPlayerColor = if (uiState.isFlipped) ChessColor.WHITE else ChessColor.BLACK
        val bottomPlayerColor = if (uiState.isFlipped) ChessColor.BLACK else ChessColor.WHITE
        val topPlayerName = when (uiState.gameMode) {
            GameMode.PV_AI -> if (uiState.playerColor == topPlayerColor) "Player" else "Stockfish (${uiState.aiDifficulty.label})"
            GameMode.AI_VS_AI -> if (topPlayerColor == ChessColor.WHITE) "Engine White" else "Engine Black"
            GameMode.PVP -> if (topPlayerColor == ChessColor.WHITE) "White Player" else "Black Player"
            GameMode.ONLINE -> if (topPlayerColor == ChessColor.WHITE) "White Player" else "Black Player"
            GameMode.LAN -> if (uiState.playerColor == topPlayerColor) "You" else (uiState.lanOpponentName ?: "LAN Peer")
        }
        val topPlayerRating = when (uiState.gameMode) {
            GameMode.PV_AI -> if (uiState.playerColor == topPlayerColor) "1500" else uiState.aiDifficulty.elo.toString()
            GameMode.AI_VS_AI -> uiState.aiDifficulty.elo.toString()
            else -> "1500"
        }
        val topTime = if (topPlayerColor == ChessColor.WHITE) uiState.whiteTimeSeconds else uiState.blackTimeSeconds

        val whiteMat = remember(uiState.board) { uiState.board.calculateMaterialScore(ChessColor.WHITE) }
        val blackMat = remember(uiState.board) { uiState.board.calculateMaterialScore(ChessColor.BLACK) }
        val topMatDiff = if (topPlayerColor == ChessColor.WHITE) (whiteMat - blackMat).coerceAtLeast(0) else (blackMat - whiteMat).coerceAtLeast(0)
        val bottomMatDiff = if (bottomPlayerColor == ChessColor.WHITE) (whiteMat - blackMat).coerceAtLeast(0) else (blackMat - whiteMat).coerceAtLeast(0)

        PlayerBadgeWithClock(
            name = topPlayerName,
            rating = topPlayerRating,
            color = topPlayerColor,
            remainingSeconds = topTime,
            isActiveTurn = uiState.board.turn == topPlayerColor && uiState.gameStatus == GameStatus.ONGOING,
            capturedMaterialDiff = topMatDiff,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Chessboard & Vertical Engine Evaluation Indicator (height-matched)
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

        // In-board Checkmate Banner
        if (uiState.gameStatus == GameStatus.CHECKMATE) {
            val winnerColor = uiState.board.turn.opposite()
            val winnerLabel = if (winnerColor == ChessColor.WHITE) "WHITE" else "BLACK"
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))))
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("in_game_checkmate_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚔", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                        Column {
                            Text(
                                text = "CHECKMATE • $winnerLabel WINS",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Decisive strike: ${uiState.lastMove?.san ?: "Terminal mate"}",
                                color = Color(0xFFFECACA),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .clickable { viewModel.resetGame() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("checkmate_quick_rematch")
                        ) {
                            Text(
                                text = "REMATCH",
                                color = Color(0xFF991B1B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { viewModel.reopenGameOver() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("checkmate_reopen_summary")
                        ) {
                            Text(
                                text = "RECAP",
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

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Player Plate (User / White if not flipped)
        val bottomPlayerName = when (uiState.gameMode) {
            GameMode.PV_AI -> if (uiState.playerColor == bottomPlayerColor) "Player" else "Stockfish (${uiState.aiDifficulty.label})"
            GameMode.AI_VS_AI -> if (bottomPlayerColor == ChessColor.WHITE) "Engine White" else "Engine Black"
            GameMode.PVP -> if (bottomPlayerColor == ChessColor.WHITE) "White Player" else "Black Player"
            GameMode.ONLINE -> if (bottomPlayerColor == ChessColor.WHITE) "White Player" else "Black Player"
            GameMode.LAN -> if (uiState.playerColor == bottomPlayerColor) "You" else (uiState.lanOpponentName ?: "LAN Peer")
        }
        val bottomPlayerRating = when (uiState.gameMode) {
            GameMode.PV_AI -> if (uiState.playerColor == bottomPlayerColor) "1500" else uiState.aiDifficulty.elo.toString()
            GameMode.AI_VS_AI -> uiState.aiDifficulty.elo.toString()
            else -> "1500"
        }
        val bottomTime = if (bottomPlayerColor == ChessColor.WHITE) uiState.whiteTimeSeconds else uiState.blackTimeSeconds

        PlayerBadgeWithClock(
            name = bottomPlayerName,
            rating = bottomPlayerRating,
            color = bottomPlayerColor,
            remainingSeconds = bottomTime,
            isActiveTurn = uiState.board.turn == bottomPlayerColor && uiState.gameStatus == GameStatus.ONGOING,
            capturedMaterialDiff = bottomMatDiff,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable Move History Component (Algebraic Notation)
        MoveHistoryComponent(
            moves = uiState.moveHistory,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // AI vs AI Controls
        if (uiState.gameMode == GameMode.AI_VS_AI) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF1E2837), RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1522))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENGINE SPEED",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${uiState.aiVsAiSpeedMs / 1000.0}s / move",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = uiState.aiVsAiSpeedMs.toFloat(),
                        onValueChange = { viewModel.setAiVsAiSpeed(it.toLong()) },
                        valueRange = 300f..3000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF0284C7)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleAiVsAiPlay() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isAiVsAiPlaying) Color(0xFF991B1B) else Color(0xFF15803D)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).testTag("ai_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isAiVsAiPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (uiState.isAiVsAiPlaying) "PAUSE MATCH" else "PLAY MATCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.stepAiVsAi() },
                            enabled = !uiState.isAiVsAiPlaying && uiState.gameStatus == GameStatus.ONGOING,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).testTag("ai_step_move_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("STEP MOVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Gameplay Tactical Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(6.dp))
                    .background(Color(0xFF101726))
                    .clickable(enabled = uiState.moveHistory.isNotEmpty() && !uiState.isAiThinking) { viewModel.undoMove() }
                    .padding(vertical = 8.dp)
                    .testTag("undo_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = null,
                        tint = if (uiState.moveHistory.isNotEmpty()) Color(0xFF94A3B8) else Color(0xFF475569),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "UNDO",
                        color = if (uiState.moveHistory.isNotEmpty()) Color(0xFFCBD5E1) else Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(6.dp))
                    .background(Color(0xFF101726))
                    .clickable(enabled = uiState.gameStatus == GameStatus.ONGOING && !uiState.isAiThinking) { viewModel.requestHint() }
                    .padding(vertical = 8.dp)
                    .testTag("hint_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "HINT",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(6.dp))
                    .background(Color(0xFF101726))
                    .clickable(enabled = uiState.gameStatus == GameStatus.ONGOING) { viewModel.resign(uiState.playerColor) }
                    .padding(vertical = 8.dp)
                    .testTag("resign_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "RESIGN",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Pawn Promotion Dialog
    if (uiState.showPromotionDialog) {
        val promoColor = uiState.board.turn
        PromotionDialog(
            color = promoColor,
            onSelectPiece = { viewModel.executePromotion(it) },
            onDismiss = { viewModel.dismissPromotionDialog() }
        )
    }

    // Match End / Checkmate Modal Dialog
    if (uiState.gameStatus != GameStatus.ONGOING && !uiState.isGameOverDismissed) {
        val whiteMat = remember(uiState.board) { uiState.board.calculateMaterialScore(ChessColor.WHITE) }
        val blackMat = remember(uiState.board) { uiState.board.calculateMaterialScore(ChessColor.BLACK) }
        GameOverDialog(
            gameStatus = uiState.gameStatus,
            turn = uiState.board.turn,
            lastMove = uiState.lastMove,
            totalMoves = uiState.moveHistory.size,
            whiteMaterial = whiteMat,
            blackMaterial = blackMat,
            onRematch = { viewModel.resetGame() },
            onAnalyze = { viewModel.dismissGameOver() },
            onDismiss = { viewModel.dismissGameOver() }
        )
    }

    // New Game Setup Dialog
    if (showNewGameModal) {
        NewGameDialog(
            currentMode = uiState.gameMode,
            currentDifficulty = uiState.aiDifficulty,
            currentColor = uiState.playerColor,
            currentTimeControl = uiState.timeControl,
            onStartGame = { mode, diff, color, tc ->
                showNewGameModal = false
                if (mode == GameMode.ONLINE) {
                    onOpenMultiplayer()
                } else if (mode == GameMode.LAN) {
                    onOpenLan()
                } else {
                    viewModel.resetGame(mode, diff, color, tc)
                }
            },
            onDismiss = { showNewGameModal = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameDialog(
    currentMode: GameMode,
    currentDifficulty: AiDifficulty,
    currentColor: ChessColor,
    currentTimeControl: TimeControl,
    onStartGame: (GameMode, AiDifficulty, ChessColor, TimeControl) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    var selectedDiff by remember { mutableStateOf(currentDifficulty) }
    var selectedColor by remember { mutableStateOf(currentColor) }
    var selectedTc by remember { mutableStateOf(currentTimeControl) }

    androidx.compose.material3.BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF2C394F), RoundedCornerShape(12.dp))
                .background(Color(0xFF0F1522))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "MATCH CONFIGURATION",
                    color = Color(0xFFF1F5F9),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    "GAME MODE",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                // Row 1: Single & Local
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        GameMode.PV_AI to "vs Engine",
                        GameMode.PVP to "Pass & Play",
                        GameMode.AI_VS_AI to "AI vs AI"
                    ).forEach { (mode, label) ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSelected) Color(0xFF0284C7) else Color(0xFF1E2837), RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0A0E17))
                                .clickable { selectedMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
                // Row 2: Multiplayer modes
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        GameMode.LAN to "Local Network (LAN)",
                        GameMode.ONLINE to "Cloud Online"
                    ).forEach { (mode, label) ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSelected) Color(0xFF10B981) else Color(0xFF1E2837), RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFF0A0E17))
                                .clickable { selectedMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF34D399) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                if (selectedMode == GameMode.PV_AI || selectedMode == GameMode.AI_VS_AI) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "ENGINE LEVEL (STOCKFISH ELO)",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Column(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AiDifficulty.entries.forEach { diff ->
                            val isSelected = selectedDiff == diff
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSelected) Color(0xFF0284C7) else Color(0xFF1E2837), RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0A0E17))
                                    .clickable { selectedDiff = diff }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        diff.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFF1F5F9) else Color(0xFF94A3B8)
                                    )
                                    Text(
                                        "${diff.elo} ELO",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedMode == GameMode.PV_AI) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "PLAY AS",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ChessColor.WHITE to "White",
                            ChessColor.BLACK to "Black"
                        ).forEach { (col, label) ->
                            val isSelected = selectedColor == col
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSelected) Color(0xFF0284C7) else Color(0xFF1E2837), RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0A0E17))
                                    .clickable { selectedColor = col }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "TIME CONTROL",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeControl.entries.forEach { tc ->
                        val isSelected = selectedTc == tc
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSelected) Color(0xFF0284C7) else Color(0xFF1E2837), RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0A0E17))
                                .clickable { selectedTc = tc }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tc.label.substringBefore(" "),
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("CANCEL", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onStartGame(selectedMode, selectedDiff, selectedColor, selectedTc) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("confirm_start_game")
                    ) {
                        Text("START MATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
