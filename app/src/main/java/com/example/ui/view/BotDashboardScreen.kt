package com.example.ui.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

    var activeTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Dashboard", "Console Logs", "Playground IA", "Configurar")

    // Primary modern dark background gradient to give a beautiful futuristic server style
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617)  // Slate 950
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BOT TELEGRAM IA",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8), // Cyan 400
                            letterSpacing = 1.5.sp,
                            fontSize = 20.sp
                        )
                        Text(
                            text = if (isRunning) "SERVIDOR ATIVO & ESCUTANDO" else "SERVIDOR DESCONECTADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = if (isRunning) Color(0xFF4ADE80) else Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) Color(0xFF4ADE80) else Color(0xFFEF4444))
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleServer() },
                        modifier = Modifier.testTag("server_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Parar Servidor" else "Iniciar Servidor",
                            tint = if (isRunning) Color(0xFFEF4444) else Color(0xFF4ADE80),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // High comfort standard M3 navigation aligned with edge-to-edge
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val selected = activeTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { activeTab = index },
                        label = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Info
                                    1 -> Icons.Default.List
                                    2 -> Icons.Default.Send
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF1E293B),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
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
                    2 -> PlaygroundTab(
                        viewModel = viewModel
                    )
                    3 -> SettingsTab(
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
                        onSaveSettings = { token, prompt, temp, apiKey, apiType, baseUrl, model ->
                            viewModel.updateConfig(token, prompt, temp, apiKey, apiType, baseUrl, model)
                        },
                        onVerifyToken = { token ->
                            viewModel.verifyBotTokenDirectly(token)
                        }
                    )
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
    onSaveSettings: (String, String, Float, String, String, String, String) -> Unit,
    onVerifyToken: (String) -> Unit
) {
    var token by remember(initialToken) { mutableStateOf(initialToken) }
    var systemPrompt by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    var temperature by remember(initialTemp) { mutableFloatStateOf(initialTemp) }
    var aiApiKey by remember(initialAiApiKey) { mutableStateOf(initialAiApiKey) }
    var aiApiType by remember(initialAiApiType) { mutableStateOf(initialAiApiType) }
    var aiBaseUrl by remember(initialAiBaseUrl) { mutableStateOf(initialAiBaseUrl) }
    var aiModel by remember(initialAiModel) { mutableStateOf(initialAiModel) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "CONFIGURAÇÕES DO MOTOR TELEGRAM IA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF38BDF8)
            )
        }

        // Credentials card
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
                        text = "CONFIGURAR TOKEN DO BOT TELEGRAM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Token do Bot") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_token_input"),
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

                    Button(
                        onClick = { onVerifyToken(token) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("settings_verify_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        if (verificationStatus == "VERIFYING") {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Validar Token", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
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
                            text = "CONFIGURAÇÃO DA INTELIGÊNCIA ARTIFICIAL",
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
                        listOf("GEMINI" to "Google Gemini ♊", "OPENAI" to "ChatGPT / Outros 🌐").forEach { (typePref, label) ->
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
                                text = if (aiApiType == "GEMINI") "Usar chave GEMINI_API_KEY do AI Studio (Padrão)" else "Digite seu token aqui (ex: sk-...)",
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (aiApiType == "GEMINI") {
                            "💡 Suporta qualquer gateway local, proxies, ou chaves Gemini pagas enviando ao endpoint compatível."
                        } else {
                            "💡 Compatível com qualquer endpoint no formato OpenAI Chat (ex: DeepSeek, Groq, local LM Studio, TogetherAI, OpenRouter, etc)."
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // AI Engine Config
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
                        text = "PROMPTS & COMPORTAMENTO (GEMINI)",
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
                            .height(140.dp)
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mais Preciso (0.2f)", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("Equilibrado (0.7f)", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("Mais Criativo (1.2f)", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        // Quick Save action
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
}
