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
    val aiModel: String = "gemini-1.5-flash"
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

@Entity(tableName = "bot_interviews")
data class BotInterviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cargo: String,
    val area: String,
    val nivel: String,
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val scoreGeneral: Int = 0,
    val scoreCommunication: Int = 0,
    val scoreClarity: Int = 0,
    val scoreTechnical: Int = 0,
    val scoreConfidence: Int = 0,
    val strengths: String = "",
    val improvements: String = "",
    val recommendations: String = "",
    val chatHistoryJson: String = "[]" // JSON representation of the interview messages
)
