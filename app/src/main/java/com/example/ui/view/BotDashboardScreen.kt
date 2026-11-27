package com.example.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.SynapseStatusPill
import com.example.ui.components.rememberSynapseHaptic
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.BotViewModel
import com.example.ui.viewmodel.LogType
import kotlinx.coroutines.launch

data class NewsResource(val name: String, val url: String, val category: String)
data class SimulatedNews(val title: String, val desc: String, val url: String)

data class DrawerItemData(
    val index: Int,
    val title: String,
    val subText: String,
    val icon: ImageVector
)

data class WebhookQueryLog(
    val method: String,
    val code: String,
    val time: String,
    val body: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotDashboardScreen(
    viewModel: BotViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalSynapseColors.current
    val config by viewModel.configState.collectAsStateWithLifecycle()
    val isRunning by viewModel.isServerRunning.collectAsStateWithLifecycle()
    val consoleLogs by viewModel.consoleLogs.collectAsStateWithLifecycle()

    val totalMsgCount by viewModel.totalMessagesState.collectAsStateWithLifecycle()
    val aiResponseCount by viewModel.aiResponsesState.collectAsStateWithLifecycle()
    val distinctChatsCount by viewModel.distinctChatsState.collectAsStateWithLifecycle()

    val hapticsOn by viewModel.hapticsOn.collectAsStateWithLifecycle()
    val hapticIntensity by viewModel.hapticIntensity.collectAsStateWithLifecycle()
    val triggerHaptic = rememberSynapseHaptic(enabled = hapticsOn, intensity = hapticIntensity)

    var activeTab by remember { mutableIntStateOf(3) } // Default: Chat Playground
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen || activeTab != 3) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            activeTab = 3
        }
    }

    val menuOptions = remember {
        listOf(
            DrawerItemData(3, "Chat & Sandbox IA", "Conversa direta com o modelo", Icons.Rounded.ChatBubbleOutline),
            DrawerItemData(0, "Painel Geral", "Status do servidor e métricas", Icons.Rounded.SpaceDashboard),
            DrawerItemData(6, "Simulador de Entrevistas", "Treino técnico com relatório", Icons.Rounded.WorkOutline),
            DrawerItemData(2, "Navegador de Contexto", "Extração web e notícias", Icons.Rounded.Public),
            DrawerItemData(5, "Tradutor Multilíngue", "Tradução instantânea e TTS", Icons.Rounded.Translate),
            DrawerItemData(1, "Terminal de Logs", "Telemetria em tempo real", Icons.Rounded.Terminal),
            DrawerItemData(4, "Ajustes do Sistema", "IA, Telegram, temas e voz", Icons.Rounded.Tune)
        )
    }

    val bottomNavDestinations = remember {
        listOf(
            Triple(3, "Chat", Icons.Rounded.ChatBubbleOutline),
            Triple(0, "Painel", Icons.Rounded.SpaceDashboard),
            Triple(6, "Entrevistas", Icons.Rounded.WorkOutline),
            Triple(4, "Ajustes", Icons.Rounded.Tune)
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(colors.surfaceLow, colors.voidBackground)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.voidBackground,
                drawerContentColor = colors.textPrimary,
                modifier = Modifier.width(304.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.voidBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ai_core_orb_1795777695231),
                            contentDescription = "Synapse Console",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Synapse Console",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (isRunning) "Bot conectado • Polling ativo" else "Standby operacional",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isRunning) colors.success else colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = colors.borderSubtle, modifier = Modifier.padding(bottom = 14.dp))

                    menuOptions.forEach { option ->
                        val isSelected = activeTab == option.index
                        NavigationDrawerItem(
                            selected = isSelected,
                            onClick = {
                                triggerHaptic()
                                activeTab = option.index
                                scope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Column {
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = option.subText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) colors.accent.copy(alpha = 0.85f) else colors.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = colors.accent.copy(alpha = 0.14f),
                                selectedIconColor = colors.accent,
                                selectedTextColor = colors.accent,
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = colors.textMuted,
                                unselectedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    ) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = colors.voidBackground,
                        contentColor = colors.textPrimary,
                        header = {
                            IconButton(
                                onClick = {
                                    triggerHaptic()
                                    scope.launch { drawerState.open() }
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = "Abrir menu",
                                    tint = colors.textPrimary
                                )
                            }
                        }
                    ) {
                        menuOptions.forEach { item ->
                            val selected = activeTab == item.index
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    triggerHaptic()
                                    activeTab = item.index
                                },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = {
                                    Text(
                                        text = item.title.substringBefore(" "),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = colors.accent,
                                    selectedTextColor = colors.accent,
                                    indicatorColor = colors.accent.copy(alpha = 0.16f),
                                    unselectedIconColor = colors.textMuted,
                                    unselectedTextColor = colors.textMuted
                                )
                            )
                        }
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = when (activeTab) {
                                            0 -> "Painel Operacional"
                                            1 -> "Terminal de Logs"
                                            2 -> "Navegador de Contexto"
                                            3 -> "Synapse Chat"
                                            4 -> "Ajustes"
                                            5 -> "Tradutor IA"
                                            6 -> "Entrevistas IA"
                                            else -> "Synapse Console"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            navigationIcon = {
                                if (!isWideScreen) {
                                    IconButton(
                                        onClick = {
                                            triggerHaptic()
                                            scope.launch { drawerState.open() }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Menu,
                                            contentDescription = "Menu Lateral",
                                            tint = colors.textPrimary
                                        )
                                    }
                                }
                            },
                            actions = {
                                var showMenu by remember { mutableStateOf(false) }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    SynapseStatusPill(isOnline = isRunning)

                                    Box {
                                        IconButton(
                                            onClick = {
                                                triggerHaptic()
                                                showMenu = !showMenu
                                            },
                                            modifier = Modifier.testTag("three_dots_menu_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MoreVert,
                                                contentDescription = "Mais Opções",
                                                tint = colors.textPrimary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            modifier = Modifier.background(colors.cardSurface)
                                        ) {
                                            DropdownMenuItem(
                                                leadingIcon = { Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = colors.textSecondary) },
                                                text = { Text("Limpar Histórico", color = colors.textPrimary) },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.clearLocalHistory()
                                                }
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = { Icon(Icons.Rounded.Terminal, contentDescription = null, tint = colors.textSecondary) },
                                                text = { Text("Limpar Logs", color = colors.textPrimary) },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.clearLogs()
                                                }
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = { Icon(Icons.Rounded.Bolt, contentDescription = null, tint = colors.accent) },
                                                text = { Text("Simular Atividade", color = colors.textPrimary) },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.addLog("Rotina de diagnóstico executada pelo painel.", LogType.INFO)
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colors.voidBackground.copy(alpha = 0.85f),
                                titleContentColor = colors.textPrimary
                            )
                        )
                    },
                    bottomBar = {
                        if (!isWideScreen) {
                            NavigationBar(
                                containerColor = colors.voidBackground,
                                tonalElevation = 0.dp
                            ) {
                                bottomNavDestinations.forEach { (tabIndex, label, icon) ->
                                    val selected = activeTab == tabIndex
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            triggerHaptic()
                                            activeTab = tabIndex
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = colors.accent,
                                            selectedTextColor = colors.accent,
                                            indicatorColor = colors.accent.copy(alpha = 0.16f),
                                            unselectedIconColor = colors.textMuted,
                                            unselectedTextColor = colors.textMuted
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .background(backgroundBrush),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 840.dp)
                        ) {
                            AnimatedContent(
                                targetState = activeTab,
                                transitionSpec = {
                                    val dur = if (colors.reduceMotion) 0 else 220
                                    fadeIn(animationSpec = tween(dur)) togetherWith fadeOut(animationSpec = tween(dur))
                                },
                                label = "TabContentAnimation"
                            ) { targetTab ->
                                when (targetTab) {
                                    0 -> DashboardTab(
                                        isRunning = isRunning,
                                        botName = config?.botName ?: "Desconhecido",
                                        botUsername = config?.botUsername ?: "Vazio",
                                        token = config?.token ?: "",
                                        totalMsgCount = totalMsgCount,
                                        aiResponseCount = aiResponseCount,
                                        distinctChatsCount = distinctChatsCount,
                                        onToggleServer = { viewModel.toggleServer() },
                                        onNavigateTab = { activeTab = it },
                                        onHaptic = triggerHaptic
                                    )
                                    1 -> LogsTab(
                                        logs = consoleLogs,
                                        onClearLogs = { viewModel.clearLogs() },
                                        onHaptic = triggerHaptic
                                    )
                                    2 -> ContextBrowserTab(
                                        viewModel = viewModel,
                                        onHaptic = triggerHaptic
                                    )
                                    3 -> PlaygroundTab(
                                        viewModel = viewModel,
                                        onHaptic = triggerHaptic
                                    )
                                    4 -> SettingsTab(
                                        initialToken = config?.token ?: "",
                                        initialPrompt = config?.systemPrompt ?: "",
                                        initialTemp = config?.temperature ?: 0.7f,
                                        initialAiApiKey = config?.aiApiKey ?: "",
                                        initialAiApiType = config?.aiApiType ?: "GEMINI",
                                        initialAiBaseUrl = config?.aiBaseUrl ?: "https://generativelanguage.googleapis.com/",
                                        initialAiModel = config?.aiModel ?: "gemini-1.5-flash",
                                        verificationStatus = viewModel.botVerificationStatus.collectAsStateWithLifecycle().value,
                                        verifiedBotName = viewModel.verifiedBotName.collectAsStateWithLifecycle().value,
                                        verifiedBotUsername = viewModel.verifiedBotUsername.collectAsStateWithLifecycle().value,
                                        isAmoled = viewModel.isAmoled.collectAsStateWithLifecycle().value,
                                        accentColor = viewModel.accentColorHex.collectAsStateWithLifecycle().value,
                                        onSaveSettings = { token, prompt, temp, apiKey, apiType, baseUrl, model ->
                                            viewModel.updateConfig(token, prompt, temp, apiKey, apiType, baseUrl, model)
                                        },
                                        onVerifyToken = { token ->
                                            viewModel.verifyBotTokenDirectly(token)
                                        },
                                        onUpdateTheme = { amoled, accent ->
                                            viewModel.updateTheme(amoled, accent)
                                        },
                                        onClearWebhook = {
                                            viewModel.clearTelegramWebhook()
                                        },
                                        viewModel = viewModel,
                                        onHaptic = triggerHaptic
                                    )
                                    5 -> TranslatorTab(
                                        viewModel = viewModel,
                                        onHaptic = triggerHaptic
                                    )
                                    6 -> InterviewSimulatorTab(
                                        viewModel = viewModel,
                                        onHaptic = triggerHaptic
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
