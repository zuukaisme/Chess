package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun PlayerBadgeWithClock(
    name: String,
    rating: String,
    color: ChessColor,
    remainingSeconds: Long,
    isActiveTurn: Boolean,
    capturedMaterialDiff: Int = 0,
    modifier: Modifier = Modifier
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val isLowTime = remainingSeconds in 1..15

    val activeBorderColor = when {
        isActiveTurn && isLowTime -> Color(0xFFEF4444)
        isActiveTurn -> Color(0xFF38BDF8)
        else -> Color(0xFF1E2837)
    }

    val plateBg = if (isActiveTurn) Color(0xFF151C28) else Color(0xFF0D121B)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, activeBorderColor, RoundedCornerShape(8.dp))
            .background(plateBg)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("player_clock_${color.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player avatar / side badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (color == ChessColor.WHITE) Color(0xFFE2E8F0) else Color(0xFF475569), CircleShape)
                .background(if (color == ChessColor.WHITE) Color(0xFFF8FAFC) else Color(0xFF1E2430)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (color == ChessColor.WHITE) "W" else "B",
                color = if (color == ChessColor.WHITE) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Name, rank & material difference
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (capturedMaterialDiff > 0) {
                    Text(
                        text = " +$capturedMaterialDiff",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Text(
                text = "Rating: $rating",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Digital countdown clock display
        val clockBg = when {
            isActiveTurn && isLowTime -> Color(0xFF450A0A)
            isActiveTurn -> Color(0xFF0C243C)
            else -> Color(0xFF070B11)
        }
        val clockText = when {
            isActiveTurn && isLowTime -> Color(0xFFF87171)
            isActiveTurn -> Color(0xFF38BDF8)
            else -> Color(0xFF94A3B8)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, if (isActiveTurn) activeBorderColor else Color(0xFF1E2837), RoundedCornerShape(6.dp))
                .background(clockBg)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = timeFormatted,
                color = clockText,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
