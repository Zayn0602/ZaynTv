package com.example.ui.provider

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Provider
import com.example.data.model.ProviderType
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynLiveRed

@Composable
fun AddProviderScreen(
    viewModel: ZaynTvViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val theme = LocalAppThemeColors.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: M3U URL, 1: Xtream, 2: Local File, 3: Stalker

    // M3U URL State
    var m3uName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var m3uEpgUrl by remember { mutableStateOf("") }

    // Xtream State
    var xtreamName by remember { mutableStateOf("") }
    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Stalker State
    var stalkerName by remember { mutableStateOf("") }
    var stalkerPortal by remember { mutableStateOf("") }
    var stalkerMac by remember { mutableStateOf("") }

    // File Picker for local M3U/M3U8 playlists (SAF)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Local Playlist"
                    viewModel.importLocalM3u(displayName, inputStream)
                }
            } catch (e: Exception) {
                viewModel.showToast("Failed to open file: ${e.message}")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { viewModel.navigateTo(CurrentScreen.PROVIDERS) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADD IPTV PROVIDER",
                    color = theme.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf("M3U URL", "Xtream Codes", "Local File", "Stalker / MAG")
                tabs.forEachIndexed { index, tabTitle ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) theme.primary else theme.card)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabTitle,
                            color = if (isSelected) theme.background else theme.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            ProviderField(value = m3uName, onValueChange = { m3uName = it }, label = "Playlist Name", placeholder = "e.g. My Premium Playlist")
                            Spacer(modifier = Modifier.height(14.dp))
                            ProviderField(value = m3uUrl, onValueChange = { m3uUrl = it }, label = "M3U / M3U8 URL", placeholder = "http://provider.com/playlist.m3u8")
                            Spacer(modifier = Modifier.height(14.dp))
                            ProviderField(value = m3uEpgUrl, onValueChange = { m3uEpgUrl = it }, label = "EPG / XMLTV URL (Optional)", placeholder = "http://provider.com/epg.xml")
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    if (m3uUrl.isNotBlank()) viewModel.addM3uProvider(m3uName, m3uUrl, m3uEpgUrl)
                                    else viewModel.showToast("Please enter an M3U URL")
                                },
                                enabled = !uiState.isImporting,
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                if (uiState.isImporting) {
                                    CircularProgressIndicator(color = theme.background, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("PARSING PLAYLIST...", color = theme.background, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("CONNECT & LOAD CHANNELS", color = theme.background, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    1 -> {
                        item {
                            ProviderField(value = xtreamName, onValueChange = { xtreamName = it }, label = "Provider Name", placeholder = "e.g. Premium Xtream")
                            Spacer(modifier = Modifier.height(14.dp))
                            ProviderField(value = xtreamServer, onValueChange = { xtreamServer = it }, label = "Server URL (with port)", placeholder = "http://server.net:8080")
                            Spacer(modifier = Modifier.height(14.dp))
                            ProviderField(value = xtreamUser, onValueChange = { xtreamUser = it }, label = "Username", placeholder = "Account Username")
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = xtreamPassword,
                                onValueChange = { xtreamPassword = it },
                                label = { Text("Password", color = theme.textSecondary) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = theme.textSecondary)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.primary,
                                    unfocusedBorderColor = theme.primary.copy(alpha = 0.2f),
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        if (xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPassword.isNotBlank()) {
                                            val dummy = Provider(
                                                id = "test",
                                                name = "Test",
                                                type = ProviderType.XTREAM,
                                                url = xtreamServer,
                                                username = xtreamUser,
                                                password = xtreamPassword
                                            )
                                            viewModel.testConnection(dummy)
                                        } else {
                                            viewModel.showToast("Fill in Server, User and Password first")
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = theme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("TEST CONNECTION", color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        if (xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPassword.isNotBlank()) {
                                            viewModel.addXtreamProvider(xtreamName, xtreamServer, xtreamUser, xtreamPassword)
                                        } else {
                                            viewModel.showToast("Please fill in Server, Username and Password")
                                        }
                                    },
                                    enabled = !uiState.isImporting,
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                    modifier = Modifier.weight(1.2f).height(50.dp)
                                ) {
                                    if (uiState.isImporting) {
                                        CircularProgressIndicator(color = theme.background, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text("CONNECT XTREAM", color = theme.background, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(theme.card)
                                    .border(1.dp, theme.primary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FileOpen, contentDescription = null, tint = theme.primary, modifier = Modifier.size(54.dp))
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("Select Local M3U File", color = theme.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Browse internal storage or USB drive for .m3u/.m3u8 files", color = theme.textSecondary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                                        enabled = !uiState.isImporting,
                                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                                    ) {
                                        Text("BROWSE FILES", color = theme.background, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        item {
                            ProviderField(value = stalkerName, onValueChange = { stalkerName = it }, label = "Portal Name", placeholder = "e.g. My MAG Portal")
                            Spacer(modifier = Modifier.height(14.dp))
                            ProviderField(value = stalkerPortal, onValueChange = { stalkerPortal = it }, label = "Portal URL", placeholder = "http://portal.server.com:8080/c/")
                            Spacer(modifier = Modifier.height(14.dp))
                            ProviderField(value = stalkerMac, onValueChange = { stalkerMac = it }, label = "MAC Address", placeholder = "00:1A:79:XX:XX:XX")
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    if (stalkerPortal.isNotBlank() && stalkerMac.isNotBlank()) {
                                        viewModel.addStalkerProvider(stalkerName, stalkerPortal, stalkerMac)
                                    } else {
                                        viewModel.showToast("Please enter Portal URL and MAC Address")
                                    }
                                },
                                enabled = !uiState.isImporting,
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("CONNECT STALKER PORTAL", color = theme.background, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Connection Test Result Dialog
        uiState.testConnectionResult?.let { res ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, if (res.isSuccess) theme.primary else ZaynLiveRed, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (res.isSuccess) "CONNECTION SUCCESSFUL" else "CONNECTION FAILED",
                        color = if (res.isSuccess) theme.primary else ZaynLiveRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(res.statusMessage, color = theme.textPrimary, fontSize = 14.sp)
                    if (res.isSuccess) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• Server Reachable: YES", color = theme.primary, fontSize = 13.sp)
                        Text("• Authentication: SUCCESS", color = theme.primary, fontSize = 13.sp)
                        Text("• Live Channels Found: ${res.liveCount}", color = theme.textPrimary, fontSize = 13.sp)
                        Text("• Expiration Date: ${res.expirationDate}", color = theme.textSecondary, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.dismissTestConnectionDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("OK", color = theme.background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Import Result Modal Dialog
        uiState.importSummary?.let { summary ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, theme.primary, RoundedCornerShape(16.dp))
                        .padding(28.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    if (summary.playableCount > 0) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = theme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Playlist imported successfully", color = theme.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(summary.providerName, color = theme.primary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.background)
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("Total entries found: ${summary.totalEntries}", color = theme.textPrimary, fontSize = 14.sp)
                            Text("Playable channels: ${summary.playableCount}", color = theme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.dismissImportSummaryAndPlay() },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("VIEW CHANNELS & START PLAYBACK", color = theme.background, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ZaynLiveRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No playable channels found", color = theme.textPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.dismissImportSummary() },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TRY AGAIN", color = theme.background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    val theme = LocalAppThemeColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = theme.textSecondary) },
        placeholder = { Text(placeholder, color = theme.textSecondary.copy(alpha = 0.5f)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = theme.primary,
            unfocusedBorderColor = theme.primary.copy(alpha = 0.2f),
            focusedTextColor = theme.textPrimary,
            unfocusedTextColor = theme.textPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
