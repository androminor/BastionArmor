package com.example.bastionarmor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val currentWave: Int,
    val playerGold: Int,
    val playerLives: Int,
    val score: Int,
    val towersJson: String,
    val enemiesJson: String,
    val gameStatus: String,
    val gameSpeed: Float,
    val selectedTowerType: String? = null
)
