package com.example.ui.management

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynLiveRed

@Composable
fun ChannelManagementScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current

    var editingChannelForRename by remember { mutableStateOf<Channel?>(null) }
    var editingChannelForNumber by remember { mutableStateOf<Channel?>(null) }
    var tempName by remember { mutableStateOf("") }
    var tempNumber by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(20.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(CurrentScreen.SETTINGS) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CHANNEL MANAGEMENT",
                    color = theme.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            // Restore Original Button
            uiState.activeProvider?.let { prov ->
                OutlinedButton(
                    onClick = { viewModel.restoreOriginalProviderList(prov.id) }
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = theme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESTORE ORIGINAL", color = theme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.channels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No channels found. Add a provider first.", color = theme.textSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(uiState.channels, key = { _, ch -> ch.id }) { index, channel ->
                    val isHidden = uiState.hiddenChannelIds.contains(channel.id) || channel.isCustomHidden
                    val isFav = uiState.favorites.contains(channel.id)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isHidden) theme.card.copy(alpha = 0.4f) else theme.card)
                            .border(1.dp, if (isHidden) theme.textSecondary.copy(alpha = 0.2f) else theme.primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Channel Number & Name
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = channel.formattedNumber,
                                color = theme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(38.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = channel.displayName,
                                    color = if (isHidden) theme.textSecondary else theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = channel.displayGroup,
                                    color = theme.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Action Buttons: Reorder Up/Down, Edit Number, Rename, Hide/Show, Favorite
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.moveChannelOrder(channel.id, true) }, enabled = index > 0) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) theme.textPrimary else theme.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { viewModel.moveChannelOrder(channel.id, false) }, enabled = index < uiState.channels.size - 1) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (index < uiState.channels.size - 1) theme.textPrimary else theme.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                editingChannelForNumber = channel
                                tempNumber = channel.displayChannelNumber.toString()
                            }) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = "Change Number", tint = theme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                editingChannelForRename = channel
                                tempName = channel.displayName
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", tint = theme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                if (isHidden) viewModel.restoreHiddenChannel(channel.id) else viewModel.hideChannel(channel.id)
                            }) {
                                Icon(
                                    imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = if (isHidden) ZaynLiveRed else theme.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.toggleFavorite(channel.id) }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) Color(0xFFFFD700) else theme.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rename Dialog
        editingChannelForRename?.let { ch ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, theme.primary, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text("RENAME CHANNEL", color = theme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Channel Name", color = theme.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.primary.copy(alpha = 0.3f),
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { editingChannelForRename = null }) {
                            Text("CANCEL", color = theme.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (tempName.isNotBlank()) {
                                    viewModel.renameChannelLocally(ch.id, tempName)
                                }
                                editingChannelForRename = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Text("SAVE", color = theme.background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Custom Number Dialog
        editingChannelForNumber?.let { ch ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, theme.primary, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text("CHANGE CHANNEL NUMBER", color = theme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = tempNumber,
                        onValueChange = { if (it.all { c -> c.isDigit() }) tempNumber = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Channel Number (e.g. 101)", color = theme.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.primary.copy(alpha = 0.3f),
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { editingChannelForNumber = null }) {
                            Text("CANCEL", color = theme.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val num = tempNumber.toIntOrNull()
                                if (num != null && num > 0) {
                                    viewModel.setCustomChannelNumber(ch.id, num)
                                }
                                editingChannelForNumber = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Text("SAVE", color = theme.background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
