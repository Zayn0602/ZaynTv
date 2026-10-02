package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TvOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.model.AspectRatioMode
import com.example.data.model.Channel
import com.example.data.model.WatermarkPosition
import com.example.data.model.WatermarkSize
import com.example.player.PlaybackState
import com.example.player.ZaynPlayerManager
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynLiveRed

@OptIn(UnstableApi::class)
@Composable
fun TvPlayerView(
    playerManager: ZaynPlayerManager,
    playbackState: PlaybackState,
    currentChannel: Channel?,
    aspectRatio: AspectRatioMode,
    watermarkEnabled: Boolean = true,
    watermarkPosition: WatermarkPosition = WatermarkPosition.TOP_RIGHT,
    watermarkOpacity: Float = 0.40f,
    watermarkSize: WatermarkSize = WatermarkSize.SMALL,
    onRetry: () -> Unit,
    onNextChannel: () -> Unit,
    onOpenChannelList: () -> Unit,
    onTapPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    val resizeMode = remember(aspectRatio) { playerManager.getResizeMode(aspectRatio) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTapPlayer
            )
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    keepScreenOn = true
                    this.resizeMode = resizeMode
                    player = playerManager.getPlayer()
                }
            },
            update = { view ->
                view.resizeMode = resizeMode
                view.keepScreenOn = true
                val currentPlayer = playerManager.getPlayer()
                if (view.player != currentPlayer) {
                    view.player = currentPlayer
                }
            },
            onRelease = { view ->
                view.player = null
            },
            modifier = Modifier.fillMaxSize()
        )

        // ZaynTV DTH Channel Watermark (Requirement 3: Bottom-Right broadcast branding)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_zayntv_brand_logo),
                contentDescription = "ZaynTV Watermark",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = 84.dp, height = 26.dp)
                    .alpha(0.55f)
            )
        }

        // Buffering Indicator
        if (playbackState is PlaybackState.Buffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = theme.primary,
                        modifier = Modifier.size(52.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Loading ${currentChannel?.displayName ?: "Channel"}...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Fallback in progress indicator
        if (playbackState is PlaybackState.FallbackAttempt) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x77000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = theme.secondary,
                        modifier = Modifier.size(46.dp),
                        strokeWidth = 3.5.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = playbackState.message,
                        color = theme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Playback Error Screen
        if (playbackState is PlaybackState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF0070A10))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, theme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TvOff,
                        contentDescription = "Playback issue",
                        tint = ZaynLiveRed,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Unable to play this channel",
                        color = theme.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentChannel?.let { "${it.formattedNumber}  ${it.displayName}" } ?: "Live Stream",
                        color = theme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playbackState.message,
                        color = theme.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.background)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RETRY", color = theme.background, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onNextChannel,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.background)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, tint = theme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("NEXT CHANNEL", color = theme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(onClick = onOpenChannelList) {
                            Text("CHANNEL LIST", color = theme.textPrimary)
                        }
                    }
                }
            }
        }
    }
}
