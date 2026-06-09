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

                                // 2. Trigger AI Response using context history
                                addLog("IA pensando para <$senderName>...", LogType.AI_SYS)
                                val aiReply = repository.generateAiContent(
                                    userMessage = msg.text,
                                    systemPrompt = config.systemPrompt,
                                    temperature = config.temperature,
                                    chatId = chatId
                                )

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

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
