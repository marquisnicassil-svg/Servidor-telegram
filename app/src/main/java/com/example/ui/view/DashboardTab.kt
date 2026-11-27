package com.example.ui.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.components.SynapseStatusPill
import com.example.ui.components.SynapseSurfaceCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSynapseColors

@Composable
fun DashboardTab(
    isRunning: Boolean,
    botName: String,
    botUsername: String,
    token: String,
    totalMsgCount: Int,
    aiResponseCount: Int,
    distinctChatsCount: Int,
    onToggleServer: () -> Unit,
    onNavigateTab: (Int) -> Unit = {},
    onHaptic: () -> Unit = {}
) {
    val colors = LocalSynapseColors.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val pulseScale = if (!colors.reduceMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "server_orb_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = if (isRunning) 0.96f else 0.98f,
            targetValue = if (isRunning) 1.18f else 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
        scale
    } else 1f

    val statusColor = if (isRunning) colors.success else colors.danger

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Server Control Card with Atmospheric Art
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.cardSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                statusColor.copy(alpha = 0.55f),
                                colors.borderSubtle.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_synapse_hero_1795777695323),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.32f)
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colors.voidBackground.copy(alpha = 0.35f),
                                    colors.cardSurface.copy(alpha = 0.92f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYNAPSE ENGINE",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.accent,
                            fontWeight = FontWeight.Bold
                        )
                        SynapseStatusPill(
                            isOnline = isRunning,
                            onlineText = "ATIVO",
                            offlineText = "PARADO"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            statusColor.copy(alpha = 0.36f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.voidBackground.copy(alpha = 0.85f))
                                .border(2.dp, statusColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Rounded.Bolt else Icons.Rounded.PowerSettingsNew,
                                contentDescription = if (isRunning) "Servidor Online" else "Servidor Offline",
                                tint = statusColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isRunning) "Servidor Telegram Operacional" else "Servidor em Standby",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isRunning)
                            "Escuta ativa via Long Polling com respostas automáticas de IA em segundo plano."
                        else
                            "Polling pausado. Ligue o servidor para processar mensagens recebidas no Telegram.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onHaptic()
                            onToggleServer()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) colors.danger else colors.accent,
                            contentColor = if (isRunning) Color.White else colors.onAccent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("dashboard_server_action_btn")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Rounded.StopCircle else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "Parar Servidor" else "Ligar Servidor",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SynapseSectionHeader(
                    eyebrow = "TELEMETRIA",
                    title = "Métricas em Tempo Real"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricTile(
                        title = "Leituras",
                        value = totalMsgCount.toString(),
                        subtitle = "Mensagens",
                        icon = Icons.Rounded.MarkChatRead,
                        accent = colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "IA Respostas",
                        value = aiResponseCount.toString(),
                        subtitle = "Entregues",
                        icon = Icons.Rounded.AutoAwesome,
                        accent = colors.success,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "Contatos",
                        value = distinctChatsCount.toString(),
                        subtitle = "Ativos",
                        icon = Icons.Rounded.Group,
                        accent = colors.warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SynapseSectionHeader(
                    eyebrow = "ATALHOS",
                    title = "Acesso Rápido"
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionTile(
                        label = "Chat IA",
                        icon = Icons.Rounded.ChatBubbleOutline,
                        tint = colors.accent,
                        onClick = { onHaptic(); onNavigateTab(3) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "Terminal",
                        icon = Icons.Rounded.Terminal,
                        tint = colors.success,
                        onClick = { onHaptic(); onNavigateTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "Entrevistas",
                        icon = Icons.Rounded.WorkOutline,
                        tint = colors.secondaryAccent,
                        onClick = { onHaptic(); onNavigateTab(6) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "Ajustes",
                        icon = Icons.Rounded.Tune,
                        tint = colors.warning,
                        onClick = { onHaptic(); onNavigateTab(4) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bot Identity Card
        item {
            SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_ai_core_orb_1795777695231),
                                contentDescription = "Avatar do Bot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            )
                            Column {
                                Text(
                                    text = "IDENTIDADE DO BOT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.accent
                                )
                                Text(
                                    text = if (botName.isNotEmpty() && botName != "Desconhecido") botName else "Não Autenticado",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        if (botUsername.isNotEmpty() && botUsername != "Vazio") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.accent.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "@$botUsername",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.accent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.borderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Token Telegram Ativo",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (token.length > 20) "${token.take(10)}••••••••${token.takeLast(8)}" else "Token não configurado",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (token.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onHaptic()
                                    clipboardManager.setText(AnnotatedString(token))
                                    android.widget.Toast.makeText(context, "Token copiado", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copiar token",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // API Key Alert Banner (if missing)
        item {
            val keyToCheck = BuildConfig.GEMINI_API_KEY
            val isKeyMissing = keyToCheck.isEmpty() || keyToCheck == "MY_GEMINI_API_KEY"
            if (isKeyMissing) {
                SynapseSurfaceCard(
                    accentBorder = colors.warning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.warning.copy(alpha = 0.08f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.warning.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = "Alerta de chave",
                                tint = colors.warning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chave Gemini não detectada no BuildConfig",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.warning
                            )
                            Text(
                                text = "Configure GEMINI_API_KEY em Secrets ou informe uma chave personalizada em Ajustes > Motor de IA.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalSynapseColors.current
    SynapseSurfaceCard(
        modifier = modifier,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSynapseColors.current
    SynapseSurfaceCard(
        modifier = modifier.defaultMinSize(minHeight = 76.dp),
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
