package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.chess.ui.AnalysisScreen
import com.example.chess.ui.ChessViewModel
import com.example.chess.ui.GameScreen
import com.example.chess.ui.LanMultiplayerDialog
import com.example.chess.ui.MultiplayerDialog
import com.example.chess.ui.PuzzlesScreen
import com.example.chess.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    PLAY("PLAY", Icons.Default.SportsEsports, "tab_play"),
    PUZZLES("TACTICS", Icons.Default.Psychology, "tab_puzzles"),
    ANALYSIS("ANALYSIS", Icons.Default.Analytics, "tab_analysis"),
    SETTINGS("SETTINGS", Icons.Default.Tune, "tab_settings")
}

class MainActivity : ComponentActivity() {
    private val chessViewModel: ChessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(AppTab.PLAY) }
                var showMultiplayerDialog by remember { mutableStateOf(false) }
                var showLanDialog by remember { mutableStateOf(false) }

                BackHandler(enabled = currentTab != AppTab.PLAY) {
                    currentTab = AppTab.PLAY
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Bespoke dark esports header bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .background(Color(0xFF06090E))
                                .border(1.dp, Color(0xFF161F2E))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8))
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Text(
                                        text = "CHESS MASTER",
                                        color = Color(0xFFF8FAFC),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.2.sp
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // LAN Match button (Same Network Wi-Fi)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                                            .background(Color(0xFF064E3B).copy(alpha = 0.35f))
                                            .clickable { showLanDialog = true }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                            .testTag("lan_multiplayer_top_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Wifi,
                                                contentDescription = "LAN",
                                                tint = Color(0xFF34D399),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "LAN",
                                                color = Color(0xFF34D399),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    // Cloud Multiplayer button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(4.dp))
                                            .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                                            .clickable { showMultiplayerDialog = true }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                            .testTag("online_multiplayer_top_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = "Cloud",
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "CLOUD",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        // Custom dock navigation bar (replaces default bubbly Google NavigationBar)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .background(Color(0xFF06090E))
                                .border(1.dp, Color(0xFF151D2A))
                                .padding(horizontal = 6.dp, vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppTab.entries.forEach { tab ->
                                    val isSelected = currentTab == tab
                                    val activeColor = Color(0xFF38BDF8)
                                    val inactiveColor = Color(0xFF64748B)

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) Color(0xFF0C1929) else Color.Transparent)
                                            .border(
                                                if (isSelected) 0.5.dp else 0.dp,
                                                if (isSelected) Color(0xFF0284C7) else Color.Transparent,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { currentTab = tab }
                                            .padding(vertical = 5.dp)
                                            .testTag(tab.tag)
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            tint = if (isSelected) activeColor else inactiveColor,
                                            modifier = Modifier.size(19.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tab.title,
                                            color = if (isSelected) activeColor else inactiveColor,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF070B12))
                    ) {
                        when (currentTab) {
                            AppTab.PLAY -> GameScreen(
                                viewModel = chessViewModel,
                                onOpenMultiplayer = { showMultiplayerDialog = true },
                                onOpenLan = { showLanDialog = true }
                            )
                            AppTab.PUZZLES -> PuzzlesScreen(viewModel = chessViewModel)
                            AppTab.ANALYSIS -> AnalysisScreen(viewModel = chessViewModel)
                            AppTab.SETTINGS -> SettingsScreen(viewModel = chessViewModel)
                        }

                        if (showLanDialog) {
                            LanMultiplayerDialog(
                                viewModel = chessViewModel,
                                onDismiss = { showLanDialog = false }
                            )
                        }

                        if (showMultiplayerDialog) {
                            MultiplayerDialog(
                                viewModel = chessViewModel,
                                onDismiss = { showMultiplayerDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
