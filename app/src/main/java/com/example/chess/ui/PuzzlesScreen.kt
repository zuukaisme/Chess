package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.ChessColor
import com.example.chess.puzzle.PuzzleRepository

@Composable
fun PuzzlesScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allPuzzles = PuzzleRepository.puzzles

    LaunchedEffect(Unit) {
        if (uiState.currentPuzzle == null && allPuzzles.isNotEmpty()) {
            viewModel.loadPuzzle(allPuzzles.first())
        }
    }

    val currentPuzzle = uiState.currentPuzzle ?: allPuzzles.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Tactical Rating & Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                .background(Color(0xFF0F1522))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TACTICAL LAB",
                        color = Color(0xFFF8FAFC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${currentPuzzle.title.uppercase()} • ${currentPuzzle.theme.uppercase()}",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(4.dp))
                    .background(Color(0xFF0C1929))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "RATING: ${uiState.puzzleRating}",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Puzzle Carousel (Sleek chip selectors)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(allPuzzles) { puzzle ->
                val isSelected = puzzle.id == currentPuzzle.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E2838),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.3f) else Color(0xFF0B1019))
                        .clickable { viewModel.loadPuzzle(puzzle) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("puzzle_item_${puzzle.id}")
                ) {
                    Text(
                        text = "${puzzle.title} [${puzzle.rating}]",
                        color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chessboard with selected pieceStyle
        ChessBoardView(
            board = uiState.board,
            boardTheme = uiState.boardTheme,
            flipped = uiState.isFlipped,
            pieceStyle = uiState.pieceStyle,
            selectedSquare = uiState.selectedSquare,
            legalMoves = uiState.legalMoves,
            lastMove = uiState.lastMove,
            hintMove = uiState.hintMove,
            showLegalMoveHints = uiState.showLegalMoveHints,
            onSquareClick = { viewModel.onSquareClicked(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tactical Status & Feedback Banner
        val feedbackBg = when {
            uiState.puzzleSolved -> Color(0xFF064E3B).copy(alpha = 0.5f)
            uiState.puzzleFailed -> Color(0xFF7F1D1D).copy(alpha = 0.5f)
            else -> Color(0xFF0F1522)
        }
        val feedbackBorder = when {
            uiState.puzzleSolved -> Color(0xFF10B981)
            uiState.puzzleFailed -> Color(0xFFEF4444)
            else -> Color(0xFF1E2838)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, feedbackBorder, RoundedCornerShape(6.dp))
                .background(feedbackBg)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = when {
                        uiState.puzzleSolved -> Icons.Default.Check
                        uiState.puzzleFailed -> Icons.Default.Close
                        else -> Icons.Default.Lightbulb
                    },
                    contentDescription = null,
                    tint = when {
                        uiState.puzzleSolved -> Color(0xFF34D399)
                        uiState.puzzleFailed -> Color(0xFFFCA5A5)
                        else -> Color(0xFFF59E0B)
                    },
                    modifier = Modifier.size(20.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            uiState.puzzleSolved -> "PUZZLE SOLVED • +15 ELO GAINED"
                            uiState.puzzleFailed -> "INCORRECT MOVE • REVIEW & RETRY"
                            else -> "${if (currentPuzzle.playerColor == ChessColor.WHITE) "WHITE" else "BLACK"} TO MOVE — FIND BEST SEQUENCE"
                        },
                        color = when {
                            uiState.puzzleSolved -> Color(0xFF34D399)
                            uiState.puzzleFailed -> Color(0xFFFCA5A5)
                            else -> Color(0xFFF1F5F9)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = currentPuzzle.description,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action controls (Bespoke tactical buttons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Retry
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1522))
                    .clickable { viewModel.loadPuzzle(currentPuzzle) }
                    .padding(vertical = 10.dp)
                    .testTag("retry_puzzle_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RETRY", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            // Hint
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFD97706), RoundedCornerShape(4.dp))
                    .background(Color(0xFF451A03).copy(alpha = 0.3f))
                    .clickable { viewModel.requestHint() }
                    .padding(vertical = 10.dp)
                    .testTag("puzzle_hint_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("HINT", color = Color(0xFFFBBF24), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            // Next Puzzle
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(4.dp))
                    .background(Color(0xFF0284C7))
                    .clickable {
                        val currentIndex = allPuzzles.indexOfFirst { it.id == currentPuzzle.id }
                        val nextIndex = (currentIndex + 1) % allPuzzles.size
                        viewModel.loadPuzzle(allPuzzles[nextIndex])
                    }
                    .padding(vertical = 10.dp)
                    .testTag("next_puzzle_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NEXT PUZZLE", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
