package com.example.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.DeveloperConfig
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.theme.ZaynBorder
import com.example.ui.theme.ZaynCardBackground
import com.example.ui.theme.ZaynCyanPrimary
import com.example.ui.theme.ZaynDeepBackground
import com.example.ui.theme.ZaynFocusBorder
import com.example.ui.theme.ZaynTextMuted
import com.example.ui.theme.ZaynTextPrimary
import com.example.ui.theme.ZaynTextSecondary

@Composable
fun AboutScreen(
    viewModel: ZaynTvViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            viewModel.showToast("Unable to open browser")
        }
    }

    val openEmail: (String) -> Unit = { email ->
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            viewModel.showToast("No email app found")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZaynDeepBackground)
            .padding(20.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.navigateTo(CurrentScreen.SETTINGS) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ZaynCyanPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ABOUT ${DeveloperConfig.APP_NAME}",
                color = ZaynTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                // Logo & App Name Header
                Image(
                    painter = painterResource(id = R.drawable.ic_zayntv_logo),
                    contentDescription = "ZaynTV Official Logo",
                    modifier = Modifier
                        .height(76.dp)
                        .padding(bottom = 6.dp)
                )

                Text(
                    text = "Version ${DeveloperConfig.APP_VERSION}",
                    color = ZaynTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Developer Credentials Card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(ZaynCardBackground)
                        .border(1.dp, ZaynBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Developed by",
                            color = ZaynTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DeveloperConfig.DEVELOPER_NAME,
                            color = ZaynTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = DeveloperConfig.DEVELOPER_HANDLE,
                            color = ZaynCyanPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Connect with Developer Section
                Text(
                    text = "Connect with Developer",
                    color = ZaynCyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Social Grid (2 columns)
                val socialButtons = listOf(
                    "Facebook" to DeveloperConfig.FACEBOOK_URL,
                    "Instagram" to DeveloperConfig.INSTAGRAM_URL,
                    "X / Twitter" to DeveloperConfig.X_URL,
                    "YouTube" to DeveloperConfig.YOUTUBE_URL,
                    "Telegram" to DeveloperConfig.TELEGRAM_URL,
                    "GitHub" to DeveloperConfig.GITHUB_URL
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    socialButtons.chunked(2).forEach { rowPair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowPair.forEach { (label, url) ->
                                SocialButton(
                                    label = label,
                                    onClick = { openUrl(url) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Email Section (Hidden if empty as requested)
                if (DeveloperConfig.DEVELOPER_EMAIL.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Email",
                        color = ZaynCyanPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SocialButton(
                        label = DeveloperConfig.DEVELOPER_EMAIL,
                        onClick = { openEmail(DeveloperConfig.DEVELOPER_EMAIL) },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                HorizontalDivider(color = ZaynBorder, thickness = 1.dp, modifier = Modifier.fillMaxWidth(0.9f))
                Spacer(modifier = Modifier.height(20.dp))

                // Legal & Open Source Notices
                Text(
                    text = "ZaynTV is a TV-first IPTV player. All IPTV streams are supplied by the user. ZaynTV does not bundle or distribute any copyrighted media content.",
                    color = ZaynTextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Built with Android Jetpack, Jetpack Compose & AndroidX Media3 ExoPlayer",
                    color = ZaynTextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SocialButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) ZaynCyanPrimary else ZaynCardBackground
    val contentColor = if (isFocused) ZaynDeepBackground else ZaynTextPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) ZaynFocusBorder else ZaynBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
