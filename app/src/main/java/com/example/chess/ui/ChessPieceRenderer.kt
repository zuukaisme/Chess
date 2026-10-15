package com.example.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.chess.model.ChessColor
import com.example.chess.model.ChessPiece
import com.example.chess.model.PieceStyle
import com.example.chess.model.PieceType

@Composable
fun ChessPieceView(
    piece: ChessPiece,
    style: PieceStyle = PieceStyle.WIKIPEDIA,
    modifier: Modifier = Modifier
) {
    if (style == PieceStyle.MINIMAL_BAUHAUS) {
        Box(
            modifier = modifier.fillMaxSize().padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            BauhausPieceView(piece = piece)
        }
        return
    }

    val drawableRes = when (style) {
        PieceStyle.WIKIPEDIA -> when (piece.color) {
            ChessColor.WHITE -> when (piece.type) {
                PieceType.PAWN -> R.drawable.ic_chess_wp
                PieceType.KNIGHT -> R.drawable.ic_chess_wn
                PieceType.BISHOP -> R.drawable.ic_chess_wb
                PieceType.ROOK -> R.drawable.ic_chess_wr
                PieceType.QUEEN -> R.drawable.ic_chess_wq
                PieceType.KING -> R.drawable.ic_chess_wk
            }
            ChessColor.BLACK -> when (piece.type) {
                PieceType.PAWN -> R.drawable.ic_chess_bp
                PieceType.KNIGHT -> R.drawable.ic_chess_bn
                PieceType.BISHOP -> R.drawable.ic_chess_bb
                PieceType.ROOK -> R.drawable.ic_chess_br
                PieceType.QUEEN -> R.drawable.ic_chess_bq
                PieceType.KING -> R.drawable.ic_chess_bk
            }
        }
        PieceStyle.ALPHA, PieceStyle.CYBER -> when (piece.color) {
            ChessColor.WHITE -> when (piece.type) {
                PieceType.PAWN -> R.drawable.ic_chess_alpha_wp
                PieceType.KNIGHT -> R.drawable.ic_chess_alpha_wn
                PieceType.BISHOP -> R.drawable.ic_chess_alpha_wb
                PieceType.ROOK -> R.drawable.ic_chess_alpha_wr
                PieceType.QUEEN -> R.drawable.ic_chess_alpha_wq
                PieceType.KING -> R.drawable.ic_chess_alpha_wk
            }
            ChessColor.BLACK -> when (piece.type) {
                PieceType.PAWN -> R.drawable.ic_chess_alpha_bp
                PieceType.KNIGHT -> R.drawable.ic_chess_alpha_bn
                PieceType.BISHOP -> R.drawable.ic_chess_alpha_bb
                PieceType.ROOK -> R.drawable.ic_chess_alpha_br
                PieceType.QUEEN -> R.drawable.ic_chess_alpha_bq
                PieceType.KING -> R.drawable.ic_chess_alpha_bk
            }
        }
        PieceStyle.CHESS7, PieceStyle.ROYAL_GOLD, PieceStyle.WOODEN_LUXE -> when (piece.color) {
            ChessColor.WHITE -> when (piece.type) {
                PieceType.PAWN -> R.drawable.ic_chess_c7_wp
                PieceType.KNIGHT -> R.drawable.ic_chess_c7_wn
                PieceType.BISHOP -> R.drawable.ic_chess_c7_wb
                PieceType.ROOK -> R.drawable.ic_chess_c7_wr
                PieceType.QUEEN -> R.drawable.ic_chess_c7_wq
                PieceType.KING -> R.drawable.ic_chess_c7_wk
            }
            ChessColor.BLACK -> when (piece.type) {
                PieceType.PAWN -> R.drawable.ic_chess_c7_bp
                PieceType.KNIGHT -> R.drawable.ic_chess_c7_bn
                PieceType.BISHOP -> R.drawable.ic_chess_c7_bb
                PieceType.ROOK -> R.drawable.ic_chess_c7_br
                PieceType.QUEEN -> R.drawable.ic_chess_c7_bq
                PieceType.KING -> R.drawable.ic_chess_c7_bk
            }
        }
        PieceStyle.MINIMAL_BAUHAUS -> R.drawable.ic_chess_wp // Handled above
    }

    val colorFilter = when (style) {
        PieceStyle.CYBER -> {
            if (piece.color == ChessColor.WHITE) {
                ColorFilter.tint(Color(0xFF38BDF8))
            } else {
                ColorFilter.tint(Color(0xFFF43F5E))
            }
        }
        PieceStyle.ROYAL_GOLD -> {
            if (piece.color == ChessColor.WHITE) {
                ColorFilter.tint(Color(0xFFFBBF24))
            } else {
                ColorFilter.tint(Color(0xFF10B981))
            }
        }
        PieceStyle.WOODEN_LUXE -> {
            if (piece.color == ChessColor.WHITE) {
                ColorFilter.tint(Color(0xFFFDE68A))
            } else {
                ColorFilter.tint(Color(0xFF9A3412))
            }
        }
        else -> null
    }

    Box(
        modifier = modifier.fillMaxSize().padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "${piece.color} ${piece.type}",
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
            modifier = Modifier.fillMaxSize(0.92f)
        )
    }
}

