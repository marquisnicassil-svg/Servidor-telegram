package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import com.example.data.api.NetworkModule
import com.example.data.database.BotDao
import com.example.data.database.BotConfigEntity
import com.example.data.database.BotMessageEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

class BotRepository(private val botDao: BotDao) {

    // --- Database Operations ---
    val configFlow: Flow<BotConfigEntity?> = botDao.getConfigFlow()
    val allMessagesFlow: Flow<List<BotMessageEntity>> = botDao.getAllMessagesFlow()
    val distinctChatsCountFlow: Flow<Int> = botDao.getDistinctChatsCountFlow()
    val messagesCountFlow: Flow<Int> = botDao.getMessagesCountFlow()
    val aiResponsesCountFlow: Flow<Int> = botDao.getAiResponsesCountFlow()

    suspend fun getConfig(): BotConfigEntity? = botDao.getConfig()

    suspend fun saveConfig(config: BotConfigEntity) {
        botDao.saveConfig(config)
    }

    suspend fun updateActiveState(isActive: Boolean) {
        botDao.updateActiveState(isActive)
    }

    suspend fun insertMessage(message: BotMessageEntity) {
        botDao.insertMessage(message)
    }

    suspend fun clearAllMessages() {
        botDao.clearAllMessages()
    }

    suspend fun getRecentMessagesForChat(chatId: Long, limit: Int): List<BotMessageEntity> {
        return botDao.getRecentMessagesForChat(chatId, limit)
    }

    // --- Telegram API Operations ---
    suspend fun verifyBotToken(token: String): TelegramUser {
        val response = NetworkModule.telegramApiService.getMe(token)
        if (response.ok && response.result != null) {
            return response.result
        } else {
            throw Exception("Telegram API Error: ${response.description ?: "Falha na resposta ok"}")
        }
    }

    suspend fun getTelegramUpdates(token: String, offset: Long?, timeout: Int = 10): List<TelegramUpdate> {
        val response = NetworkModule.telegramApiService.getUpdates(
            token = token,
            offset = offset,
            limit = 10,
            timeout = timeout
        )
        if (response.ok) {
            return response.result
        } else {
            throw Exception("Telegram API Error: ${response.description ?: "Falha na resposta de updates"}")
        }
    }

    suspend fun sendTelegramMessage(token: String, chatId: Long, text: String): Boolean {
        val request = TelegramSendMessageRequest(chatId = chatId, text = text)
        val response = NetworkModule.telegramApiService.sendMessage(token, request)
        if (response.ok) {
            return true
        } else {
            throw Exception("Telegram API Error: ${response.description ?: "Desconhecido ao enviar mensagem"}")
        }
    }

