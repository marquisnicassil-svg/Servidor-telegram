package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bot_config")
data class BotConfigEntity(
    @PrimaryKey val id: Int = 1,
    val token: String,
    val systemPrompt: String = "Você é um assistente útil e amigável no Telegram.",
    val temperature: Float = 0.7f,
    val botUsername: String = "",
    val botName: String = "",
    val isActive: Boolean = false,
    val aiApiKey: String = "",
    val aiApiType: String = "GEMINI", // "GEMINI" or "OPENAI"
    val aiBaseUrl: String = "https://generativelanguage.googleapis.com/",
    val aiModel: String = "gemini-1.5-flash",
    val gmailEmail: String = "",
    val gmailIsConnected: Boolean = false,
    val gmailToken: String = "",
    val gmailUseDemoMode: Boolean = true
)

@Entity(tableName = "bot_messages")
data class BotMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val senderId: Long,
    val senderName: String,
    val messageText: String,
    val isBotReply: Boolean, // true if reply from AI, false if message from user
    val timestamp: Long = System.currentTimeMillis()
)
