package com.example.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SynapseOutlinedField
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.components.SynapseSurfaceCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSynapseColors
import kotlinx.coroutines.delay

fun LazyListScope.telegramIntegrationsSection(
    token: String,
    onTokenChange: (String) -> Unit,
    verificationStatus: String?,
    verifiedBotName: String?,
    verifiedBotUsername: String?,
    aiApiKey: String,
    onAiApiKeyChange: (String) -> Unit,
    aiApiType: String,
    onAiApiTypeChange: (String) -> Unit,
    aiBaseUrl: String,
    onAiBaseUrlChange: (String) -> Unit,
    aiModel: String,
    onAiModelChange: (String) -> Unit,
    webhookName: String,
    onWebhookNameChange: (String) -> Unit,
    webhookDescription: String,
    onWebhookDescriptionChange: (String) -> Unit,
    webhookUrl: String,
    onRegenerateWebhookUrl: () -> Unit,
    webhookIsOnline: Boolean,
    onWebhookOnlineChange: (Boolean) -> Unit,
    webhookLastRequestReceived: WebhookQueryLog?,
    webhookHistoryList: List<WebhookQueryLog>,
    isTestingWebhook: Boolean,
    onTestWebhook: () -> Unit,
    onVerifyToken: () -> Unit,
    onClearWebhook: () -> Unit,
    onSaveAll: () -> Unit,
    onHaptic: () -> Unit
) {
    item {
        SynapseSectionHeader(
            eyebrow = "CONECTORES",
            title = "Integrações e Automação",
            subtitle = "Gerencie o bot do Telegram, provedores de IA e gateways Webhook."
        )
    }

    // Telegram Bot Card
    item {
        val colors = LocalSynapseColors.current
        var isTokenVisible by remember { mutableStateOf(false) }

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    }
                    Text("Telegram Bot (@BotFather)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                }

                SynapseOutlinedField(
                    value = token,
                    onValueChange = onTokenChange,
                    label = "Token do Bot",
                    placeholder = "Cole o token gerado pelo @BotFather...",
                    visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                            Icon(
                                imageVector = if (isTokenVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Alternar visibilidade",
                                tint = colors.textMuted
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_token_input")
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceLow)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "ENDPOINT TELEGRAM",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (token.isNotEmpty()) "https://api.telegram.org/bot${token.take(12)}.../setWebhook" else "Configure um token primeiro",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = if (token.isNotEmpty()) colors.accent else colors.textMuted
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onHaptic()
                            onVerifyToken()
                        },
                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("settings_verify_btn")
                    ) {
                        if (verificationStatus == "VERIFYING") {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                        } else {
                            Icon(Icons.Rounded.Verified, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testar", color = colors.accent, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    if (token.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                onHaptic()
                                onClearWebhook()
                            },
                            border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Rounded.LinkOff, contentDescription = null, tint = colors.danger, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Limpar Webhook", color = colors.danger, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                if (verificationStatus != null) {
                    val statusTint = when (verificationStatus) {
                        "VALID" -> colors.success
                        "INVALID" -> colors.danger
                        else -> colors.accent
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusTint.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, statusTint.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = when (verificationStatus) {
                                    "VALID" -> "Bot Autenticado com Sucesso"
                                    "INVALID" -> "Falha na Autenticação do Telegram"
                                    else -> "Verificando token..."
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = statusTint
                            )
                            if (verificationStatus == "VALID" && verifiedBotName != null) {
                                Text("Nome: $verifiedBotName (@$verifiedBotUsername)", style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Provider Quick Override inside Integrations
    item {
        val colors = LocalSynapseColors.current
        var isApiKeyVisible by remember { mutableStateOf(false) }

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFA855F7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Psychology, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(19.dp))
                    }
                    Text("Cérebro IA do Bot", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GEMINI" to "Gemini", "OPENAI" to "OpenAI", "CUSTOM" to "Universal").forEach { (typePref, label) ->
                        val isSelected = aiApiType == typePref
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFA855F7).copy(alpha = 0.18f) else colors.surfaceLow,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFA855F7) else colors.borderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .clickable {
                                    onHaptic()
                                    onAiApiTypeChange(typePref)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) Color(0xFFC084FC) else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                SynapseOutlinedField(
                    value = aiApiKey,
                    onValueChange = onAiApiKeyChange,
                    label = "API Key",
                    placeholder = if (aiApiType == "GEMINI") "Cole sua Gemini API Key..." else "Cole sua API Key...",
                    accentColor = Color(0xFFA855F7),
                    visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                            Icon(
                                imageVector = if (isApiKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Alternar visibilidade",
                                tint = colors.textMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SynapseOutlinedField(
                        value = aiModel,
                        onValueChange = onAiModelChange,
                        label = "Modelo",
                        accentColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                    SynapseOutlinedField(
                        value = aiBaseUrl,
                        onValueChange = onAiBaseUrlChange,
                        label = "Endpoint",
                        accentColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }

    // Webhook Manager Card
    item {
        val colors = LocalSynapseColors.current
        val clipboardManager = LocalClipboardManager.current
        var copiedTextIndicator by remember { mutableStateOf(false) }

        LaunchedEffect(copiedTextIndicator) {
            if (copiedTextIndicator) {
                delay(2000)
                copiedTextIndicator = false
            }
        }

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Webhook Gateway", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text(
                            text = if (webhookIsOnline) "Ativo e escutando eventos" else "Gateway desativado",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (webhookIsOnline) colors.success else colors.danger
                        )
                    }
                    Switch(
                        checked = webhookIsOnline,
                        onCheckedChange = {
                            onHaptic()
                            onWebhookOnlineChange(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = colors.success
                        )
                    )
                }

                SynapseOutlinedField(
                    value = webhookName,
                    onValueChange = onWebhookNameChange,
                    label = "Nome do Webhook",
                    accentColor = colors.success,
                    modifier = Modifier.fillMaxWidth()
                )

                SynapseOutlinedField(
                    value = webhookDescription,
                    onValueChange = onWebhookDescriptionChange,
                    label = "Descrição (Opcional)",
                    accentColor = colors.success,
                    modifier = Modifier.fillMaxWidth()
                )

                SynapseOutlinedField(
                    value = webhookUrl,
                    onValueChange = {},
                    readOnly = true,
                    label = "URL do Webhook",
                    accentColor = colors.success,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onHaptic()
                            clipboardManager.setText(AnnotatedString(webhookUrl))
                            copiedTextIndicator = true
                        },
                        border = BorderStroke(1.dp, colors.borderStrong),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = if (copiedTextIndicator) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            tint = if (copiedTextIndicator) colors.success else colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copiedTextIndicator) "Copiado" else "Copiar URL",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (copiedTextIndicator) colors.success else colors.textPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onHaptic()
                            onRegenerateWebhookUrl()
                        },
                        border = BorderStroke(1.dp, colors.borderStrong),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Regenerar", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    }
                }

                Button(
                    onClick = {
                        onHaptic()
                        onTestWebhook()
                    },
                    enabled = !isTestingWebhook && webhookIsOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.success.copy(alpha = 0.16f),
                        contentColor = colors.success,
                        disabledContainerColor = colors.surfaceLow,
                        disabledContentColor = colors.textMuted
                    ),
                    border = BorderStroke(1.dp, if (webhookIsOnline) colors.success else colors.borderSubtle),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    if (isTestingWebhook) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.success)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando teste...", style = MaterialTheme.typography.labelLarge)
                    } else {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (webhookIsOnline) "Enviar Requisição Teste" else "Ative o Gateway para Testar",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (webhookLastRequestReceived != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.voidBackground)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BadgeTag(webhookLastRequestReceived.method, colors.success)
                                val ok = webhookLastRequestReceived.code.contains("200") || webhookLastRequestReceived.code.contains("201")
                                BadgeTag(webhookLastRequestReceived.code, if (ok) colors.success else colors.danger)
                            }
                            Text(webhookLastRequestReceived.time, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = colors.textMuted)
                        }
                        Text(
                            text = webhookLastRequestReceived.body,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = colors.textSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }

                if (webhookHistoryList.isNotEmpty()) {
                    Text("DISPAROS RECENTES", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    webhookHistoryList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceLow)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(item.method, fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.accent)
                                Text("/synapse-${webhookUrl.substringAfterLast("-")}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = colors.textMuted)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val ok = item.code.contains("200") || item.code.contains("201")
                                Text(item.code, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = if (ok) colors.success else colors.danger)
                                Text(item.time, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    // Future Smart Hooks Grid
    item {
        val colors = LocalSynapseColors.current
        val services = listOf(
            Triple("Discord Webhooks", "Alertas de canais", Color(0xFF8B5CF6)),
            Triple("WhatsApp API", "Notificações diretas", Color(0xFF10B981)),
            Triple("n8n Workflow", "Automação zero-code", Color(0xFFEF4444)),
            Triple("Make", "Cenários webhook", Color(0xFFEC4899)),
            Triple("Zapier", "Conector cloud", Color(0xFFF59E0B)),
            Triple("APIs REST", "Triggers JSON custom", Color(0xFF38BDF8))
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("CONECTORES SMART HOOKS", style = MaterialTheme.typography.labelSmall, color = colors.warning)
            services.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { (title, desc, tint) ->
                        SynapseSurfaceCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 14.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(tint))
                                    Text(title, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(6.dp))
                                BadgeTag("EM BREVE", tint)
                            }
                        }
                    }
                }
            }
        }
    }

    item {
        val colors = LocalSynapseColors.current
        Button(
            onClick = {
                onHaptic()
                onSaveAll()
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("settings_save_btn")
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salvar Integrações", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BadgeTag(text: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
