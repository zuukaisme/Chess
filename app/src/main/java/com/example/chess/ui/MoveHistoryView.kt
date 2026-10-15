package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.Move

/**
 * Scrollable MoveHistory component that logs algebraic chess notation (SAN)
 * for each move played in the current game session.
 *
 * Supports both a compact horizontal scrolling strip for live play and
 * an expandable full match table with turn pairs (1. e4 e5, 2. Nf3 Nc6...).
 */
@Composable
fun MoveHistoryComponent(
    moves: List<Move>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rowListState = rememberLazyListState()
    val colListState = rememberLazyListState()

    val turnPairs = remember(moves) { moves.chunked(2) }

    // Auto-scroll to latest move played in session
    LaunchedEffect(moves.size) {
        if (turnPairs.isNotEmpty()) {
            rowListState.animateScrollToItem(turnPairs.size - 1)
            colListState.animateScrollToItem(turnPairs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF1E2837), RoundedCornerShape(8.dp))
            .background(Color(0xFF0F1522))
            .testTag("scrollable_move_history")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            // Header bar with status and expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = "Move Log",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MOVE HISTORY",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    if (moves.isNotEmpty()) {
                        Text(
                            text = " (${moves.size} moves)",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp).testTag("toggle_move_log_expand")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse History" else "Expand History",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (moves.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Game started. 1. White to move",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else if (!isExpanded) {
                // Horizontal compact scrollable ribbon with latest moves
                LazyRow(
                    state = rowListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(turnPairs.size) { index ->
                        val pair = turnPairs[index]
                        val moveNumber = index + 1
                        val whiteMove = pair[0]
                        val blackMove = pair.getOrNull(1)
                        val isLatestPair = index == turnPairs.size - 1

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isLatestPair) Color(0xFF16253B) else Color(0xFF131B29))
                                .border(
                                    0.5.dp,
                                    if (isLatestPair) Color(0xFF0284C7) else Color(0xFF222F43),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$moveNumber.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = whiteMove.san,
                                color = Color(0xFFF8FAFC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (blackMove != null) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = blackMove.san,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            } else {
                // Expanded two-column vertical scrollable log table
                LazyColumn(
                    state = colListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .padding(top = 6.dp)
                ) {
                    items(turnPairs.size) { index ->
                        val pair = turnPairs[index]
                        val moveNum = index + 1
                        val whiteMove = pair[0]
                        val blackMove = pair.getOrNull(1)
                        val isEven = index % 2 == 0
                        val isLatest = index == turnPairs.size - 1

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    when {
                                        isLatest -> Color(0xFF16253B)
                                        isEven -> Color(0xFF121824)
                                        else -> Color(0xFF0F141F)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$moveNum.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(36.dp)
                            )

                            Text(
                                text = whiteMove.san,
                                color = Color(0xFFF8FAFC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = blackMove?.san ?: "",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
