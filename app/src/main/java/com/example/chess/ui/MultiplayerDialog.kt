package com.example.chess.ui

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.GameMode
import com.example.chess.model.TimeControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerDialog(
    viewModel: ChessViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = viewModel.authManager

    val currentUser by authManager.currentUser.collectAsState()
    val isAuthLoading by authManager.isLoading.collectAsState()
    val authError by authManager.errorMessage.collectAsState()

    var isHostTab by remember { mutableStateOf(true) }
    var hostNameInput by remember { mutableStateOf(currentUser?.displayName ?: "Player 1") }
    var guestNameInput by remember { mutableStateOf(currentUser?.displayName ?: "Player 2") }
    var joinRoomCodeInput by remember { mutableStateOf("") }
    var selectedTc by remember { mutableStateOf(TimeControl.BLITZ_5) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }

    val currentRoom by viewModel.multiplayerService.currentRoom.collectAsState()

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("multiplayer_dialog")
            ) {
                Text(
                    text = "Real-Time Online Chess",
                    color = Color(0xFFF1F5F9),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Account / Google Sign-In Status Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (currentUser != null) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentUser?.displayName ?: "Guest (Unauthenticated)",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = currentUser?.email ?: "Sign in for cloud multiplayer",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (currentUser == null) {
                            Button(
                                onClick = {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        authManager.signInWithGoogle(activity, coroutineScope)
                                    }
                                },
                                enabled = !isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.testTag("sign_in_google_button")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White)
                                } else {
                                    Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { authManager.signOut() },
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Text("Sign Out", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                if (authError != null) {
                    Text(
                        text = authError!!,
                        color = Color(0xFFF87171),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (currentRoom != null) {
                    val room = currentRoom!!
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Room Code:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(
                        text = room.roomId,
                        color = Color(0xFF38BDF8),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (room.status == "WAITING") "Waiting for opponent to join..." else "Match Active: ${room.hostPlayerName} vs ${room.guestPlayerName}",
                        color = if (room.status == "WAITING") Color(0xFFF59E0B) else Color(0xFF34D399),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            viewModel.multiplayerService.leaveRoom()
                            onDismiss()
                        }) {
                            Text("Leave Room", color = Color(0xFFEF4444))
                        }
                        if (room.status == "PLAYING") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.resetGame(mode = GameMode.ONLINE)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text("Enter Board")
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Host / Join Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isHostTab,
                            onClick = { isHostTab = true },
                            label = { Text("Create Room", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isHostTab,
                            onClick = { isHostTab = false },
                            label = { Text("Join Room", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isHostTab) {
                        Text("Your Nickname", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = hostNameInput,
                            onValueChange = { hostNameInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Time Control", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(TimeControl.BULLET_1, TimeControl.BLITZ_3, TimeControl.BLITZ_5, TimeControl.RAPID_10).forEach { tc ->
                                FilterChip(
                                    selected = selectedTc == tc,
                                    onClick = { selectedTc = tc },
                                    label = { Text(tc.label.substringBefore(" "), fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0284C7),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isConnecting = true
                                errorMessage = null
                                viewModel.multiplayerService.createRoom(
                                    hostName = hostNameInput,
                                    timeControl = selectedTc,
                                    onSuccess = { isConnecting = false },
                                    onError = {
                                        isConnecting = false
                                        errorMessage = it
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            enabled = !isConnecting,
                            modifier = Modifier.fillMaxWidth().testTag("create_room_submit_button")
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(modifier = Modifier.width(16.dp), color = Color.White)
                            } else {
                                Text("Generate Room Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text("Your Nickname", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = guestNameInput,
                            onValueChange = { guestNameInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Enter 6-Character Room Code", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = joinRoomCodeInput,
                            onValueChange = { joinRoomCodeInput = it.uppercase().take(6) },
                            placeholder = { Text("e.g. 7K2B4X", color = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("room_code_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (joinRoomCodeInput.length == 6) {
                                    isConnecting = true
                                    errorMessage = null
                                    viewModel.multiplayerService.joinRoom(
                                        roomId = joinRoomCodeInput,
                                        guestName = guestNameInput,
                                        onSuccess = {
                                            isConnecting = false
                                            viewModel.resetGame(mode = GameMode.ONLINE)
                                            onDismiss()
                                        },
                                        onError = {
                                            isConnecting = false
                                            errorMessage = it
                                        }
                                    )
                                } else {
                                    errorMessage = "Please enter valid 6-character code"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            enabled = !isConnecting,
                            modifier = Modifier.fillMaxWidth().testTag("join_room_submit_button")
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(modifier = Modifier.width(16.dp), color = Color.White)
                            } else {
                                Text("Join Game", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFF87171),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}