    // --- Gemini API Operations ---
    private fun getSmartFallbackResponse(message: String): String {
        val cleanMsg = message.trim().lowercase()
        return when {
            cleanMsg == "/start" || cleanMsg.contains("olá") || cleanMsg.contains("ola") || cleanMsg.contains("oi") || cleanMsg.contains("hello") || cleanMsg.contains("bom dia") || cleanMsg.contains("boa tarde") || cleanMsg.contains("boa noite") -> {
                "✨ *Olá! Seja muito bem-vindo!* \n\n" +
                "Eu sou o seu **Assistente Virtual IA no Telegram**! 🤖\n" +
                "No momento, o servidor está rodando em modo **Fallback Conectado** (sem chave Gemini configurada), mas estou pronto para conversar com você nesta conexão direta!\n\n" +
                "💡 *Comandos disponíveis:*\n" +
                "• `/ajuda` - Veja o que posso fazer\n" +
                "• `/status` - Métricas atuais do servidor\n" +
                "• `/ambiente` - Informações da conexão\n\n" +
                "Como posso te ajudar hoje?"
            }
            cleanMsg == "/ajuda" || cleanMsg.contains("ajuda") || cleanMsg.contains("help") || cleanMsg.contains("como funciona") -> {
                "📋 *Painel de Ajuda Rápida* 🤖\n\n" +
                "Você está conectado ao servidor do Telegram hospedado diretamente no seu aplicativo.\n\n" +
                "🔑 *Para ativar a IA completa via Gemini:*\n" +
                "1. Abra seu projeto no **Google AI Studio**.\n" +
                "2. Vá ao painel de **Secrets** (ícone de chave na barra lateral).\n" +
                "3. Adicione `GEMINI_API_KEY` com sua chave pessoal do Gemini.\n" +
                "4. Clique em reiniciar o servidor no app ao lado.\n\n" +
                "Enquanto isso, você pode continuar enviando mensagens para mim aqui no Telegram!"
            }
            cleanMsg == "/status" || cleanMsg.contains("status") || cleanMsg.contains("servidor") || cleanMsg.contains("métricas") -> {
                "📊 *Status do Servidor Telegram* 🖥️\n\n" +
                "• **Alimentação:** Local Script Switch v2.0\n" +
                "• **Status da Conexão:** Ativo & Polling 📶\n" +
                "• **API do Gemini:** Desativada (Modo Conversação Local)\n" +
                "• **Velocidade de Resposta:** Instantânea (< 1s)\n\n" +
                "O servidor está monitorando novas mensagens a cada segundo com sucesso!"
            }
            cleanMsg == "/ambiente" || cleanMsg.contains("ambiente") || cleanMsg.contains("dispositivo") -> {
                "📱 *Dados do Ambiente de Execução*\n\n" +
                "• **Hospedagem:** Android Client Runtime 🚀\n" +
                "• **Banco de Dados:** Room Database SQLite (Persistência Offline)\n" +
                "• **Canal de Comunicação:** Telegram Long Polling API v6.0\n\n" +
                "Seu aplicativo está servindo essa conexão perfeitamente!"
            }
            cleanMsg.contains("quem é você") || cleanMsg.contains("quem e voce") || cleanMsg.contains("nome") -> {
                "Eu sou o seu chatbot inteligente conectado ao Telegram! Fui integrado em Kotlin com Android Jetpack Compose e Room Database."
            }
            cleanMsg.contains("piada") -> {
                "Por que o computador foi ao médico? Porque ele estava com vírus! 🤖💻"
            }
            cleanMsg.contains("obrigado") || cleanMsg.contains("obrigada") || cleanMsg.contains("valeu") || cleanMsg.contains("legal") || cleanMsg.contains("top") -> {
                "De nada! Fico muito contente em ajudar. Se precisar de mais alguma coisa ou quiser testar outros comandos, é apenas me chamar!"
            }
            cleanMsg.contains("tudo bem") || cleanMsg.contains("como vai") -> {
                "Comigo está tudo ótimo! Estou aqui processando as mensagens do Telegram com sucesso. E com você, tudo bem?"
            }
            else -> {
                "💬 *Recebi sua mensagem:* \"$message\"\n\n" +
                "Estou conectado de forma estável no Telegram! 🛰️\n\n" +
                "Como você não possui uma chave `GEMINI_API_KEY` configurada no momento, estou respondendo no modo de simulação inteligente local.\n\n" +
                "Digite `/ajuda`, `/status` ou `/ambiente` para explorar meus comandos no servidor!"
            }
        }
    }

    suspend fun generateAiContent(
        userMessage: String,
        systemPrompt: String,
        temperature: Float,
        chatId: Long,
        historyLimit: Int = 8
    ): String {
        return try {
            val config = getConfig()
            val aiApiKey = config?.aiApiKey ?: ""
            val aiApiType = config?.aiApiType ?: "GEMINI"
            val aiBaseUrl = config?.aiBaseUrl ?: "https://generativelanguage.googleapis.com/"
            val aiModel = config?.aiModel ?: "gemini-1.5-flash"

            generateAiContentUniversal(
                userMessage = userMessage,
                systemPrompt = systemPrompt,
                temperature = temperature,
                chatId = chatId,
                aiApiKey = aiApiKey,
                aiApiType = aiApiType,
                aiBaseUrl = aiBaseUrl,
                aiModel = aiModel,
                historyLimit = historyLimit
            )
        } catch (e: Exception) {
            Log.e("BotRepository", "Error running generateAiContent wrapper", e)
            "Desculpe, ocorreu um erro interno de processamento na inteligência artificial: ${e.localizedMessage ?: e.message}"
        }
    }

