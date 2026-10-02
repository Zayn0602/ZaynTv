package com.example.ui.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FilterRule
import com.example.data.model.FilterType
import com.example.data.model.Provider
import com.example.data.model.ProviderType
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynLiveRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ProvidersScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current

    var providerToDelete by remember { mutableStateOf<Provider?>(null) }
    var isFilterDialogOpen by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf(FilterType.CATEGORY) }
    var filterPatternInput by remember { mutableStateOf("") }
    var isExcludeFilter by remember { mutableStateOf(true) }

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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo(CurrentScreen.LIVE_TV) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "IPTV PROVIDERS",
                        color = theme.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { isFilterDialogOpen = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = theme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FILTERS (${uiState.filterRules.size})", color = theme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(CurrentScreen.ADD_PROVIDER) },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = theme.background, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD PROVIDER", color = theme.background, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.providers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No IPTV providers configured.", color = theme.textSecondary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.navigateTo(CurrentScreen.ADD_PROVIDER) },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Text("ADD M3U / XTREAM PROVIDER", color = theme.background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.providers, key = { it.id }) { provider ->
                        ProviderCard(
                            provider = provider,
                            isActive = uiState.activeProvider?.id == provider.id,
                            onSetActive = {
                                viewModel.selectProvider(provider)
                                viewModel.navigateTo(CurrentScreen.LIVE_TV)
                            },
                            onRefresh = { viewModel.refreshProvider(provider) },
                            onTest = { viewModel.testProvider(provider) },
                            onDelete = { providerToDelete = provider }
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        providerToDelete?.let { prov ->
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
                        .border(1.dp, ZaynLiveRed, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text("DELETE PROVIDER", color = ZaynLiveRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Are you sure you want to remove '${prov.name}' from ZaynTV? Cached channels will be deleted locally.",
                        color = theme.textPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = { providerToDelete = null }) {
                            Text("CANCEL", color = theme.textSecondary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                viewModel.deleteProvider(prov.id)
                                providerToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZaynLiveRed)
                        ) {
                            Text("DELETE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Filter Rules Dialog
        if (isFilterDialogOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xEE000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, theme.primary, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PROVIDER FILTERS", color = theme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { viewModel.resetAllFilters() }) {
                            Text("RESET ALL", color = ZaynLiveRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Filter out unwanted categories (e.g. Adult, For Kids) or include only specific groups.",
                        color = theme.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Add new filter rule row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Include / Exclude Toggle
                        OutlinedButton(
                            onClick = { isExcludeFilter = !isExcludeFilter }
                        ) {
                            Text(
                                text = if (isExcludeFilter) "EXCLUDE" else "INCLUDE",
                                color = if (isExcludeFilter) ZaynLiveRed else theme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Filter Type
                        OutlinedButton(
                            onClick = {
                                selectedFilterType = when (selectedFilterType) {
                                    FilterType.CATEGORY -> FilterType.GROUP
                                    FilterType.GROUP -> FilterType.CHANNEL_NAME
                                    FilterType.CHANNEL_NAME -> FilterType.CATEGORY
                                }
                            }
                        ) {
                            Text(selectedFilterType.name, color = theme.textPrimary, fontSize = 12.sp)
                        }

                        // Pattern Input
                        OutlinedTextField(
                            value = filterPatternInput,
                            onValueChange = { filterPatternInput = it },
                            placeholder = { Text("e.g. Adult, News, Sports", color = theme.textSecondary.copy(alpha = 0.5f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primary,
                                unfocusedBorderColor = theme.primary.copy(alpha = 0.2f),
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (filterPatternInput.isNotBlank()) {
                                    viewModel.addFilterRule(
                                        FilterRule(
                                            id = UUID.randomUUID().toString(),
                                            type = selectedFilterType,
                                            pattern = filterPatternInput.trim(),
                                            isExclude = isExcludeFilter,
                                            isEnabled = true
                                        )
                                    )
                                    filterPatternInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Text("ADD", color = theme.background, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Filters List
                    if (uiState.filterRules.isEmpty()) {
                        Text("No active filters. All channels and categories are shown.", color = theme.textSecondary, fontSize = 13.sp)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.height(180.dp)
                        ) {
                            items(uiState.filterRules, key = { it.id }) { rule ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(theme.background)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (rule.isExclude) "EXCLUDE" else "INCLUDE",
                                            color = if (rule.isExclude) ZaynLiveRed else theme.primary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("[${rule.type.name}]", color = theme.textSecondary, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("\"${rule.pattern}\"", color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeFilterRule(rule.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Rule", tint = ZaynLiveRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { isFilterDialogOpen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("DONE", color = theme.background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: Provider,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onRefresh: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val dateStr = remember(provider.lastUpdated) {
        if (provider.lastUpdated > 0) {
            SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(provider.lastUpdated))
        } else "Never"
    }

    val typeLabel = when (provider.type) {
        ProviderType.M3U_URL -> "M3U URL"
        ProviderType.M3U_LOCAL -> "Local M3U File"
        ProviderType.XTREAM -> "Xtream Codes API"
        ProviderType.STALKER -> "MAC / Stalker Portal"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) theme.primary.copy(alpha = 0.15f) else theme.card)
            .border(
                width = if (isActive || isFocused) 2.dp else 1.dp,
                color = if (isActive) theme.primary else if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = theme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = provider.name,
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(typeLabel, color = theme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Channels: ${provider.channelCount}", color = theme.textSecondary, fontSize = 13.sp)
            Text("Updated: $dateStr", color = theme.textSecondary, fontSize = 13.sp)
            Text("Status: ${if (provider.isActive) "Active" else "Disabled"}", color = if (provider.isActive) theme.primary else ZaynLiveRed, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onSetActive,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.background, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isActive) "ACTIVE" else "SELECT & PLAY", color = theme.background, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.textPrimary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("REFRESH", color = theme.textPrimary, fontSize = 12.sp)
            }

            OutlinedButton(onClick = onTest) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = theme.textPrimary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("TEST", color = theme.textPrimary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Provider", tint = ZaynLiveRed)
            }
        }
    }
}
