package com.example.chess.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Vertical progress indicator component that visually displays the AI's
 * real-time engine evaluation score of the current board position.
 *
 * Smoothly interpolates score between White and Black advantage using an
 * animated progress bar with algebraic score labels and equality markings.
 */
@Composable
fun EvaluationProgressIndicator(
    centipawns: Int,
    flipped: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Sigmoid curve mapping centipawns (-1200..+1200) to 0.05..0.95 height ratio
    // 0 cp -> 50% (Equality)
    // +100 cp (+1 pawn) -> ~60%
    // +300 cp (+3 pawns) -> ~75%
    // +600 cp (+6 pawns) -> ~88%
    val clamped = max(-1500, min(1500, centipawns))
    val whitePercentage = (1.0f / (1.0f + Math.exp(-clamped / 280.0).toFloat())).coerceIn(0.04f, 0.96f)

    // Account for perspective: when flipped, Black is on bottom, White on top
    val targetBottomProgress = if (flipped) (1f - whitePercentage) else whitePercentage

    val animatedBottomProgress by animateFloatAsState(
        targetValue = targetBottomProgress,
        animationSpec = tween(durationMillis = 350),
        label = "eval_bar_animation"
    )

    val isMate = abs(centipawns) >= 15000
    val evalLabel = when {
        isMate -> if (centipawns > 0) "+M" else "-M"
        abs(centipawns) < 15 -> "0.0"
        else -> {
            val pawns = centipawns / 100.0f
            if (pawns > 0) String.format("+%.1f", pawns) else String.format("%.1f", pawns)
        }
    }

    val isWhiteWinning = centipawns > 25
    val isBlackWinning = centipawns < -25

    Box(
        modifier = modifier
            .width(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF263346), RoundedCornerShape(6.dp))
            .background(Color(0xFF0F141F))
            .testTag("evaluation_progress_indicator")
    ) {
        // Vertical indicator column
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Section
            // If not flipped: Top is Black. If flipped: Top is White.
            val topWeight = (1f - animatedBottomProgress).coerceAtLeast(0.02f)
            val isTopWhite = flipped
            val topBackground = if (isTopWhite) {
                Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF141923), Color(0xFF1F2736)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(topWeight)
                    .background(topBackground),
                contentAlignment = Alignment.TopCenter
            ) {
                val showTopScore = if (isTopWhite) isWhiteWinning else isBlackWinning
                if (showTopScore) {
                    Text(
                        text = evalLabel,
                        color = if (isTopWhite) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Bottom Section
            // If not flipped: Bottom is White. If flipped: Bottom is Black.
            val bottomWeight = animatedBottomProgress.coerceAtLeast(0.02f)
            val isBottomWhite = !flipped
            val bottomBackground = if (isBottomWhite) {
                Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFFFFFFF)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF1F2736), Color(0xFF141923)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(bottomWeight)
                    .background(bottomBackground),
                contentAlignment = Alignment.BottomCenter
            ) {
                val showBottomScore = if (isBottomWhite) isWhiteWinning else isBlackWinning
                if (showBottomScore) {
                    Text(
                        text = evalLabel,
                        color = if (isBottomWhite) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else if (!isWhiteWinning && !isBlackWinning) {
                    // Equality text at bottom
                    Text(
                        text = "0.0",
                        color = if (isBottomWhite) Color(0xFF475569) else Color(0xFF94A3B8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }

        // Precision tick marks overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Center equality benchmark
            val centerY = h * 0.5f
            drawLine(
                color = Color(0x9964748B),
                start = Offset(0f, centerY),
                end = Offset(w, centerY),
                strokeWidth = 1.5f
            )

            // Advantage ticks: +1, +3, +5 and -1, -3, -5
            val tickPositions = floatArrayOf(0.20f, 0.32f, 0.41f, 0.59f, 0.68f, 0.80f)
            for (p in tickPositions) {
                val y = h * p
                drawLine(
                    color = Color(0x5564748B),
                    start = Offset(w * 0.6f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }
    }
}
