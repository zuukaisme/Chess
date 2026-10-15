package com.example.chess.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.AiDifficulty
import com.example.chess.model.ChessColor
import com.example.chess.model.ChessPiece
import com.example.chess.model.PieceStyle
import com.example.chess.model.PieceType
import com.example.chess.ui.theme.BoardTheme

@Composable
fun SettingsScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Screen Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                .background(Color(0xFF0F1522))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF38BDF8))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SYSTEM & APPEARANCE CONFIG",
                color = Color(0xFFF1F5F9),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Piece Variant Selection Section
        SettingsCard(
            title = "PIECE VARIANTS & THEMES",
            subtitle = "Switch visual styling of all 32 chess pieces",
            icon = Icons.Default.Extension
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PieceStyle.entries.forEach { style ->
                    val isSelected = uiState.pieceStyle == style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E2838),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(if (isSelected) Color(0xFF0369A1).copy(alpha = 0.2f) else Color(0xFF0A0F1A))
                            .clickable { viewModel.setPieceStyle(style) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("piece_style_${style.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = style.title,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFF1F5F9),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = style.subtitle,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Piece Mini Previews (White Knight, Black King, White Pawn)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF131B2A))
                                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(24.dp)) {
                                ChessPieceView(
                                    piece = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE),
                                    style = style
                                )
                            }
                            Box(modifier = Modifier.size(24.dp)) {
                                ChessPieceView(
                                    piece = ChessPiece(PieceType.KING, ChessColor.BLACK),
                                    style = style
                                )
                            }
                            Box(modifier = Modifier.size(20.dp)) {
                                ChessPieceView(
                                    piece = ChessPiece(PieceType.PAWN, ChessColor.WHITE),
                                    style = style
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Board Theme Selection Section
        SettingsCard(
            title = "CHESSBOARD COLORWAY",
            subtitle = "Custom square surface palettes & contrast themes",
            icon = Icons.Default.Brush
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoardTheme.entries.forEach { theme ->
                    val isSelected = uiState.boardTheme == theme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E2838),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(if (isSelected) Color(0xFF0369A1).copy(alpha = 0.25f) else Color(0xFF0A0F1A))
                            .clickable { viewModel.setBoardTheme(theme) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("theme_option_${theme.name.lowercase()}")
                    ) {
                        // 2x2 board swatch
                        Column(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(3.dp))
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.weight(1f).background(theme.lightSquare))
                                Box(modifier = Modifier.weight(1f).background(theme.darkSquare))
                            }
                            Row(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.weight(1f).background(theme.darkSquare))
                                Box(modifier = Modifier.weight(1f).background(theme.lightSquare))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = theme.title.substringBefore(" "),
                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. AI Engine Difficulty
        SettingsCard(
            title = "STOCKFISH ENGINE CALIBRATION",
            subtitle = "Evaluation search depth and tactical rating target",
            icon = Icons.Default.Speed
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AiDifficulty.entries.take(3).forEach { diff ->
                        DifficultyBadge(diff, uiState.aiDifficulty == diff) {
                            viewModel.setDifficulty(diff)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AiDifficulty.entries.drop(3).forEach { diff ->
                        DifficultyBadge(diff, uiState.aiDifficulty == diff) {
                            viewModel.setDifficulty(diff)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Audio & Gameplay Toggles (Custom Tactical Switches)
        SettingsCard(
            title = "GAMEPLAY & AUDIO FEEDBACK",
            subtitle = "Tactile, audio, and visual assists",
            icon = Icons.Default.VolumeUp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TacticalToggleRow(
                    title = "Sound Effects (Move, Capture, Check)",
                    subtitle = "Auditory feedback on all board interactions",
                    checked = uiState.soundEnabled,
                    onToggle = { viewModel.toggleSound() },
                    tag = "toggle_sound_effects"
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E2838)))

                TacticalToggleRow(
                    title = "Legal Move Target Hints",
                    subtitle = "Tactical destination dots & capture rings",
                    checked = uiState.showLegalMoveHints,
                    onToggle = { viewModel.toggleHints() },
                    tag = "toggle_move_hints"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. System Specs & Network Status Card
        SettingsCard(
            title = "NETWORK & ENGINE PROTOCOL",
            subtitle = "Same-network Wi-Fi multiplayer and AI engine specs",
            icon = Icons.Default.Info
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SpecRow("Engine Core", "Stockfish Minimax + AlphaBeta PST")
                SpecRow("LAN Socket Protocol", "TCP Port 8888 + UDP Broadcast 8889")
                SpecRow("Cloud Multiplayer", "Firebase Firestore Real-time Sync")
                SpecRow("Algebraic Notation", "FIDE Standard (SAN & PGN format)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
            .background(Color(0xFF0F1522))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = title,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1A2333)).padding(bottom = 8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.DifficultyBadge(
    diff: AiDifficulty,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E2838),
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0A0E17))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("ai_diff_${diff.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = diff.label.substringBefore(" "),
                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFF1F5F9),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${diff.elo} ELO",
                color = if (isSelected) Color(0xFF7DD3FC) else Color(0xFF64748B),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun TacticalToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFFF1F5F9),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }

        // Custom cyber toggle switch (not Google Switch)
        val thumbOffset by animateDpAsState(
            targetValue = if (checked) 20.dp else 2.dp,
            animationSpec = tween(150),
            label = "thumb"
        )
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (checked) Color(0xFF0284C7) else Color(0xFF334155), RoundedCornerShape(12.dp))
                .background(if (checked) Color(0xFF0369A1).copy(alpha = 0.5f) else Color(0xFF0F172A))
                .testTag(tag),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (checked) Color(0xFF38BDF8) else Color(0xFF64748B))
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