    suspend fun generateAiContentUniversal(
        userMessage: String,
        systemPrompt: String,
        temperature: Float,
        chatId: Long,
        aiApiKey: String,
        aiApiType: String,
        aiBaseUrl: String,
        aiModel: String,
        historyLimit: Int = 8
    ): String {
        val finalApiKey = if (aiApiKey.isNotBlank()) {
            aiApiKey.trim()
        } else {
            if (aiApiType == "GEMINI") {
                BuildConfig.GEMINI_API_KEY
            } else {
                ""
            }
        }

        if (finalApiKey.isEmpty() || finalApiKey == "MY_GEMINI_API_KEY") {
            return getSmartFallbackResponse(userMessage)
        }

        return try {
            val dbHistory = getRecentMessagesForChat(chatId, historyLimit)
            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            val isCompatibleOpenAI = aiApiType == "OPENAI" || aiApiType == "GROQ" || aiApiType == "OPENROUTER" || aiApiType == "CUSTOM"

            if (isCompatibleOpenAI) {
                val messages = mutableListOf<OpenAIMessage>()
                if (systemPrompt.isNotBlank()) {
                    messages.add(OpenAIMessage(role = "system", content = systemPrompt))
                }
                dbHistory.reversed().forEach { msg ->
                    val role = if (msg.isBotReply) "assistant" else "user"
                    messages.add(OpenAIMessage(role = role, content = msg.messageText))
                }
                if (dbHistory.firstOrNull()?.messageText != userMessage) {
                    messages.add(OpenAIMessage(role = "user", content = userMessage))
                }

                val openAiRequest = OpenAIChatRequest(
                    model = aiModel,
                    messages = messages,
                    temperature = temperature
                )

                val requestAdapter = moshi.adapter(OpenAIChatRequest::class.java)
                val jsonBody = requestAdapter.toJson(openAiRequest)

                val cleanUrl = if (aiBaseUrl.endsWith("/")) aiBaseUrl else "$aiBaseUrl/"
                val finalUrl = if (cleanUrl.contains("chat/completions")) {
                    cleanUrl
                } else {
                    "${cleanUrl}chat/completions"
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = okhttp3.RequestBody.create(mediaType, jsonBody)

                val cleanApiKey = finalApiKey.replace(Regex("(?i)^Bearer\\s+"), "").replace(Regex("^[\"']|[\"']$"), "").trim()

                val reqBuilder = okhttp3.Request.Builder()
                    .url(finalUrl)
                    .post(body)
                    .addHeader("Authorization", "Bearer $cleanApiKey")
                    .addHeader("Content-Type", "application/json")

                if (aiApiType == "OPENROUTER") {
                    reqBuilder.addHeader("HTTP-Referer", "https://google.com")
                    reqBuilder.addHeader("X-Title", "Synapse AI")
                }

                val req = reqBuilder.build()

                val client = NetworkModule.okHttpClient
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(req).execute()
                }

                if (response.isSuccessful) {
                    val respBodyStr = response.body?.string() ?: ""
                    val responseAdapter = moshi.adapter(OpenAIChatResponse::class.java)
                    val openAiResponse = responseAdapter.fromJson(respBodyStr)
                    openAiResponse?.choices?.firstOrNull()?.message?.content
                        ?: "Erro: Resposta vazia ou formato inválido do provedor OpenAI compatível."
                } else {
                    val errBody = response.body?.string() ?: ""
                    "Erro da API Compatível OpenAI (${response.code}): $errBody"
                }
            } else {
                val contents = mutableListOf<GeminiContent>()
                dbHistory.reversed().forEach { msg ->
                    val role = if (msg.isBotReply) "model" else "user"
                    contents.add(
                        GeminiContent(
                            role = role,
                            parts = listOf(GeminiPart(text = msg.messageText))
                        )
                    )
                }
                if (dbHistory.firstOrNull()?.messageText != userMessage) {
                    contents.add(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = userMessage))
                        )
                    )
                }

                val geminiRequest = GeminiGenerateContentRequest(
                    contents = contents,
                    generationConfig = GeminiGenerationConfig(temperature = temperature),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )

                val requestAdapter = moshi.adapter(GeminiGenerateContentRequest::class.java)
                val jsonBody = requestAdapter.toJson(geminiRequest)

                val cleanUrl = if (aiBaseUrl.endsWith("/")) aiBaseUrl else "$aiBaseUrl/"
                val finalUrl = "${cleanUrl}v1beta/models/${aiModel}:generateContent?key=$finalApiKey"

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = okhttp3.RequestBody.create(mediaType, jsonBody)

                val req = okhttp3.Request.Builder()
                    .url(finalUrl)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val client = NetworkModule.okHttpClient
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(req).execute()
                }

                if (response.isSuccessful) {
                    val respBodyStr = response.body?.string() ?: ""
                    val responseAdapter = moshi.adapter(GeminiGenerateContentResponse::class.java)
                    val geminiResponse = responseAdapter.fromJson(respBodyStr)
                    geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                        ?: "Erro: Sem resposta do cérebro IA do Gemini."
                } else {
                    val errBody = response.body?.string() ?: ""
                    "Erro da API Gemini (${response.code}): $errBody"
                }
            }
        } catch (e: Exception) {
            Log.e("BotRepository", "Error in universal generative call", e)
            "Erro de conexão com o motor de Inteligência Artificial: ${e.localizedMessage ?: e.message}"
        }
    }
}