@Composable
fun BauhausPieceView(
    piece: ChessPiece,
    modifier: Modifier = Modifier
) {
    val pieceColor = if (piece.color == ChessColor.WHITE) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val strokeColor = if (piece.color == ChessColor.WHITE) Color(0xFF334155) else Color(0xFF94A3B8)

    Canvas(modifier = modifier.fillMaxSize(0.82f)) {
        val w = size.width
        val h = size.height
        when (piece.type) {
            PieceType.PAWN -> {
                val pSize = w * 0.42f
                val left = (w - pSize) / 2f
                val top = (h - pSize) / 2f
                drawRect(color = pieceColor, topLeft = Offset(left, top), size = Size(pSize, pSize))
                drawRect(color = strokeColor, topLeft = Offset(left, top), size = Size(pSize, pSize), style = Stroke(width = 2.dp.toPx()))
            }
            PieceType.ROOK -> {
                val rSize = w * 0.72f
                val left = (w - rSize) / 2f
                val top = (h - rSize) / 2f
                drawRect(color = pieceColor, topLeft = Offset(left, top), size = Size(rSize, rSize))
                drawRect(color = strokeColor, topLeft = Offset(left, top), size = Size(rSize, rSize), style = Stroke(width = 2.5.dp.toPx()))
            }
            PieceType.KNIGHT -> {
                val path = Path().apply {
                    moveTo(w * 0.25f, h * 0.85f)
                    lineTo(w * 0.75f, h * 0.85f)
                    lineTo(w * 0.75f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.15f)
                    lineTo(w * 0.25f, h * 0.15f)
                    close()
                }
                drawPath(path, color = pieceColor)
                drawPath(path, color = strokeColor, style = Stroke(width = 2.5.dp.toPx()))
            }
            PieceType.BISHOP -> {
                val path = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f)
                    lineTo(w * 0.78f, h * 0.78f)
                    moveTo(w * 0.78f, h * 0.22f)
                    lineTo(w * 0.22f, h * 0.78f)
                }
                drawPath(path, color = pieceColor, style = Stroke(width = w * 0.18f, cap = StrokeCap.Square))
                drawPath(path, color = strokeColor, style = Stroke(width = w * 0.05f, cap = StrokeCap.Square))
            }
            PieceType.QUEEN -> {
                drawCircle(color = pieceColor, radius = w * 0.32f, center = Offset(w / 2f, h * 0.56f))
                drawCircle(color = strokeColor, radius = w * 0.32f, center = Offset(w / 2f, h * 0.56f), style = Stroke(width = 2.5.dp.toPx()))
                drawCircle(color = pieceColor, radius = w * 0.12f, center = Offset(w / 2f, h * 0.18f))
                drawCircle(color = strokeColor, radius = w * 0.12f, center = Offset(w / 2f, h * 0.18f), style = Stroke(width = 2.dp.toPx()))
            }
            PieceType.KING -> {
                val baseW = w * 0.62f
                val baseH = h * 0.42f
                val left = (w - baseW) / 2f
                val top = h * 0.5f
                drawRect(color = pieceColor, topLeft = Offset(left, top), size = Size(baseW, baseH))
                drawRect(color = strokeColor, topLeft = Offset(left, top), size = Size(baseW, baseH), style = Stroke(width = 2.5.dp.toPx()))

                val diamondPath = Path().apply {
                    moveTo(w / 2f, h * 0.12f)
                    lineTo(w * 0.68f, h * 0.30f)
                    lineTo(w / 2f, h * 0.48f)
                    lineTo(w * 0.32f, h * 0.30f)
                    close()
                }
                drawPath(diamondPath, color = pieceColor)
                drawPath(diamondPath, color = strokeColor, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}
