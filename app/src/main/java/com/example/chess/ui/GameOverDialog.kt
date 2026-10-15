package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.chess.model.ChessColor
import com.example.chess.model.GameStatus
import com.example.chess.model.Move

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverDialog(
    gameStatus: GameStatus,
    turn: ChessColor,
    lastMove: Move?,
    totalMoves: Int,
    whiteMaterial: Int,
    blackMaterial: Int,
    onRematch: () -> Unit,
    onAnalyze: () -> Unit,
    onDismiss: () -> Unit
) {
    val isCheckmate = gameStatus == GameStatus.CHECKMATE
    val isDraw = gameStatus in listOf(
        GameStatus.STALEMATE,
        GameStatus.DRAW_REPETITION,
        GameStatus.DRAW_50_MOVES,
        GameStatus.DRAW_INSUFFICIENT_MATERIAL
    )

    val winnerColor = if (isDraw) null else turn.opposite()
    val winnerName = when (winnerColor) {
        ChessColor.WHITE -> "WHITE"
        ChessColor.BLACK -> "BLACK"
        null -> "NO CONTEST"
    }

    val title = when (gameStatus) {
        GameStatus.CHECKMATE -> "⚔ CHECKMATE"
        GameStatus.STALEMATE -> "STALEMATE"
        GameStatus.DRAW_REPETITION -> "DRAW (REPETITION)"
        GameStatus.DRAW_50_MOVES -> "DRAW (50 MOVES)"
        GameStatus.DRAW_INSUFFICIENT_MATERIAL -> "DRAW (MATERIAL)"
        GameStatus.RESIGNED -> "RESIGNATION"
        GameStatus.TIME_EXPIRED -> "TIME EXPIRED"
        else -> "MATCH FINISHED"
    }

    val subtitle = when {
        isDraw -> "Draw declared under official chess regulations"
        isCheckmate -> "$winnerName delivers terminal checkmate"
        winnerColor == ChessColor.WHITE -> "White secures the match victory"
        winnerColor == ChessColor.BLACK -> "Black secures the match victory"
        else -> "Match ended"
    }

    val bannerGlow = when {
        isCheckmate -> listOf(Color(0xFFDC2626), Color(0xFF7F1D1D), Color(0xFF450A0A))
        isDraw -> listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
        else -> listOf(Color(0xFF0284C7), Color(0xFF075985), Color(0xFF0F172A))
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, if (isCheckmate) Color(0xFFEF4444) else Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                .background(Color(0xFF080C14))
                .testTag("game_over_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top banner with rich crimson / cyan gradient glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(bannerGlow))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }
                            Text(
                                text = subtitle,
                                color = if (isCheckmate) Color(0xFFFECACA) else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp).testTag("close_game_over_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Checkmate Hero Card (if Checkmate)
                if (isCheckmate) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFB91C1C), RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E0A0A))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFFEF4444), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "♔#",
                                    color = Color(0xFFF87171),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FATAL STRIKE: ${lastMove?.san ?: "N/A"}",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "The ${turn.name.lowercase()} king has no escape or legal moves.",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Match Statistics Matrix
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(
                        text = "MATCH METRICS & SUMMARY",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Decisive move tile
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F1522))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "FINAL MOVE",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = lastMove?.san ?: "-",
                                    color = if (isCheckmate) Color(0xFFF87171) else Color(0xFF38BDF8),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Total turns tile
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F1522))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL MOVES",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalMoves",
                                    color = Color(0xFFF8FAFC),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Material Balance
                        val diff = whiteMaterial - blackMaterial
                        val diffText = when {
                            diff > 0 -> "+$diff W"
                            diff < 0 -> "+${-diff} B"
                            else -> "Equal"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F1522))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "MATERIAL",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = diffText,
                                    color = Color(0xFF10B981),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tactical Action Buttons (Rematch, Analyze, View Board)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rematch Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (isCheckmate) Color(0xFFDC2626) else Color(0xFF0284C7), RoundedCornerShape(6.dp))
                                .background(if (isCheckmate) Color(0xFFDC2626) else Color(0xFF0284C7))
                                .clickable { onRematch() }
                                .padding(vertical = 10.dp)
                                .testTag("game_over_rematch_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REMATCH",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Analysis Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                                .background(Color(0xFF131B2A))
                                .clickable { onAnalyze() }
                                .padding(vertical = 10.dp)
                                .testTag("game_over_analyze_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ANALYZE",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Inspect Board Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                            .background(Color(0xFF0A0F18))
                            .clickable { onDismiss() }
                            .padding(vertical = 8.dp)
                            .testTag("game_over_inspect_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VIEW FINAL BOARD POSITION",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
