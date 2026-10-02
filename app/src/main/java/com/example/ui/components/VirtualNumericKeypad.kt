package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ZaynBorder
import com.example.ui.theme.ZaynCardBackground
import com.example.ui.theme.ZaynCyanPrimary
import com.example.ui.theme.ZaynDeepBackground
import com.example.ui.theme.ZaynFocusBorder
import com.example.ui.theme.ZaynTextPrimary

@Composable
fun VirtualNumericKeypad(
    input: String,
    onDigitPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ZaynDeepBackground.copy(alpha = 0.95f))
            .border(1.5.dp, ZaynBorder, RoundedCornerShape(20.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CHANNEL",
                color = ZaynCyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Channel Input Display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ZaynCardBackground)
                    .border(1.dp, ZaynBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .width(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (input.isEmpty()) "CH ---" else "CH $input",
                    color = ZaynTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("0", "BACKSPACE", "OK")
            )

            rows.forEach { rowKeys ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    rowKeys.forEach { key ->
                        KeypadButton(
                            label = key,
                            onClick = {
                                when (key) {
                                    "BACKSPACE" -> onBackspace()
                                    "OK" -> onConfirm()
                                    else -> onDigitPress(key)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isConfirm = label == "OK"
    val isBack = label == "BACKSPACE"

    val bgColor = when {
        isFocused -> ZaynCyanPrimary
        isConfirm -> ZaynCyanPrimary.copy(alpha = 0.2f)
        else -> ZaynCardBackground
    }

    val contentColor = when {
        isFocused -> ZaynDeepBackground
        isConfirm -> ZaynCyanPrimary
        else -> ZaynTextPrimary
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) ZaynFocusBorder else ZaynBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        } else if (isConfirm) {
            Text(
                text = "OK",
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        } else {
            Text(
                text = label,
                color = contentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
