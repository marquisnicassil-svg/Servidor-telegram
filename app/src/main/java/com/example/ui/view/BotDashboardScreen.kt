package com.example.ui.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.database.BotMessageEntity
import com.example.ui.viewmodel.BotViewModel
import com.example.ui.viewmodel.ConsoleLogItem
import com.example.ui.viewmodel.LogType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DrawerItemData(
    val index: Int,
    val title: String,
    val subText: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotDashboardScreen(
    viewModel: BotViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.configState.collectAsStateWithLifecycle()
    val isRunning by viewModel.isServerRunning.collectAsStateWithLifecycle()
    val consoleLogs by viewModel.consoleLogs.collectAsStateWithLifecycle()
    
    // Stats flows
    val totalMsgCount by viewModel.totalMessagesState.collectAsStateWithLifecycle()
    val aiResponseCount by viewModel.aiResponsesState.collectAsStateWithLifecycle()
    val distinctChatsCount by viewModel.distinctChatsState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(3) } // Set default view to Playground / Chat as requested
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Primary modern dark background gradient to give a beautiful futuristic server style
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617)  // Slate 950
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF000000), // Super Black (#000000)
                drawerContentColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000))
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    // Drawer Header / Logo in elegant Slate
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "CORE SYSTEM",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Acesso & Painel de Controle",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 16.dp))

                    // Dynamic Drawer Navigation Links as requested
                    val menuOptions = listOf(
                        DrawerItemData(3, "💬 Novo Chat / Conversas", "Aba de Sandbox com IA", Icons.Default.Send),
                        DrawerItemData(4, "⚙️ Configurações (Ajustes)", "Chaves e Prompt do Sistema", Icons.Default.Settings),
                        DrawerItemData(1, "🖥️ Terminal de Comandos", "Registros e logs em tempo real", Icons.Default.List),
                        DrawerItemData(5, "🌐 Tradutor de Textos", "Mecanismo Tradutor IA", Icons.Default.Info),
                        DrawerItemData(0, "📊 Painel Geral / Dashboard", "Status operacional do bot", Icons.Default.Home),
                        DrawerItemData(2, "🌐 Navegador de Contexto", "Análise de Link & Notícia", Icons.Default.Info)
                    )

                    menuOptions.forEach { option ->
                        val isSelected = activeTab == option.index
                        Card(
                            onClick = {
                                activeTab = option.index
                                scope.launch { drawerState.close() }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E293B) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = option.title,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = option.subText,
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (activeTab) {
                                    0 -> "📊 PAINEL OPERACIONAL"
                                    1 -> "🖥️ TERMINAL DE COMANDOS"
                                    2 -> "🌐 NAVEGADOR DE CONTEXTO"
                                    3 -> "💬 NOVO CHAT / CONVERSAS"
                                    4 -> "⚙️ CONFIGURAÇÕES (AJUSTES)"
                                    5 -> "🌐 TRADUTOR DE TEXTOS"
                                    else -> "BOT TELEGRAM IA"
                                },
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Lateral",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isRunning) Color(0xFF4ADE80) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "Online" else "Offline",
                                color = if (isRunning) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A),
                        titleContentColor = Color.White
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundBrush)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
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
                        onToggleServer = { viewModel.toggleServer() }
                    )
                    1 -> LogsTab(
                        logs = consoleLogs,
                        onClearLogs = { viewModel.clearLogs() }
                    )
                    2 -> ContextBrowserTab(
                        viewModel = viewModel
                    )
                    3 -> PlaygroundTab(
                        viewModel = viewModel
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
                        }
                    )
                    5 -> TranslatorTab(viewModel = viewModel)
                }
            }
        }
    }
}
}

