package com.example.ui.view

import android.Manifest
import android.os.Build
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SynapseOutlinedField
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.components.SynapseSurfaceCard
import com.example.ui.components.SynapseToggleRow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.BotViewModel
import java.util.Locale

fun LazyListScope.appearanceSettingsSection(
    isAmoled: Boolean,
    accentColor: String,
    onUpdateTheme: (Boolean, String) -> Unit,
    onHaptic: () -> Unit
) {
    item {
        SynapseSectionHeader(
            eyebrow = "PERSONALIZAÇÃO",
            title = "Aparência e Temas",
            subtitle = "Ajuste o contraste do fundo e a cor de destaque em todo o console."
        )
    }

    item {
        val colors = LocalSynapseColors.current
        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                SynapseToggleRow(
                    title = "Fundo Obsidian AMOLED",
                    description = "Preto profundo para maior contraste e economia de bateria em telas OLED",
                    checked = isAmoled,
                    onCheckedChange = {
                        onHaptic()
                        onUpdateTheme(it, accentColor)
                    },
                    icon = Icons.Rounded.DarkMode
                )
            }
        }
    }

    item {
        val colors = LocalSynapseColors.current
        val palette = listOf(
            "#38BDF8" to "Ciano",
            "#818CF8" to "Índigo",
            "#A855F7" to "Violeta",
            "#10B981" to "Esmeralda",
            "#F59E0B" to "Âmbar",
            "#EF4444" to "Coral"
        )

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Cor de Destaque", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    palette.forEach { (hex, label) ->
                        val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { colors.accent }
                        val isSelected = accentColor.equals(hex, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else colors.borderSubtle,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onHaptic()
                                        onUpdateTheme(isAmoled, hex)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "$label selecionado",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) colors.accent else colors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

fun LazyListScope.aiEngineSettingsSection(
    aiApiKey: String,
    onAiApiKeyChange: (String) -> Unit,
    aiApiType: String,
    onAiApiTypeChange: (String) -> Unit,
    aiBaseUrl: String,
    onAiBaseUrlChange: (String) -> Unit,
    aiModel: String,
    onAiModelChange: (String) -> Unit,
    systemPrompt: String,
    onSystemPromptChange: (String) -> Unit,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    onSave: () -> Unit,
    onHaptic: () -> Unit
) {
    item {
        SynapseSectionHeader(
            eyebrow = "INTELIGÊNCIA",
            title = "Motor de IA",
            subtitle = "Provedor de linguagem, diretriz do sistema e criatividade."
        )
    }

    item {
        val colors = LocalSynapseColors.current
        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Provedor Ativo", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GEMINI" to "Gemini", "OPENAI" to "ChatGPT", "CUSTOM" to "Universal").forEach { (typePref, label) ->
                        val isSelected = aiApiType == typePref
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.accent.copy(alpha = 0.16f) else colors.surfaceLow,
                            border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 46.dp)
                                .clickable {
                                    onHaptic()
                                    onAiApiTypeChange(typePref)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) colors.accent else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                SynapseOutlinedField(
                    value = aiApiKey,
                    onValueChange = onAiApiKeyChange,
                    label = "Chave de API",
                    placeholder = if (aiApiType == "GEMINI") "Usa Secrets GEMINI_API_KEY se vazio" else "Digite seu token (sk-...)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_ai_api_key_input")
                )

                SynapseOutlinedField(
                    value = aiBaseUrl,
                    onValueChange = onAiBaseUrlChange,
                    label = "URL Base (Endpoint)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_ai_base_url_input")
                )

                SynapseOutlinedField(
                    value = aiModel,
                    onValueChange = onAiModelChange,
                    label = "Modelo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_ai_model_input")
                )
            }
        }
    }

    item {
        val colors = LocalSynapseColors.current
        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Comportamento & Prompt", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)

                SynapseOutlinedField(
                    value = systemPrompt,
                    onValueChange = onSystemPromptChange,
                    label = "System Prompt",
                    singleLine = false,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 128.dp)
                        .testTag("settings_prompt_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Criatividade (Temperature)", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    Text(
                        text = String.format(Locale.US, "%.2f", temperature),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 13.sp
                    )
                }

                Slider(
                    value = temperature,
                    onValueChange = onTemperatureChange,
                    valueRange = 0.1f..1.5f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accent,
                        activeTrackColor = colors.accent,
                        inactiveTrackColor = colors.borderSubtle
                    )
                )
            }
        }
    }

    item {
        val colors = LocalSynapseColors.current
        Button(
            onClick = {
                onHaptic()
                onSave()
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
            Text("Salvar Configurações de IA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

fun LazyListScope.databaseSettingsSection(
    supabaseUrl: String,
    onSupabaseUrlChange: (String) -> Unit,
    supabaseTable: String,
    onSupabaseTableChange: (String) -> Unit,
    supabaseKey: String,
    onSupabaseKeyChange: (String) -> Unit,
    supabaseAuto: Boolean,
    onSupabaseAutoChange: (Boolean) -> Unit,
    testConnectionStatus: String,
    onTestConnection: () -> Unit,
    onBackupNow: () -> Unit,
    onHaptic: () -> Unit
) {
    item {
        SynapseSectionHeader(
            eyebrow = "SINCRONIZAÇÃO",
            title = "Conexão e Banco de Dados",
            subtitle = "Configuração de nuvem Supabase e backup de histórico."
        )
    }

    item {
        val colors = LocalSynapseColors.current
        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SynapseOutlinedField(
                    value = supabaseUrl,
                    onValueChange = onSupabaseUrlChange,
                    label = "URL do Projeto Supabase",
                    modifier = Modifier.fillMaxWidth()
                )
                SynapseOutlinedField(
                    value = supabaseTable,
                    onValueChange = onSupabaseTableChange,
                    label = "Tabela de Backups",
                    modifier = Modifier.fillMaxWidth()
                )
                SynapseOutlinedField(
                    value = supabaseKey,
                    onValueChange = onSupabaseKeyChange,
                    label = "Chave Pública (Anon Key)",
                    modifier = Modifier.fillMaxWidth()
                )
                SynapseToggleRow(
                    title = "Auto-Backup na Nuvem",
                    description = "Sincronizar mensagens automaticamente após cada resposta",
                    checked = supabaseAuto,
                    onCheckedChange = {
                        onHaptic()
                        onSupabaseAutoChange(it)
                    },
                    icon = Icons.Rounded.CloudSync
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onHaptic(); onTestConnection() },
                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text(
                            text = if (testConnectionStatus == "TESTING") "Conectando..." else "Testar Conexão",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.accent
                        )
                    }
                    OutlinedButton(
                        onClick = { onHaptic(); onBackupNow() },
                        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text(
                            text = if (testConnectionStatus == "BACKUP_RUNNING") "Enviando..." else "Gerar Backup",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.success
                        )
                    }
                }

                if (testConnectionStatus.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.accent.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (testConnectionStatus) {
                                "TESTING" -> "Verificando integridade da conexão..."
                                "SUCCESS" -> "Canal de sincronização operacional!"
                                "BACKUP_RUNNING" -> "Compactando registros e enviando backup..."
                                "BACKUP_OK" -> "Sincronização e backup concluídos!"
                                else -> testConnectionStatus
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.accent,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

fun LazyListScope.notificationsSettingsSection(
    viewModel: BotViewModel,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onHaptic: () -> Unit
) {
    item {
        SynapseSectionHeader(
            eyebrow = "ALERTAS",
            title = "Notificações e Sons",
            subtitle = "Controle notificações push e feedback sonoro."
        )
    }

    item {
        val colors = LocalSynapseColors.current
        val notifSystem by viewModel.notifSystemOn.collectAsStateWithLifecycle()
        val notifMessages by viewModel.notifMessagesOn.collectAsStateWithLifecycle()
        val notifUpdates by viewModel.notifUpdatesOn.collectAsStateWithLifecycle()
        val notifIntegrations by viewModel.notifIntegrationsOn.collectAsStateWithLifecycle()
        val notifSecurity by viewModel.notifSecurityOn.collectAsStateWithLifecycle()
        val silentMode by viewModel.notifSilentMode.collectAsStateWithLifecycle()
        val soundEffects by viewModel.soundEffectsOn.collectAsStateWithLifecycle()

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SynapseToggleRow(
                    title = "Notificações Gerais",
                    description = "Alertas do sistema, inicialização e status",
                    checked = notifSystem,
                    onCheckedChange = { onHaptic(); viewModel.updateNotificationSetting("notif_system_on", it) },
                    icon = Icons.Rounded.NotificationsActive
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Mensagens & Chat",
                    description = "Respostas recebidas no Telegram e Playground",
                    checked = notifMessages,
                    onCheckedChange = { onHaptic(); viewModel.updateNotificationSetting("notif_messages_on", it) },
                    icon = Icons.Rounded.MarkChatUnread
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Atualizações do Sistema",
                    description = "Sincronização de cache e novidades",
                    checked = notifUpdates,
                    onCheckedChange = { onHaptic(); viewModel.updateNotificationSetting("notif_updates_on", it) },
                    icon = Icons.Rounded.SystemUpdate
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Integrações & Webhooks",
                    description = "Eventos de webhook, Telegram e automações",
                    checked = notifIntegrations,
                    onCheckedChange = { onHaptic(); viewModel.updateNotificationSetting("notif_integrations_on", it) },
                    icon = Icons.Rounded.Hub
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Segurança",
                    description = "Avisos de autenticação e chaves",
                    checked = notifSecurity,
                    onCheckedChange = { onHaptic(); viewModel.updateNotificationSetting("notif_security_on", it) },
                    icon = Icons.Rounded.Shield
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Modo Silencioso",
                    description = "Pausa todos os alertas visuais e sonoros",
                    checked = silentMode,
                    onCheckedChange = { onHaptic(); viewModel.updateNotificationSetting("notif_silent_mode", it) },
                    icon = Icons.Rounded.NotificationsOff,
                    accentColor = colors.warning
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Efeitos Sonoros",
                    description = "Feedback sonoro ao interagir e receber alertas",
                    checked = soundEffects,
                    onCheckedChange = { onHaptic(); viewModel.updateNotificationSetting("sound_effects_on", it) },
                    icon = Icons.Rounded.GraphicEq
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    Button(
                        onClick = { onHaptic(); onRequestPermission() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.warning, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Autorizar Notificações no Android", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        onHaptic()
                        viewModel.sendPushNotification(
                            title = "Synapse Console Ativo",
                            message = "Notificações push nativas operacionais no seu dispositivo.",
                            category = "system"
                        )
                    },
                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Notificação de Teste", style = MaterialTheme.typography.labelLarge, color = colors.accent)
                }
            }
        }
    }
}

fun LazyListScope.accessibilitySettingsSection(
    viewModel: BotViewModel,
    onHaptic: () -> Unit
) {
    item {
        SynapseSectionHeader(
            eyebrow = "ACESSIBILIDADE & VOZ",
            title = "Voz, Háptica e Leitura",
            subtitle = "Personalize vibração, alto contraste, movimento reduzido e síntese TTS."
        )
    }

    item {
        val colors = LocalSynapseColors.current
        val hapticsOn by viewModel.hapticsOn.collectAsStateWithLifecycle()
        val hapticIntensity by viewModel.hapticIntensity.collectAsStateWithLifecycle()

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SynapseToggleRow(
                    title = "Resposta Háptica",
                    description = "Feedback tátil sutil ao tocar em ações e menus",
                    checked = hapticsOn,
                    onCheckedChange = { viewModel.updateAccessibilitySetting("haptics_on", it) },
                    icon = Icons.Rounded.Vibration
                )

                if (hapticsOn) {
                    HorizontalDivider(color = colors.borderSubtle)
                    Text("Intensidade da Vibração", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("weak" to "Fraca", "medium" to "Média", "strong" to "Forte").forEach { (key, label) ->
                            val isSelected = hapticIntensity == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) colors.accent else colors.surfaceLow,
                                border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 42.dp)
                                    .clickable {
                                        viewModel.updateAccessibilitySetting("haptic_intensity", key)
                                        viewModel.playSound("click")
                                        onHaptic()
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) colors.onAccent else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    item {
        val colors = LocalSynapseColors.current
        val highContrastOn by viewModel.highContrastOn.collectAsStateWithLifecycle()
        val reduceMotionOn by viewModel.reduceMotionOn.collectAsStateWithLifecycle()
        val readHoverOn by viewModel.readHoverOn.collectAsStateWithLifecycle()

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SynapseToggleRow(
                    title = "Modo Alto Contraste",
                    description = "Bordas reforçadas e preto absoluto para máxima nitidez",
                    checked = highContrastOn,
                    onCheckedChange = { onHaptic(); viewModel.updateAccessibilitySetting("high_contrast_on", it) },
                    icon = Icons.Rounded.Contrast
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Reduzir Movimento",
                    description = "Desativa animações pulsantes e transições rápidas",
                    checked = reduceMotionOn,
                    onCheckedChange = { onHaptic(); viewModel.updateAccessibilitySetting("reduce_motion_on", it) },
                    icon = Icons.Rounded.MotionPhotosOff
                )
                HorizontalDivider(color = colors.borderSubtle)
                SynapseToggleRow(
                    title = "Narração de Elementos",
                    description = "Leitura assistida para itens selecionados",
                    checked = readHoverOn,
                    onCheckedChange = { onHaptic(); viewModel.updateAccessibilitySetting("read_hover_on", it) },
                    icon = Icons.Rounded.RecordVoiceOver
                )
            }
        }
    }

    item {
        val colors = LocalSynapseColors.current
        val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
        val speechPitch by viewModel.speechPitch.collectAsStateWithLifecycle()
        val speechVoiceGender by viewModel.speechVoiceGender.collectAsStateWithLifecycle()

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Síntese de Voz (TTS)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("female" to "Feminina", "male" to "Masculina", "neutral" to "Neutra").forEach { (key, label) ->
                        val isSelected = speechVoiceGender == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.accent else colors.surfaceLow,
                            border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 42.dp)
                                .clickable {
                                    onHaptic()
                                    viewModel.updateAccessibilitySetting("speech_voice_gender", key)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) colors.onAccent else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Velocidade de Leitura", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1fx", speechRate),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
                Slider(
                    value = speechRate,
                    onValueChange = { viewModel.updateAccessibilitySetting("speech_rate", it) },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tom de Voz (Pitch)", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1f", speechPitch),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
                Slider(
                    value = speechPitch,
                    onValueChange = { viewModel.updateAccessibilitySetting("speech_pitch", it) },
                    valueRange = 0.5f..1.5f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
                )

                OutlinedButton(
                    onClick = {
                        onHaptic()
                        viewModel.speakText("Olá! Este é um teste da voz do Synapse Console no Android.")
                    },
                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Testar Voz Agora", style = MaterialTheme.typography.labelLarge, color = colors.accent)
                }
            }
        }
    }
}

fun LazyListScope.accountSettingsSection() {
    item {
        SynapseSectionHeader(
            eyebrow = "CONTA",
            title = "Minha Conta & Sessão",
            subtitle = "Gerenciamento de sessão local e sincronização."
        )
    }

    item {
        val colors = LocalSynapseColors.current
        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text("Sessão Local Protegida", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text("Armazenamento criptografado no dispositivo", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    }
                }
                Text(
                    text = "Suas chaves, histórico de conversas e avaliações de entrevista são armazenados localmente via Room Database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}
