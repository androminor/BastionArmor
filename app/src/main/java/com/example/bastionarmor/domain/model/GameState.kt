package com.example.bastionarmor.domain.model

data class GameState(
    val currentWave: Int = 1,
    val playerGold: Int = 200,
    val playerLives: Int = 20,
    val score: Int = 0,
    val towers: List<Tower> = emptyList(),
    val enemies: List<Enemy> = emptyList(),
    val gameStatus: GameStatus = GameStatus.WAITING_TO_START, // Changed from PLAYING
    val gameSpeed: Float = 1.0f,
    val selectedTowerType: TowerType? = null
) {
    fun canAfford(cost: Int): Boolean = playerGold >= cost

    fun isGameOver(): Boolean = playerLives <= 0 || gameStatus == GameStatus.GAME_OVER

    fun isWaveComplete(): Boolean = enemies.isEmpty()

    companion object {
        fun default() = GameState()
    }
}

enum class GameStatus {
    WAITING_TO_START, // Added new status

    PLAYING,
    PAUSE,
    GAME_OVER,
    WAVE_COMPLETE,
    VICTORY
}