@Composable
fun DashboardTab(
    isRunning: Boolean,
    botName: String,
    botUsername: String,
    token: String,
    totalMsgCount: Int,
    aiResponseCount: Int,
    distinctChatsCount: Int,
    onToggleServer: () -> Unit
) {
    // Elegant pulsing animation for the live server bulb
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isRunning) 1.0f else 0.95f,
        targetValue = if (isRunning) 1.2f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Server Controller Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isRunning) Color(0x334ADE80) else Color(0x33EF4444),
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        ) {}
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) Color(0xFF4ADE80) else Color(0xFFEF4444))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isRunning) "PROVEDOR IA ATIVO" else "PROVEDOR IA PARADO",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isRunning) 
                            "Seu bot de IA está rodando em segundo plano no dispositivo. Vá ao Telegram para conversar com ele."
                            else "As atualizações estão paradas. O Telegram não receberá respostas automáticas do Gemini.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onToggleServer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color(0xFFEF4444) else Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .testTag("dashboard_server_action_btn")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "Parar Servidor" else "Ligar Servidor",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Selected Credentials Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IDENTIDADE DO BOT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Nome Exibido", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = if (botName.isNotEmpty() && botName != "Desconhecido") botName else "Não Autenticado",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Username", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = if (botUsername.isNotEmpty() && botUsername != "Vazio") "@$botUsername" else "-",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Token Ativo", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = if (token.length > 20) "${token.take(12)}...${token.takeLast(12)}" else "Token não configurado",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }
        }

        // Stats Panel (Horizontal Cards Carousel or Staggered List)
        item {
            Column {
                Text(
                    text = "MÉTRICAS DO SERVIDOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Leituras",
                        value = totalMsgCount.toString(),
                        subtitle = "Total logs",
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "IA Respostas",
                        value = aiResponseCount.toString(),
                        subtitle = "Entregues",
                        color = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Contatos",
                        value = distinctChatsCount.toString(),
                        subtitle = "Usuários",
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick verification warning for API Keys
        item {
            val keyToCheck = BuildConfig.GEMINI_API_KEY
            val isKeyMissing = keyToCheck.isEmpty() || keyToCheck == "MY_GEMINI_API_KEY"
            
            if (isKeyMissing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x55EF4444), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Aviso: GEMINI_API_KEY não foi configurada! Verifique o painel Secrets na barra lateral do AI Studio para que o robô possa responder.",
                            fontSize = 12.sp,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun LogsTab(
    logs: List<ConsoleLogItem>,
    onClearLogs: () -> Unit
) {
    val listState = rememberLazyListState()

    // Auto scroll terminal to the last log on changes
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.scrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TERMINAL DE LOGS DO SERVIDOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF38BDF8)
            )

            IconButton(
                onClick = onClearLogs,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Limpar Terminal",
                    tint = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF020617)) // Ultra dark console
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum log gerado ainda.\nA escuta das conversas aparecerá aqui em tempo real.",
                        color = Color(0xFF475569),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(logs, key = { it.id }) { logItem ->
                        val logColor = when (logItem.type) {
                            LogType.INFO -> Color(0xFF94A3B8)   // Slate gray
                            LogType.SUCCESS -> Color(0xFF4ADE80) // Mint Neon
                            LogType.WARNING -> Color(0xFFFBBF24)// Honey orange
                            LogType.ERROR -> Color(0xFFF87171)  // Electric red
                            LogType.TG_IN -> Color(0xFF38BDF8)  // Sky blue
                            LogType.TG_OUT -> Color(0xFFC084FC) // Lilac AI response
                            LogType.AI_SYS -> Color(0xFFF472B6) // AI Mindpink
                        }

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "[${logItem.formattedTime}] ",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = logItem.message,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = logColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaygroundTab(
    viewModel: BotViewModel
) {
    val messages by viewModel.localTestMessages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isAiThinkingLocal.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PLAYGROUND - SIMULADOR DO BOT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(bottom = 4.dp)
          )
        Text(
            text = "Interaja com a IA simulando comandos de chat localmente.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Sandbox terminal board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Envie uma mensagem abaixo para iniciar o teste local.\nO robô responderá usando suas configurações atuais de prompt.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { message ->
                        val isAI = message.isBotReply
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .wrapContentWidth(if (isAI) Alignment.Start else Alignment.End)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isAI) 2.dp else 12.dp,
                                            bottomEnd = if (isAI) 12.dp else 2.dp
                                        )
                                    )
                                    .background(
                                        if (isAI) Color(0xFF0F172A) else Color(0xFF38BDF8)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isAI) Color(0xFF1E293B) else Color.Transparent,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isAI) 2.dp else 12.dp,
                                            bottomEnd = if (isAI) 12.dp else 2.dp
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isAI) "IA Bot (Simulado)" else "Você (Teste)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isAI) Color(0xFF38BDF8) else Color(0xFF020617)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = message.messageText,
                                        fontSize = 14.sp,
                                        color = if (isAI) Color.White else Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }

                    if (isThinking) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "IA pensando...",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFF472B6)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Insira sua mensagem de teste...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("playground_msg_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    keyboardType = KeyboardType.Text
                )
            )

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendLocalMockMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .testTag("playground_send_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                enabled = !isThinking && inputText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (!isThinking && inputText.isNotBlank()) Color(0xFF020617) else Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(
            onClick = { viewModel.clearLocalHistory() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Limpar histórico de teste", color = Color(0xFFEF4444), fontSize = 12.sp)
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 22.sp
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                }
            }
            Text(
                text = ">",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun SettingsTab(
    initialToken: String,
    initialPrompt: String,
    initialTemp: Float,
    initialAiApiKey: String,
    initialAiApiType: String,
    initialAiBaseUrl: String,
    initialAiModel: String,
    verificationStatus: String?, // "VERIFYING", "VALID", "INVALID"
    verifiedBotName: String?,
    verifiedBotUsername: String?,
    isAmoled: Boolean,
    accentColor: String,
    onSaveSettings: (String, String, Float, String, String, String, String) -> Unit,
    onVerifyToken: (String) -> Unit,
    onUpdateTheme: (Boolean, String) -> Unit
) {
    var token by remember(initialToken) { mutableStateOf(initialToken) }
    var systemPrompt by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    var temperature by remember(initialTemp) { mutableFloatStateOf(initialTemp) }
    var aiApiKey by remember(initialAiApiKey) { mutableStateOf(initialAiApiKey) }
    var aiApiType by remember(initialAiApiType) { mutableStateOf(initialAiApiType) }
    var aiBaseUrl by remember(initialAiBaseUrl) { mutableStateOf(initialAiBaseUrl) }
    var aiModel by remember(initialAiModel) { mutableStateOf(initialAiModel) }

    // Navigation subview state: null means Index List
    var activeSettingsView by remember { mutableStateOf<String?>(null) } // "appearance", "ai", "database", "account"

    // Mock states for Supabase settings tab
    var supabaseUrl by remember { mutableStateOf("https://hqnlyksuprjdabcgpqdl.supabase.co") }
    var supabaseKey by remember { mutableStateOf("sb_publishable_VNDxq3Hxp1Ly5tFF7BMBuQ_imQDmVSa") }
    var supabaseTable by remember { mutableStateOf("chat_backups") }
    var supabaseAuto by remember { mutableStateOf(false) }
    var testConnectionStatus by remember { mutableStateOf("") } // "", "TESTING", "SUCCESS", "ERROR"
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeSettingsView == null) {
            // Main Index List Header
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "⚙️ AJUSTES DO SISTEMA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gerencie recursos visuais, inteligência e dados de nuvem",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Options Index List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsMenuItem(
                        icon = "🎨",
                        title = "Aparência e Temas",
                        subtitle = "Modo AMOLED, cores de destaque e customização visual",
                        onClick = { activeSettingsView = "appearance" }
                    )
                    SettingsMenuItem(
                        icon = "🤖",
                        title = "Conexão Bot do Telegram",
                        subtitle = "Insira o token do BotFather e configure a integração do robô",
                        onClick = { activeSettingsView = "telegram" }
                    )
                    SettingsMenuItem(
                        icon = "🧠",
                        title = "Motor de Inteligência Artificial",
                        subtitle = "Gerencie provedores como Gemini, OpenAI, Groq, chaves de API e prompts",
                        onClick = { activeSettingsView = "ai" }
                    )
                    SettingsMenuItem(
                        icon = "☁️",
                        title = "Conexão e Banco de Dados",
                        subtitle = "Configurações do Supabase, URLs do projeto, tabelas e backups",
                        onClick = { activeSettingsView = "database" }
                    )
                    SettingsMenuItem(
                        icon = "👤",
                        title = "Minha Conta & Sessão",
                        subtitle = "Dados do usuário conectado, chaves de acesso e gerenciamento de login",
                        onClick = { activeSettingsView = "account" }
                    )
                }
            }
        } else {
            // BACK BUTTON AT THE TOP OF EVEY SUBVIEW
            item {
                Button(
                    onClick = { activeSettingsView = null },
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⬅️ Voltar para Ajustes", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (activeSettingsView) {
                "appearance" -> {
                    item {
                        Text(
                            text = "🎨 APARÊNCIA E TEMAS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    // AMOLED Switch Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Fundo Super Black (AMOLED)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Preto puro para economizar bateria",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Switch(
                                    checked = isAmoled,
                                    onCheckedChange = { onUpdateTheme(it, accentColor) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF38BDF8),
                                        checkedTrackColor = Color(0x6638BDF8)
                                    )
                                )
                            }
                        }
                    }

                    // Color Accent Picker Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Cor de Destaque Nativa",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val colorsList = listOf(
                                    "#FFFFFF" to "Branco",
                                    "#10B981" to "Esmeralda",
                                    "#F59E0B" to "Âmbar",
                                    "#EF4444" to "Carmesim",
                                    "#8B5CF6" to "Roxo"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    colorsList.forEach { (hexCode, label) ->
                                        val rgbColor = try { Color(android.graphics.Color.parseColor(hexCode)) } catch(e: Exception) { Color.White }
                                        val isSelected = accentColor.equals(hexCode, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(rgbColor)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                                                    shape = CircleShape
                                                )
                                                .clickable { onUpdateTheme(isAmoled, hexCode) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Informative design tip
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x05FFFFFF)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "💡 Dica de Aparência",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Combine qualquer Cor de Destaque com o modo AMOLED ativo para um visual minimalista de altíssimo contraste, ou desative o AMOLED para carregar tons de cinza-slate clássico.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                "ai" -> {
                    item {
                        Text(
                            text = "🧠 MOTOR DE INTELIGÊNCIA ARTIFICIAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    // Universal AI Provider Config
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CONFIGURAÇÃO DE MODELOS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Tipo de Provedor / API:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("GEMINI" to "Gemini ♊", "OPENAI" to "ChatGPT 🌐").forEach { (typePref, label) ->
                                        val isSelected = aiApiType == typePref
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0x2238BDF8) else Color(0xFF0F172A))
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    aiApiType = typePref
                                                    if (typePref == "GEMINI") {
                                                        aiBaseUrl = "https://generativelanguage.googleapis.com/"
                                                        aiModel = "gemini-1.5-flash"
                                                    } else {
                                                        aiBaseUrl = "https://api.openai.com/v1/"
                                                        aiModel = "gpt-4o"
                                                    }
                                                }
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // API Key
                                Text(
                                    text = if (aiApiType == "GEMINI") "Chave de API do Gemini (AI Key):" else "Chave de API / Token do Provedor:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = aiApiKey,
                                    onValueChange = { aiApiKey = it },
                                    placeholder = {
                                        Text(
                                            text = if (aiApiType == "GEMINI") "Usar chave GEMINI_API_KEY do AI Studio" else "Digite seu token aqui (sk-...)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_ai_api_key_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // API Base URL
                                Text(
                                    text = "URL Base da API (Endpoint Base):",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = aiBaseUrl,
                                    onValueChange = { aiBaseUrl = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_ai_base_url_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Model Name
                                Text(
                                    text = "Nome do Modelo:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = aiModel,
                                    onValueChange = { aiModel = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_ai_model_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // AI Engine Config (Prompts & Creativity)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "PROMPTS & CRIATIVIDADE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Instruções do Sistema (System Directive):",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = systemPrompt,
                                    onValueChange = { systemPrompt = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .testTag("settings_prompt_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Criatividade (Temperature):",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = String.format("%.2f", temperature),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp
                                    )
                                }

                                Slider(
                                    value = temperature,
                                    onValueChange = { temperature = it },
                                    valueRange = 0.1f..1.5f,
                                    steps = 14,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF38BDF8),
                                        activeTrackColor = Color(0xFF38BDF8),
                                        inactiveTrackColor = Color(0xFF334155)
                                    )
                                )
                            }
                        }
                    }

                    // Save settings action
                    item {
                        Button(
                            onClick = { onSaveSettings(token, systemPrompt, temperature, aiApiKey, aiApiType, aiBaseUrl, aiModel) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("settings_save_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF020617))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar Configurações", fontWeight = FontWeight.Black, color = Color(0xFF020617))
                        }
                    }
                }

                "database" -> {
                    item {
                        Text(
                            text = "☁️ CONEXÃO E BANCO DE DADOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    // Supabase configuration fields
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "CONFIGURAR CONEXÃO NUVEM",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )

                                OutlinedTextField(
                                    value = supabaseUrl,
                                    onValueChange = { supabaseUrl = it },
                                    label = { Text("URL do Projeto Supabase") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = supabaseTable,
                                    onValueChange = { supabaseTable = it },
                                    label = { Text("Nome da Tabela de Backups") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = supabaseKey,
                                    onValueChange = { supabaseKey = it },
                                    label = { Text("Chave Pública (Anon Key)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Auto-Salvar Mensagens na Nuvem", fontSize = 12.sp, color = Color.White)
                                    Switch(
                                        checked = supabaseAuto,
                                        onCheckedChange = { supabaseAuto = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFF38BDF8),
                                            checkedTrackColor = Color(0x6638BDF8)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Connection operations buttons (Mocked)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                testConnectionStatus = "TESTING"
                                                delay(1000)
                                                testConnectionStatus = "SUCCESS"
                                                delay(2000)
                                                testConnectionStatus = ""
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Text(
                                            if (testConnectionStatus == "TESTING") "Conectando..." else "🔌 Testar",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                testConnectionStatus = "BACKUP_RUNNING"
                                                delay(1200)
                                                testConnectionStatus = "BACKUP_OK"
                                                delay(2000)
                                                testConnectionStatus = ""
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Text(
                                            if (testConnectionStatus == "BACKUP_RUNNING") "Enviando..." else "📤 Backup",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4ADE80),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (testConnectionStatus.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x2238BDF8))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (testConnectionStatus) {
                                                "TESTING" -> "🔌 Ping do servidor ativo: Testando integridade..."
                                                "SUCCESS" -> "✓ Canal de sincronização operacional!"
                                                "BACKUP_RUNNING" -> "📤 Compactando base de testes e gerando backup..."
                                                "BACKUP_OK" -> "✓ Sincronização e backup concluídos!"
                                                else -> testConnectionStatus
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "telegram" -> {
                    item {
                        Text(
                            text = "🤖 CONEXÃO BOT DO TELEGRAM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    item {
                        var isTokenVisible by remember { mutableStateOf(false) }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF000000)), // Super Black (#000000)
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🤖 Token do Bot (Telegram)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = token,
                                    onValueChange = { token = it },
                                    placeholder = { Text("Insira o token gerado pelo @BotFather...", color = Color(0xFF475569)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_token_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    visualTransformation = if (isTokenVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                            Text(if (isTokenVisible) "🙈" else "👁️", fontSize = 16.sp)
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Insira o token gerado pelo @BotFather para ativar o Moreno no Telegram.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { onVerifyToken(token) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("settings_verify_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    if (verificationStatus == "VERIFYING") {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF38BDF8))
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Text("🔄 Testar Conexão do Bot", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                // Bot Credentials Indicator Card inside Token Config
                                if (verificationStatus != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (verificationStatus) {
                                                    "VALID" -> Color(0x334ADE80)
                                                    "INVALID" -> Color(0x33EF4444)
                                                    else -> Color(0x331E293B)
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when (verificationStatus) {
                                                    "VALID" -> Color(0xFF4ADE80)
                                                    "INVALID" -> Color(0xFFEF4444)
                                                    else -> Color(0xFF334155)
                                                },
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = when (verificationStatus) {
                                                    "VALID" -> "✓ Bot Autenticado com Sucesso"
                                                    "INVALID" -> "✗ Erro na Autenticação do Telegram"
                                                    else -> "Verificando token ativo..."
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = when (verificationStatus) {
                                                    "VALID" -> Color(0xFF4ADE80)
                                                    "INVALID" -> Color(0xFFEF4444)
                                                    else -> Color.White
                                                }
                                            )
                                            if (verificationStatus == "VALID" && verifiedBotName != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Nome: $verifiedBotName", fontSize = 11.sp, color = Color.White)
                                                Text("Username: @$verifiedBotUsername", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Save settings action
                    item {
                        Button(
                            onClick = { onSaveSettings(token, systemPrompt, temperature, aiApiKey, aiApiType, aiBaseUrl, aiModel) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("settings_save_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF020617))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar Token", fontWeight = FontWeight.Black, color = Color(0xFF020617))
                        }
                    }
                }

                "account" -> {
                    item {
                        Text(
                            text = "👤 MINHA CONTA & SESSÃO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🔑 Supabase Auth",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Você não está autenticado. Para sincronizar o sistema e fazer backup de mensagens na nuvem, realize o login de segurança no portal inicial.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { /* Simulated auth check */ },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🔑 Sessão Segura Ativa", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranslatorTab(
    viewModel: BotViewModel
) {
    val translatedText by viewModel.translatedText.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Inglês") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val languages = listOf("Inglês", "Espanhol", "Francês", "Alemão", "Italiano", "Japonês", "Mandarim", "Português")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "TRADUTOR IA MULTILÍNGUE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Traduza textos instantaneamente utilizando o mecanismo de inteligência artificial.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SELECIONE O IDIOMA DE DESTINO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { dropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Traduzir para: $selectedLanguage", color = Color.White, fontSize = 14.sp)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                        }
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155))
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang, color = Color.White) },
                                onClick = {
                                    selectedLanguage = lang
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Insira o texto para traduzir...", color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.translateText(inputText, selectedLanguage)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    enabled = !isTranslating && inputText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF020617))
                    } else {
                        Text("Realizar Tradução ✨", fontWeight = FontWeight.Bold, color = Color(0xFF020617))
                    }
                }
            }
        }

        if (translatedText.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TEXTO TRADUZIDO ($selectedLanguage)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = translatedText,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(context, "🔊 Reproduzindo áudio...", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🔊 Ouvir", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}

@Composable
fun ContextBrowserTab(
    viewModel: BotViewModel
) {
    val contextUrl by viewModel.contextUrl.collectAsStateWithLifecycle()
    val extractedStats by viewModel.extractedStats.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isContextProcessing.collectAsStateWithLifecycle()

    var urlInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("context_browser_container")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Navegador de Contexto",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Extração de conteúdo web inteligente",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Insira uma URL abaixo. O sistema irá realizar o download da página, limpar anúncios, menus e tags desnecessárias, e alimentar o Moreno IA em tempo real com o texto principal limpo com foco em tecnologia e futebol.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Actions & Entry Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "URL PARA EXTRAÇÃO",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("https://exemplo.com/noticia-tecnologia-ou-futebol", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("context_url_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { viewModel.processContextUrl(urlInput, chatId = 999999L) },
                        enabled = urlInput.isNotBlank() && !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF1E293B),
                            disabledContentColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("analyze_url_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Extrair e Enviar para o Moreno IA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Stats card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "STATUS DO HISTÓRICO DE EXTRAÇÃO",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Última URL extraída:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            text = contextUrl.ifBlank { "Nenhuma" },
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tamanho extraído:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            text = extractedStats,
                            color = Color(0xFF4ADE80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Foco do Mecanismo:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            text = "Tecnologia & Futebol 🚀⚽",
                            color = Color(0xFFFBBF24),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Demo Shortcuts
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "URLS DEMONSTRATIVAS Rápidas",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { urlInput = "https://ge.globo.com/futebol/brasileirao-serie-a/noticia/ia-brasileirao" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Futebol ⚽", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { urlInput = "https://tecnundo.com.br/tecnologia/mecanismo-ia-local" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Tecnologia 🚀", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
