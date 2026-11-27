package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalSynapseColors

@Composable
fun rememberSynapseHaptic(
    enabled: Boolean,
    intensity: String
): () -> Unit {
    val context = LocalContext.current
    return remember(enabled, intensity, context) {
        {
            if (enabled) {
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vm?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }
                    if (vibrator?.hasVibrator() == true) {
                        val (duration, amp) = when (intensity) {
                            "weak" -> 12L to 60
                            "strong" -> 28L to 220
                            else -> 18L to 130
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration, amp))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(duration)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}

@Composable
fun SynapseSurfaceCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    elevated: Boolean = false,
    accentBorder: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalSynapseColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val bgColor = if (elevated) colors.cardElevated else colors.cardSurface
    val borderBrush = if (accentBorder != null) {
        Brush.linearGradient(
            listOf(
                accentBorder.copy(alpha = 0.55f),
                accentBorder.copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                colors.borderStrong.copy(alpha = 0.65f),
                colors.borderSubtle.copy(alpha = 0.35f)
            )
        )
    }

    val cardModifier = modifier
        .clip(shape)
        .background(bgColor)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )

    Column(modifier = cardModifier, content = content)
}

@Composable
fun SynapseSectionHeader(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = LocalSynapseColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun SynapseStatusPill(
    isOnline: Boolean,
    onlineText: String = "Online",
    offlineText: String = "Offline",
    modifier: Modifier = Modifier
) {
    val colors = LocalSynapseColors.current
    val statusColor = if (isOnline) colors.success else colors.danger

    val pulseAlpha = if (isOnline && !colors.reduceMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "status_pill_pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_alpha"
        )
        alpha
    } else {
        1f
    }

    Surface(
        shape = CircleShape,
        color = statusColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.38f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = pulseAlpha))
            )
            Text(
                text = if (isOnline) onlineText else offlineText,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SynapseOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    readOnly: Boolean = false,
    accentColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colors = LocalSynapseColors.current
    val activeAccent = accentColor ?: colors.accent
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        label = label?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
        placeholder = placeholder?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = activeAccent,
            unfocusedBorderColor = colors.borderSubtle,
            focusedContainerColor = colors.surfaceLow,
            unfocusedContainerColor = colors.surfaceLow,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = activeAccent,
            focusedLabelColor = activeAccent,
            unfocusedLabelColor = colors.textMuted
        ),
        modifier = modifier
    )
}

@Composable
fun SynapseToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    accentColor: Color? = null
) {
    val colors = LocalSynapseColors.current
    val activeAccent = accentColor ?: colors.accent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(activeAccent.copy(alpha = if (checked) 0.16f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) activeAccent else colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                lineHeight = 16.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = activeAccent,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceLow,
                uncheckedBorderColor = colors.borderSubtle
            )
        )
    }
}

@Composable
fun <T> SynapseBottomSheetPicker(
    title: String,
    options: List<T>,
    selectedOption: T,
    optionLabel: (T) -> String,
    optionIcon: (T) -> String = { "✦" },
    onDismissRequest: () -> Unit,
    onOptionSelected: (T) -> Unit
) {
    val colors = LocalSynapseColors.current
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(colors.cardSurface)
                    .border(
                        width = 1.dp,
                        color = colors.borderStrong,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 18.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(colors.borderStrong)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.cardElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = option == selectedOption
                        Surface(
                            onClick = {
                                onOptionSelected(option)
                                onDismissRequest()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) colors.accent.copy(alpha = 0.14f) else colors.surfaceLow,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) colors.accent else colors.borderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 52.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(text = optionIcon(option), fontSize = 18.sp)
                                    Text(
                                        text = optionLabel(option),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isSelected) colors.accent else colors.textPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selecionado",
                                            tint = colors.onAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
