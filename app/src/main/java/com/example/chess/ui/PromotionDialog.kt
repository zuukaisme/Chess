package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.ChessColor
import com.example.chess.model.ChessPiece
import com.example.chess.model.PieceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionDialog(
    color: ChessColor,
    onSelectPiece: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .testTag("promotion_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Promote Pawn",
                    color = Color(0xFFF8FAFC),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val promotionOptions = listOf(
                        PieceType.QUEEN to "Queen",
                        PieceType.ROOK to "Rook",
                        PieceType.BISHOP to "Bishop",
                        PieceType.KNIGHT to "Knight"
                    )

                    for ((pieceType, name) in promotionOptions) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .clickable { onSelectPiece(pieceType) }
                                .padding(8.dp)
                                .testTag("promote_to_${pieceType.name.lowercase()}")
                        ) {
                            Box(modifier = Modifier.size(52.dp)) {
                                ChessPieceView(piece = ChessPiece(pieceType, color))
                            }
                            Text(
                                text = name,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
