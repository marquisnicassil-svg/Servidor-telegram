package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BotConfigEntity::class, BotMessageEntity::class], version = 3, exportSchema = false)
abstract class BotDatabase : RoomDatabase() {
    abstract fun botDao(): BotDao

    companion object {
        @Volatile
        private var INSTANCE: BotDatabase? = null

        fun getDatabase(context: Context): BotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BotDatabase::class.java,
                    "telegram_bot_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
