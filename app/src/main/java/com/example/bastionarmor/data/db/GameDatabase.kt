package com.example.bastionarmor.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GameStateEntity::class],
    version = 1,
    exportSchema = false // This fixes the schema export warning
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameStateDao(): GameStateDao
}