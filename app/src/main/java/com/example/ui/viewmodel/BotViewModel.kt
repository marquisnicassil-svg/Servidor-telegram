package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.BotDatabase
import com.example.data.database.BotConfigEntity
import com.example.data.database.BotMessageEntity
import com.example.data.repository.BotRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogType {
    INFO, SUCCESS, WARNING, ERROR, TG_IN, TG_OUT, AI_SYS
}

data class ConsoleLogItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val type: LogType,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

class BotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BotRepository
    
    // --- Database Flows for UI ---
    val configState: StateFlow<BotConfigEntity?>
    val messagesState: StateFlow<List<BotMessageEntity>>
    val distinctChatsState: StateFlow<Int>
    val totalMessagesState: StateFlow<Int>
    val aiResponsesState: StateFlow<Int>

    // --- UI state variables ---
    private val _consoleLogs = MutableStateFlow<List<ConsoleLogItem>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogItem>> = _consoleLogs.asStateFlow()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _botVerificationStatus = MutableStateFlow<String?>(null) // null = idle, "VERIFYING", "VALID", "INVALID"
    val botVerificationStatus: StateFlow<String?> = _botVerificationStatus.asStateFlow()

    private val _verifiedBotName = MutableStateFlow<String?>(null)
    val verifiedBotName: StateFlow<String?> = _verifiedBotName.asStateFlow()

    private val _verifiedBotUsername = MutableStateFlow<String?>(null)
    val verifiedBotUsername: StateFlow<String?> = _verifiedBotUsername.asStateFlow()

    // --- Local Test Chat State ---
    private val _localTestMessages = MutableStateFlow<List<BotMessageEntity>>(emptyList())
    val localTestMessages: StateFlow<List<BotMessageEntity>> = _localTestMessages.asStateFlow()

    private val _isAiThinkingLocal = MutableStateFlow(false)
    val isAiThinkingLocal: StateFlow<Boolean> = _isAiThinkingLocal.asStateFlow()

    // --- Polling Job ---
    private var pollingJob: Job? = null
    private var lastUpdateId: Long? = null

    init {
        val database = BotDatabase.getDatabase(application)
        repository = BotRepository(database.botDao())

        // Setup db observations
        configState = repository.configFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        messagesState = repository.allMessagesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        distinctChatsState = repository.distinctChatsCountFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        totalMessagesState = repository.messagesCountFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        aiResponsesState = repository.aiResponsesCountFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        // Seed initial configuration if database has nothing
        viewModelScope.launch {
            val currentConfig = repository.getConfig()
            if (currentConfig == null) {
                val initialConfig = BotConfigEntity(
                    token = "8664441309:AAFVXRIDBu0nCDhLNhGf729XM2Yzq5ianEs",
                    systemPrompt = "Você é o assistente virtual do Telegram, alimentado por inteligência artificial (Gemini). Responda sempre em português brasileiro de forma educada, prestativa e inteligente. Mantenha as mensagens organizadas e legíveis.",
                    temperature = 0.7f,
                    isActive = true
                )
                repository.saveConfig(initialConfig)
                addLog("Configuração inicial do Bot criada e carregada com sucesso.", LogType.SUCCESS)
                startBotServer(initialConfig)
            } else {
                addLog("Configurações anteriores recuperadas do banco de dados.", LogType.INFO)
                // Force active state to true to establish the connection directly on launch as requested
                val activeConfig = currentConfig.copy(isActive = true)
                repository.saveConfig(activeConfig)
                startBotServer(activeConfig)
            }
        }

        // Listen for internal message flow changes to keep local test chat synchronized
        viewModelScope.launch {
            repository.allMessagesFlow.collect { list ->
                // Filter messages belonging to local emulator chatId: 999999
                _localTestMessages.value = list.filter { it.chatId == 999999L }.sortedBy { it.timestamp }
            }
        }
    }

    fun addLog(msg: String, type: LogType) {
        val item = ConsoleLogItem(message = msg, type = type)
        _consoleLogs.value = (_consoleLogs.value + item).takeLast(150) // keep last 150 entries
        Log.d("BotConsole", "[${type.name}] $msg")
    }

    fun clearLogs() {
        _consoleLogs.value = emptyList()
    }

    // --- Server Control Actions ---
    fun toggleServer() {
        viewModelScope.launch {
            val config = configState.value ?: return@launch
            val nextState = !config.isActive
            
            val updatedConfig = config.copy(isActive = nextState)
            repository.saveConfig(updatedConfig)

            if (nextState) {
                startBotServer(updatedConfig)
            } else {
                stopBotServer()
            }
        }
    }

    fun updateConfig(
        token: String,
        systemPrompt: String,
        temperature: Float,
        aiApiKey: String,
        aiApiType: String,
        aiBaseUrl: String,
        aiModel: String
    ) {
        viewModelScope.launch {
            val config = configState.value
            val isCurrentlyRunning = config?.isActive ?: false
            
            if (isCurrentlyRunning) {
                stopBotServer()
            }

            // Save update preserving other database states
            val newConfig = config?.copy(
                token = token,
                systemPrompt = systemPrompt,
                temperature = temperature,
                isActive = isCurrentlyRunning,
                aiApiKey = aiApiKey,
                aiApiType = aiApiType,
                aiBaseUrl = aiBaseUrl,
                aiModel = aiModel
            ) ?: BotConfigEntity(
                token = token,
                systemPrompt = systemPrompt,
                temperature = temperature,
                isActive = isCurrentlyRunning,
                aiApiKey = aiApiKey,
                aiApiType = aiApiType,
                aiBaseUrl = aiBaseUrl,
                aiModel = aiModel
            )
            repository.saveConfig(newConfig)
            addLog("Configurações do Servidor e da IA salvas com sucesso.", LogType.SUCCESS)

            if (isCurrentlyRunning) {
                startBotServer(newConfig)
            }
        }
    }

    fun verifyBotTokenDirectly(token: String) {
        _botVerificationStatus.value = "VERIFYING"
        addLog("Iniciando verificação do token com Telegram...", LogType.INFO)
        
        viewModelScope.launch {
            try {
                val botUser = withContext(Dispatchers.IO) {
                    repository.verifyBotToken(token)
                }
                _botVerificationStatus.value = "VALID"
                _verifiedBotName.value = botUser.firstName
                _verifiedBotUsername.value = botUser.username
                
                // Update local configuration with tested credentials and save token immediately
                val config = configState.value
                if (config != null) {
                    withContext(Dispatchers.IO) {
                        repository.saveConfig(config.copy(
                            token = token,
                            botName = botUser.firstName,
                            botUsername = botUser.username ?: ""
                        ))
                    }
                }
                
                addLog("Token Válido! Conectado a: @${botUser.username} (${botUser.firstName})", LogType.SUCCESS)
            } catch (e: Exception) {
                _botVerificationStatus.value = "INVALID"
                _verifiedBotName.value = null
                _verifiedBotUsername.value = null
                val errStr = e.localizedMessage ?: e.message ?: "Erro de rede"
                addLog("Erro na Autenticação do Telegram: $errStr", LogType.ERROR)
            }
        }
    }

    private suspend fun startBotServer(config: BotConfigEntity) {
        if (config.token.isBlank()) {
            addLog("Aviso: Token do bot está vazio. Configure seu token na aba Configurar.", LogType.WARNING)
            _isServerRunning.value = false
            return
        }
        
        // Garante que qualquer job de polling anterior seja cancelado imediatamente antes
        pollingJob?.cancel()
        pollingJob = null
        
        _isServerRunning.value = true
        addLog("Iniciando servidor de Inteligência Artificial...", LogType.AI_SYS)

        // Reset offset to process incoming from scratch or current
        lastUpdateId = null

        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            var finalBotDetails = com.example.data.model.TelegramUser(
                id = 8664441309L,
                isBot = true,
                firstName = if (!config.botName.isNullOrEmpty()) config.botName else "IA Bot Server",
                username = if (!config.botUsername.isNullOrEmpty()) config.botUsername else "telegram_bot_ai"
            )

            try {
                addLog("Verificando conexão com Telegram...", LogType.INFO)
                val botDetails = repository.verifyBotToken(config.token)
                finalBotDetails = botDetails
                
                repository.saveConfig(config.copy(
                    botName = finalBotDetails.firstName,
                    botUsername = finalBotDetails.username ?: "",
                    isActive = true
                ))
                addLog("Conexão garantida! Bot online: @${finalBotDetails.username} (${finalBotDetails.firstName})", LogType.SUCCESS)
            } catch (e: Exception) {
                val errDesc = e.localizedMessage ?: e.message ?: "sem sinal de internet"
                addLog("Aviso de Conectividade: $errDesc. Iniciando em modo de escuta resiliente...", LogType.WARNING)
            }

            addLog("Escutando novas mensagens no Telegram via Long Polling...", LogType.INFO)

            while (isActive && _isServerRunning.value) {
                try {
                    val updates = repository.getTelegramUpdates(config.token, lastUpdateId)
                    
                    if (updates.isNotEmpty()) {
                        for (update in updates) {
                            lastUpdateId = update.updateId + 1
                            val msg = update.message
                            
                            // Process valid text messages only
                            if (msg?.text != null) {
                                val senderName = "${msg.from?.firstName ?: ""} ${msg.from?.lastName ?: ""}".trim()
                                val senderId = msg.from?.id ?: 0L
                                val chatId = msg.chat.id
                                
                                addLog("Telegram IN [${chatId}]: de <$senderName>: \"${msg.text}\"", LogType.TG_IN)
                                
                                // 1. Save user message in DB
                                val userMessageEntity = BotMessageEntity(
                                    chatId = chatId,
                                    senderId = senderId,
                                    senderName = senderName,
                                    messageText = msg.text,
                                    isBotReply = false
                                )
                                repository.insertMessage(userMessageEntity)

                                // 2. Trigger AI Response using context history or intercept Gmail commands
                                val incomingText = msg.text.trim()
                                val aiReply = if (incomingText.startsWith("/ver_emails")) {
                                    handleTelegramGmailCheck()
                                } else if (incomingText.startsWith("/enviar_email")) {
                                    handleTelegramGmailSend(incomingText)
                                } else {
                                    addLog("IA pensando para <$senderName>...", LogType.AI_SYS)
                                    repository.generateAiContent(
                                        userMessage = msg.text,
                                        systemPrompt = config.systemPrompt,
                                        temperature = config.temperature,
                                        chatId = chatId
                                    )
                                }

                                // 3. Send AI response back to user
                                addLog("Enviando resposta gerada para o Telegram...", LogType.INFO)
                                try {
                                    val sent = repository.sendTelegramMessage(config.token, chatId, aiReply)
                                    if (sent) {
                                        // 4. Save AI Reply in DB
                                        val aiMessageEntity = BotMessageEntity(
                                            chatId = chatId,
                                            senderId = finalBotDetails.id,
                                            senderName = finalBotDetails.firstName,
                                            messageText = aiReply,
                                            isBotReply = true
                                        )
                                        repository.insertMessage(aiMessageEntity)
                                        addLog("Telegram OUT: Resposta enviada com sucesso!", LogType.TG_OUT)
                                    }
                                } catch (err: Exception) {
                                    val sendErr = err.localizedMessage ?: err.message ?: "Desconhecido"
                                    addLog("Erro ao enviar mensagem para Telegram: $sendErr", LogType.ERROR)
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    val polErr = e.localizedMessage ?: e.message ?: "Desconhecido"
                    addLog("Erro na escuta do servidor: $polErr", LogType.ERROR)
                    delay(5000) // Sleep 5s on error to prevent infinite busy spinning
                }
                delay(1000) // Delay 1s between updates
            }
        }
    }

    private suspend fun stopBotServer() {
        _isServerRunning.value = false
        pollingJob?.cancel()
        pollingJob = null
        repository.updateActiveState(false)
        addLog("Servidor interrompido. Bot Offline.", LogType.WARNING)
    }

    // --- On Device Test Chat Actions ---
    fun sendLocalMockMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            val userMsg = BotMessageEntity(
                chatId = 999999L, // Special chat id for local test
                senderId = 1L,
                senderName = "Você (Teste)",
                messageText = text,
                isBotReply = false
            )
            repository.insertMessage(userMsg)
            addLog("Mock Teste: $text", LogType.INFO)

            _isAiThinkingLocal.value = true
            
            // Retrieve config details for local simulation
            val config = configState.value ?: BotConfigEntity(token = "")
            
            // Generate content
            val aiResponse = repository.generateAiContent(
                userMessage = text,
                systemPrompt = config.systemPrompt,
                temperature = config.temperature,
                chatId = 999999L
            )

            val aiMsg = BotMessageEntity(
                chatId = 999999L,
                senderId = 999L,
                senderName = "IA Bot (Teste)",
                messageText = aiResponse,
                isBotReply = true
            )
            repository.insertMessage(aiMsg)
            _isAiThinkingLocal.value = false
        }
    }

    fun clearLocalHistory() {
        viewModelScope.launch {
            // Clean Special local emulator chats or everything
            repository.clearAllMessages()
            addLog("Histórico de mensagens e log limpos no banco de dados.", LogType.WARNING)
        }
    }

    // --- Gmail Integration Flows ---
    private val _gmailInbox = MutableStateFlow<List<com.example.data.model.GmailEmailItem>>(emptyList())
    val gmailInbox: StateFlow<List<com.example.data.model.GmailEmailItem>> = _gmailInbox.asStateFlow()

    private val _isGmailLoading = MutableStateFlow(false)
    val isGmailLoading: StateFlow<Boolean> = _isGmailLoading.asStateFlow()

    private val _gmailStatusMsg = MutableStateFlow<String?>(null)
    val gmailStatusMsg: StateFlow<String?> = _gmailStatusMsg.asStateFlow()

    fun connectGmail(email: String, token: String, useDemo: Boolean) {
        viewModelScope.launch {
            val config = configState.value ?: return@launch
            val newConfig = config.copy(
                gmailEmail = email,
                gmailIsConnected = true,
                gmailToken = token,
                gmailUseDemoMode = useDemo
            )
            repository.saveConfig(newConfig)
            addLog("Gmail vinculado com sucesso para o usuário: $email", LogType.SUCCESS)
            fetchGmailEmails()
        }
    }

    fun disconnectGmail() {
        viewModelScope.launch {
            val config = configState.value ?: return@launch
            val newConfig = config.copy(
                gmailEmail = "",
                gmailIsConnected = false,
                gmailToken = "",
                gmailUseDemoMode = true
            )
            repository.saveConfig(newConfig)
            _gmailInbox.value = emptyList()
            addLog("Serviço do Gmail desconectado com sucesso.", LogType.WARNING)
        }
    }

    fun toggleGmailDemoModeSetting(useDemo: Boolean) {
        viewModelScope.launch {
            val config = configState.value ?: return@launch
            val newConfig = config.copy(gmailUseDemoMode = useDemo)
            repository.saveConfig(newConfig)
            addLog("Formatado modo integração Gmail: ${if (useDemo) "Demo Simulada" else "API Real"}", LogType.INFO)
            fetchGmailEmails()
        }
    }

    fun fetchGmailEmails() {
        val config = configState.value ?: return
        if (!config.gmailIsConnected) {
            _gmailInbox.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isGmailLoading.value = true
            _gmailStatusMsg.value = "Conectando ao Gmail..."
            delay(1000)

            if (config.gmailUseDemoMode) {
                _gmailInbox.value = listOf(
                    com.example.data.model.GmailEmailItem(
                        id = "msg1",
                        sender = "Google Workspace Team",
                        senderEmail = "workspace-noreply@google.com",
                        subject = "Bem-vindo ao Gmail OAuth Integrado! 🎉",
                        snippet = "Parabéns! Sua integração de Gmail com o aplicativo de IA para o Telegram foi estabelecida com sucesso. Agora você pode monitorar e-mails e receber avisos.",
                        date = "Hoje, 10:45",
                        isRead = true
                    ),
                    com.example.data.model.GmailEmailItem(
                        id = "msg2",
                        sender = "Gemini AI Team",
                        senderEmail = "gemini-api@google.com",
                        subject = "Dica de Prompting: Supercharge seu Bot ♊",
                        snippet = "Para obter melhores respostas do Telegram Bot AI, utilize variáveis de contexto de e-mails em seus prompts e limite a temperatura a 0.7f.",
                        date = "Hoje, 09:32",
                        isRead = false
                    ),
                    com.example.data.model.GmailEmailItem(
                        id = "msg3",
                        sender = "Suporte Telegram Bot",
                        senderEmail = "support@telegram.org",
                        subject = "Servidor Inteligent Ativo 🛰️",
                        snippet = "O servidor de escuta local do robô Telegram foi sincronizado com sucesso e as credenciais de Gmail conectadas.",
                        date = "Ontem, 11:15",
                        isRead = true
                    )
                )
                _gmailStatusMsg.value = "Sincronizado (Demo)"
                addLog("Gmail: Sincronizadas 3 mensagens demonstrativas.", LogType.INFO)
            } else {
                if (config.gmailToken.isBlank()) {
                    _gmailStatusMsg.value = "Erro: Token ausente"
                    _isGmailLoading.value = false
                    return@launch
                }
                try {
                    val authHeader = "Bearer ${config.gmailToken}"
                    val response = com.example.data.api.NetworkModule.gmailApiService.listMessages(authHeader, maxResults = 5)
                    val summaries = response.messages ?: emptyList()
                    
                    val parsedList = mutableListOf<com.example.data.model.GmailEmailItem>()
                    for (sum in summaries) {
                        try {
                            val details = com.example.data.api.NetworkModule.gmailApiService.getMessageDetails(authHeader = authHeader, messageId = sum.id)
                            val headers = details.payload?.headers ?: emptyList()
                            val from = headers.firstOrNull { it.name.trim().lowercase() == "from" }?.value ?: "Desconhecido"
                            val subject = headers.firstOrNull { it.name.trim().lowercase() == "subject" }?.value ?: "(Sem Assunto)"
                            val date = headers.firstOrNull { it.name.trim().lowercase() == "date" }?.value ?: ""
                            
                            val parsedEmail = if (from.contains("<")) {
                                from.substringAfter("<").substringBefore(">")
                            } else {
                                from
                            }
                            val parsedName = if (from.contains("<")) {
                                from.substringBefore("<").trim()
                            } else {
                                from
                            }

                            parsedList.add(
                                com.example.data.model.GmailEmailItem(
                                    id = details.id,
                                    sender = parsedName.ifEmpty { parsedEmail },
                                    senderEmail = parsedEmail,
                                    subject = subject,
                                    snippet = details.snippet ?: "",
                                    date = date.substringBefore("+").substringBefore("-").trim(),
                                    isRead = !details.labelIds.orEmpty().contains("UNREAD")
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("BotViewModel", "Error inside details fetch: ${e.message}")
                        }
                    }
                    _gmailInbox.value = parsedList
                    _gmailStatusMsg.value = "Sincronizado via API"
                    addLog("Gmail Inbox: Sincronizados ${parsedList.size} e-mails reais com sucesso.", LogType.SUCCESS)
                } catch (e: Exception) {
                    val errMsg = e.localizedMessage ?: e.message ?: "Falha na conexão"
                    _gmailStatusMsg.value = "Erro: Token Expirado/Inválido"
                    addLog("Erro Gmail API real: $errMsg. Use o modo demo ou re-insira um token de acesso válido.", LogType.ERROR)
                }
            }
            _isGmailLoading.value = false
        }
    }

    fun sendGmailEmail(to: String, subject: String, body: String) {
        val config = configState.value ?: return
        if (!config.gmailIsConnected) {
            addLog("Aviso: Conecte o Gmail antes de enviar e-mails.", LogType.WARNING)
            return
        }

        viewModelScope.launch {
            _isGmailLoading.value = true
            _gmailStatusMsg.value = "Enviando e-mail..."
            delay(1200)

            if (config.gmailUseDemoMode) {
                val newE = com.example.data.model.GmailEmailItem(
                    id = "sent_" + java.util.UUID.randomUUID().toString().take(6),
                    sender = "Você (Gmail Demo)",
                    senderEmail = config.gmailEmail.ifEmpty { "me@gmail.com" },
                    subject = "Enviado: $subject",
                    snippet = body,
                    date = "Agora",
                    isRead = true
                )
                _gmailInbox.value = listOf(newE) + _gmailInbox.value
                _gmailStatusMsg.value = "Mensagem enviada com sucesso!"
                addLog("E-mail Enviado! Para: $to | Assunto: $subject", LogType.SUCCESS)
            } else {
                if (config.gmailToken.isBlank()) {
                    _gmailStatusMsg.value = "Erro: Token vazio"
                    _isGmailLoading.value = false
                    return@launch
                }
                try {
                    val authHeader = "Bearer ${config.gmailToken}"
                    val emailContent = "From: ${config.gmailEmail}\n" +
                            "To: $to\n" +
                            "Subject: $subject\n" +
                            "Content-Type: text/plain; charset=UTF-8\n\n" +
                            body
                    val base64Raw = android.util.Base64.encodeToString(
                        emailContent.toByteArray(),
                        android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
                    )
                    
                    val sendReq = com.example.data.api.GmailSendRequest(raw = base64Raw)
                    com.example.data.api.NetworkModule.gmailApiService.sendMessage(authHeader, request = sendReq)
                    addLog("E-mail real enviado! Para: $to | Assunto: $subject", LogType.SUCCESS)
                    _gmailStatusMsg.value = "Enviado real!"
                    fetchGmailEmails()
                } catch (e: Exception) {
                    val errMsg = e.localizedMessage ?: e.message ?: "Falha"
                    _gmailStatusMsg.value = "Erro no envio"
                    addLog("Erro Gmail API ao enviar real: $errMsg", LogType.ERROR)
                }
            }
            _isGmailLoading.value = false
        }
    }

    fun handleTelegramGmailCheck(): String {
        val config = configState.value
        if (config == null || !config.gmailIsConnected) {
            return "⚠️ *Serviço do Gmail Desconectado!*\n\n" +
                   "Por favor, configure sua conta de e-mail na aba *Gmail Integrado* do aplicativo."
        }
        
        val emails = _gmailInbox.value
        if (emails.isEmpty()) {
            return "📥 *E-mails cadastrados para: ${config.gmailEmail}*\n\n" +
                   "Nenhum e-mail sincronizado no momento.\n" +
                   "💡 _Abra o aplicativo e clique em Sincronizar._"
        }

        val sb = StringBuilder()
        sb.append("📥 *Seus Últimos E-mails (${config.gmailEmail}):*\n\n")
        emails.take(5).forEachIndexed { index, item ->
            sb.append("${index + 1}. *De:* ${item.sender} (<${item.senderEmail}>)\n")
            sb.append("   *Assunto:* _${item.subject}_\n")
            sb.append("   *Snippet:* ${item.snippet.take(100)}...\n")
            sb.append("   *Status:* ${if (item.isRead) "Lido" else "🔴 Não Lido"}\n\n")
        }
        sb.append("💡 _Digite `/enviar_email destinatario, assunto, mensagem` para enviar novas respostas pelo Telegram._")
        return sb.toString()
    }

    fun handleTelegramGmailSend(commandText: String): String {
        val config = configState.value
        if (config == null || !config.gmailIsConnected) {
            return "⚠️ *Gmail Desconectado!* Conecte sua conta na aba Gmail do aplicativo."
        }

        val paramStr = commandText.removePrefix("/enviar_email").trim()
        if (paramStr.isEmpty()) {
            return "⚠️ *Formato Inválido!*\n\n" +
                   "Formato: `/enviar_email destinatario@gmail.com, Assunto, Escreva a mensagem`"
        }

        val parts = paramStr.split(",", limit = 3)
        if (parts.size < 3) {
            return "⚠️ *Parâmetros incorretos!*\n\n" +
                   "Separadores por vírgula em falta. Exemplo:\n" +
                   "`/enviar_email teste@gmail.com, Assunto de Teste, Mensagem do email aqui`"
        }

        val to = parts[0].trim()
        val subject = parts[1].trim()
        val body = parts[2].trim()

        sendGmailEmail(to, subject, body)

        return "📧 *Disparo de E-mail Solicitado via Telegram!*\n\n" +
               "• *Enviar para:* $to\n" +
               "• *Assunto:* $subject\n" +
               "• *Canal:* ${if (config.gmailUseDemoMode) "Demo Simulada" else "API Real"}\n\n" +
               "Monitore o status do envio na console de logs do seu aplicativo."
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
