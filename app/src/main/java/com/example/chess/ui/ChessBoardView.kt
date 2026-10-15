package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.ChessBoard
import com.example.chess.model.ChessColor
import com.example.chess.model.Move
import com.example.chess.model.PieceStyle
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import com.example.chess.ui.theme.BoardTheme

@Composable
fun ChessBoardView(
    board: ChessBoard,
    boardTheme: BoardTheme,
    flipped: Boolean = false,
    pieceStyle: PieceStyle = PieceStyle.WIKIPEDIA,
    isCheckmate: Boolean = false,
    selectedSquare: Square? = null,
    legalMoves: List<Move> = emptyList(),
    lastMove: Move? = null,
    hintMove: Move? = null,
    showLegalMoveHints: Boolean = true,
    onSquareClick: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    val inCheckKingSquare = remember(board.turn, board.isInCheck(board.turn)) {
        if (board.isInCheck(board.turn)) board.findKing(board.turn) else null
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF141A24))
            .testTag("chess_board")
    ) {
        val squareSize = maxWidth / 8

        Column(modifier = Modifier.fillMaxSize()) {
            for (displayRow in 0..7) {
                val actualRow = if (flipped) 7 - displayRow else displayRow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (displayCol in 0..7) {
                        val actualCol = if (flipped) 7 - displayCol else displayCol
                        val square = Square(actualRow, actualCol)
                        val piece = board.getPiece(square)

                        val isSelected = square == selectedSquare
                        val isLastMove = lastMove != null && (square == lastMove.from || square == lastMove.to)
                        val isInCheck = square == inCheckKingSquare
                        val isMatedKing = isCheckmate && (square == inCheckKingSquare || square == board.findKing(board.turn))
                        val targetMove = legalMoves.find { it.to == square }
                        val isLegalTarget = targetMove != null

                        // Square background color
                        val baseColor = if (square.isLight) boardTheme.lightSquare else boardTheme.darkSquare
                        val backgroundColor = when {
                            isMatedKing -> Color(0xFFDC2626) // Vivid crimson checkmate
                            isInCheck -> boardTheme.checkAlert
                            isSelected -> boardTheme.highlightSelected
                            isLastMove -> boardTheme.highlightLastMove
                            else -> baseColor
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(backgroundColor)
                                .then(
                                    if (isMatedKing) Modifier.border(2.dp, Color(0xFFFCA5A5))
                                    else if (isInCheck) Modifier.border(1.5.dp, Color(0xFFEF4444))
                                    else Modifier
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onSquareClick(square)
                                }
                                .testTag("square_${square.notation}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isMatedKing) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF7F1D1D))
                                        .padding(horizontal = 2.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "#MATE",
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            // Coordinate labels
                            val coordColor = if (square.isLight) boardTheme.darkSquare else boardTheme.lightSquare

                            // Rank numbers on file 0 (left column)
                            if (displayCol == 0) {
                                Text(
                                    text = (8 - actualRow).toString(),
                                    color = coordColor.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 3.dp, top = 2.dp)
                                )
                            }

                            // File letters on rank 0 (bottom row)
                            if (displayRow == 7) {
                                Text(
                                    text = ('a'.code + actualCol).toChar().toString(),
                                    color = coordColor.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 3.dp, bottom = 2.dp)
                                )
                            }

                            // Piece rendering with selected style variant
                            if (piece != null) {
                                ChessPieceView(
                                    piece = piece,
                                    style = pieceStyle,
                                    modifier = Modifier.fillMaxSize(0.92f)
                                )
                            }

                            // Legal move indicator
                            if (showLegalMoveHints && isLegalTarget) {
                                if (piece == null && !targetMove!!.isEnPassant) {
                                    // Move dot for empty squares
                                    Box(
                                        modifier = Modifier
                                            .size(squareSize * 0.32f)
                                            .clip(CircleShape)
                                            .background(boardTheme.highlightLegalMove)
                                    )
                                } else {
                                    // Capture ring for occupied or en passant squares
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            color = boardTheme.highlightLegalMove,
                                            radius = size.minDimension * 0.44f,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = size.minDimension * 0.09f
                                            )
                                        )
                                    }
                                }
                            }

                            // Best Move Hint indicator (arrow line)
                            if (hintMove != null && (square == hintMove.from || square == hintMove.to)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.35f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
