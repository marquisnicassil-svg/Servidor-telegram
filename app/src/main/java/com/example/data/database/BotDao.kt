package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {
    // --- Config Queries ---
    @Query("SELECT * FROM bot_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): BotConfigEntity?

    @Query("SELECT * FROM bot_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<BotConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: BotConfigEntity)

    @Query("UPDATE bot_config SET isActive = :isActive WHERE id = 1")
    suspend fun updateActiveState(isActive: Boolean)

    // --- Message Queries ---
    @Query("SELECT * FROM bot_messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<BotMessageEntity>>

    @Query("SELECT * FROM bot_messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessagesForChat(chatId: Long, limit: Int): List<BotMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: BotMessageEntity)

    @Query("DELETE FROM bot_messages")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(DISTINCT chatId) FROM bot_messages")
    fun getDistinctChatsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM bot_messages")
    fun getMessagesCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM bot_messages WHERE isBotReply = 1")
    fun getAiResponsesCountFlow(): Flow<Int>
}
