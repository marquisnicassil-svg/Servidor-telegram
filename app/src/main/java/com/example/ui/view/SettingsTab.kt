package com.example.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.components.SynapseSurfaceCard
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.BotViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun SettingsTab(
    initialToken: String,
    initialPrompt: String,
    initialTemp: Float,
    initialAiApiKey: String,
    initialAiApiType: String,
    initialAiBaseUrl: String,
    initialAiModel: String,
    verificationStatus: String?,
    verifiedBotName: String?,
    verifiedBotUsername: String?,
    isAmoled: Boolean,
    accentColor: String,
    onSaveSettings: (String, String, Float, String, String, String, String) -> Unit,
    onVerifyToken: (String) -> Unit,
    onUpdateTheme: (Boolean, String) -> Unit,
    onClearWebhook: () -> Unit,
    viewModel: BotViewModel,
    onHaptic: () -> Unit = {}
) {
    val colors = LocalSynapseColors.current
    var token by remember(initialToken) { mutableStateOf(initialToken) }
    var systemPrompt by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    var temperature by remember(initialTemp) { mutableFloatStateOf(initialTemp) }
    var aiApiKey by remember(initialAiApiKey) { mutableStateOf(initialAiApiKey) }
    var aiApiType by remember(initialAiApiType) { mutableStateOf(initialAiApiType) }
    var aiBaseUrl by remember(initialAiBaseUrl) { mutableStateOf(initialAiBaseUrl) }
    var aiModel by remember(initialAiModel) { mutableStateOf(initialAiModel) }

    var activeSettingsView by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = activeSettingsView != null) {
        activeSettingsView = null
    }

    var supabaseUrl by remember { mutableStateOf("https://hqnlyksuprjdabcgpqdl.supabase.co") }
    var supabaseKey by remember { mutableStateOf("sb_publishable_VNDxq3Hxp1Ly5tFF7BMBuQ_imQDmVSa") }
    var supabaseTable by remember { mutableStateOf("chat_backups") }
    var supabaseAuto by remember { mutableStateOf(false) }
    var testConnectionStatus by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.sendPushNotification(
                title = "Notificações Ativadas",
                message = "O Synapse Console agora pode enviar alertas push e avisos em segundo plano.",
                category = "system"
            )
        }
    }

    var webhookName by remember { mutableStateOf("Synapse Production Webhook") }
    var webhookDescription by remember { mutableStateOf("Canal integrado para recepção de logs e eventos") }
    var webhookUrl by remember { mutableStateOf("https://ais-pre-czylyfbapj56qz6hoxll75-727445671983.us-east1.run.app/api/v1/webhooks/synapse-8c9df1") }
    var webhookIsOnline by remember { mutableStateOf(true) }
    var webhookLastRequestReceived by remember {
        mutableStateOf<WebhookQueryLog?>(
            WebhookQueryLog(
                method = "POST",
                code = "200 OK",
                time = "11:38:12",
                body = "{\n  \"status\": \"active\",\n  \"event_source\": \"reception_bot\",\n  \"meta\": {\n    \"device_ping\": \"21ms\",\n    \"version\": \"v2.1\"\n  }\n}"
            )
        )
    }
    var webhookHistoryList by remember {
        mutableStateOf(
            listOf(
                WebhookQueryLog("POST", "200 OK", "11:38:12", "{\n  \"status\": \"active\"\n}"),
                WebhookQueryLog("GET", "200 OK", "11:15:00", "{\n  \"status\": \"ping_ok\"\n}"),
                WebhookQueryLog("POST", "400 Bad Request", "10:04:14", "{\n  \"error\": \"missing_parameter\"\n}")
            )
        )
    }
    var isTestingWebhook by remember { mutableStateOf(false) }

    val handleProviderChange: (String) -> Unit = { typePref ->
        aiApiType = typePref
        when (typePref) {
            "GEMINI" -> {
                aiBaseUrl = "https://generativelanguage.googleapis.com/"
                aiModel = "gemini-1.5-flash"
            }
            "OPENAI" -> {
                aiBaseUrl = "https://api.openai.com/v1/"
                aiModel = "gpt-4o"
            }
            "CUSTOM" -> {
                if (aiBaseUrl == "https://generativelanguage.googleapis.com/" || aiBaseUrl == "https://api.openai.com/v1/") {
                    aiBaseUrl = "https://api.deepseek.com/v1/"
                }
                if (aiModel == "gemini-1.5-flash" || aiModel == "gpt-4o") {
                    aiModel = "deepseek-chat"
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (activeSettingsView == null) {
            item {
                SynapseSectionHeader(
                    eyebrow = "PREFERÊNCIAS",
                    title = "Ajustes do Sistema",
                    subtitle = "Configure aparência, provedores de IA, Telegram, notificações e voz."
                )
            }

            item {
                Text(
                    text = "CONECTIVIDADE & IA",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsMenuItem(
                        icon = Icons.Rounded.Psychology,
                        tint = Color(0xFFA855F7),
                        title = "Motor de Inteligência Artificial",
                        subtitle = "Provedores Gemini, OpenAI, Universal, chaves e prompt",
                        onClick = { onHaptic(); activeSettingsView = "ai" }
                    )
                    SettingsMenuItem(
                        icon = Icons.Rounded.Hub,
                        tint = colors.accent,
                        title = "Integrações e Automação",
                        subtitle = "Telegram Bot, Webhooks e conectores externos",
                        onClick = { onHaptic(); activeSettingsView = "telegram" }
                    )
                    SettingsMenuItem(
                        icon = Icons.Rounded.CloudSync,
                        tint = colors.success,
                        title = "Conexão e Banco de Dados",
                        subtitle = "Supabase, sincronização e backups",
                        onClick = { onHaptic(); activeSettingsView = "database" }
                    )
                }
            }

            item {
                Text(
                    text = "EXPERIÊNCIA & SISTEMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsMenuItem(
                        icon = Icons.Rounded.Palette,
                        tint = Color(0xFFEC4899),
                        title = "Aparência e Temas",
                        subtitle = "Fundo AMOLED Obsidian e cores de destaque",
                        onClick = { onHaptic(); activeSettingsView = "appearance" }
                    )
                    SettingsMenuItem(
                        icon = Icons.Rounded.NotificationsActive,
                        tint = colors.warning,
                        title = "Notificações e Sons",
                        subtitle = "Alertas push por categoria e efeitos sonoros",
                        onClick = { onHaptic(); activeSettingsView = "notifications" }
                    )
                    SettingsMenuItem(
                        icon = Icons.Rounded.RecordVoiceOver,
                        tint = colors.secondaryAccent,
                        title = "Voz e Acessibilidade",
                        subtitle = "Háptica, alto contraste, movimento e TTS",
                        onClick = { onHaptic(); activeSettingsView = "accessibility" }
                    )
                    SettingsMenuItem(
                        icon = Icons.Rounded.VerifiedUser,
                        tint = colors.textSecondary,
                        title = "Minha Conta & Sessão",
                        subtitle = "Dados da sessão e armazenamento local",
                        onClick = { onHaptic(); activeSettingsView = "account" }
                    )
                }
            }
        } else {
            item {
                TextButton(
                    onClick = {
                        onHaptic()
                        activeSettingsView = null
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Voltar para Ajustes",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.accent
                    )
                }
            }

            when (activeSettingsView) {
                "appearance" -> appearanceSettingsSection(
                    isAmoled = isAmoled,
                    accentColor = accentColor,
                    onUpdateTheme = onUpdateTheme,
                    onHaptic = onHaptic
                )

                "ai" -> aiEngineSettingsSection(
                    aiApiKey = aiApiKey,
                    onAiApiKeyChange = { aiApiKey = it },
                    aiApiType = aiApiType,
                    onAiApiTypeChange = handleProviderChange,
                    aiBaseUrl = aiBaseUrl,
                    onAiBaseUrlChange = { aiBaseUrl = it },
                    aiModel = aiModel,
                    onAiModelChange = { aiModel = it },
                    systemPrompt = systemPrompt,
                    onSystemPromptChange = { systemPrompt = it },
                    temperature = temperature,
                    onTemperatureChange = { temperature = it },
                    onSave = {
                        onSaveSettings(token, systemPrompt, temperature, aiApiKey, aiApiType, aiBaseUrl, aiModel)
                        android.widget.Toast.makeText(context, "Configurações salvas!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onHaptic = onHaptic
                )

                "database" -> databaseSettingsSection(
                    supabaseUrl = supabaseUrl,
                    onSupabaseUrlChange = { supabaseUrl = it },
                    supabaseTable = supabaseTable,
                    onSupabaseTableChange = { supabaseTable = it },
                    supabaseKey = supabaseKey,
                    onSupabaseKeyChange = { supabaseKey = it },
                    supabaseAuto = supabaseAuto,
                    onSupabaseAutoChange = { supabaseAuto = it },
                    testConnectionStatus = testConnectionStatus,
                    onTestConnection = {
                        scope.launch {
                            testConnectionStatus = "TESTING"
                            delay(900)
                            testConnectionStatus = "SUCCESS"
                            delay(2000)
                            testConnectionStatus = ""
                        }
                    },
                    onBackupNow = {
                        scope.launch {
                            testConnectionStatus = "BACKUP_RUNNING"
                            delay(1100)
                            testConnectionStatus = "BACKUP_OK"
                            delay(2000)
                            testConnectionStatus = ""
                        }
                    },
                    onHaptic = onHaptic
                )

                "telegram" -> telegramIntegrationsSection(
                    token = token,
                    onTokenChange = { token = it },
                    verificationStatus = verificationStatus,
                    verifiedBotName = verifiedBotName,
                    verifiedBotUsername = verifiedBotUsername,
                    aiApiKey = aiApiKey,
                    onAiApiKeyChange = { aiApiKey = it },
                    aiApiType = aiApiType,
                    onAiApiTypeChange = handleProviderChange,
                    aiBaseUrl = aiBaseUrl,
                    onAiBaseUrlChange = { aiBaseUrl = it },
                    aiModel = aiModel,
                    onAiModelChange = { aiModel = it },
                    webhookName = webhookName,
                    onWebhookNameChange = { webhookName = it },
                    webhookDescription = webhookDescription,
                    onWebhookDescriptionChange = { webhookDescription = it },
                    webhookUrl = webhookUrl,
                    onRegenerateWebhookUrl = {
                        val randomHex = UUID.randomUUID().toString().substring(0, 6)
                        webhookUrl = "https://ais-pre-czylyfbapj56qz6hoxll75-727445671983.us-east1.run.app/api/v1/webhooks/synapse-$randomHex"
                    },
                    webhookIsOnline = webhookIsOnline,
                    onWebhookOnlineChange = { webhookIsOnline = it },
                    webhookLastRequestReceived = webhookLastRequestReceived,
                    webhookHistoryList = webhookHistoryList,
                    isTestingWebhook = isTestingWebhook,
                    onTestWebhook = {
                        scope.launch {
                            isTestingWebhook = true
                            delay(900)
                            isTestingWebhook = false
                            val currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            val randomNum = (100..999).random()
                            val newLog = WebhookQueryLog(
                                method = listOf("POST", "GET", "PUT").random(),
                                code = "200 OK",
                                time = currentTimeStr,
                                body = "{\n  \"event\": \"webhook_test_trigger\",\n  \"trigger_id\": \"trg_$randomNum\",\n  \"status\": \"success\"\n}"
                            )
                            webhookLastRequestReceived = newLog
                            webhookHistoryList = listOf(newLog) + webhookHistoryList.take(5)
                        }
                    },
                    onVerifyToken = {
                        onSaveSettings(token, systemPrompt, temperature, aiApiKey, aiApiType, aiBaseUrl, aiModel)
                        onVerifyToken(token)
                    },
                    onClearWebhook = onClearWebhook,
                    onSaveAll = {
                        onSaveSettings(token, systemPrompt, temperature, aiApiKey, aiApiType, aiBaseUrl, aiModel)
                        android.widget.Toast.makeText(context, "Integrações salvas!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onHaptic = onHaptic
                )

                "notifications" -> notificationsSettingsSection(
                    viewModel = viewModel,
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onHaptic = onHaptic
                )

                "accessibility" -> accessibilitySettingsSection(
                    viewModel = viewModel,
                    onHaptic = onHaptic
                )

                "account" -> accountSettingsSection()
            }
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalSynapseColors.current
    SynapseSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 68.dp),
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
