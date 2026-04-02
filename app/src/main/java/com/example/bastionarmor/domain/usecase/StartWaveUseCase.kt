package com.example.bastionarmor.domain.usecase

import com.example.bastionarmor.domain.model.Enemy
import com.example.bastionarmor.domain.model.EnemySpawn
import com.example.bastionarmor.domain.model.EnemyType
import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.model.GameStatus
import com.example.bastionarmor.domain.model.Position
import com.example.bastionarmor.domain.model.Wave
import com.example.bastionarmor.domain.repository.GameRepository
import javax.inject.Inject

class StartWaveUseCase @Inject constructor(private val repository: GameRepository) {
    suspend operator fun invoke(): Result<GameState> {
        val state = repository.getGameState()

        // Check if we can start a new wave
        if (state.gameStatus == GameStatus.PLAYING && state.enemies.isNotEmpty()) {
            return Result.failure(Exception("Wave already in progress"))
        }

        if (state.currentWave > 3) {
            return Result.failure(Exception("All waves completed"))
        }

        val gameBoard = repository.getGameBoard()
        val startPosition = gameBoard.path.firstOrNull() ?: Position(50f, 100f)

        val wave = getWaveForLevel(state.currentWave)
        val enemies = createEnemiesFromWave(wave, startPosition)

        println("Starting wave ${state.currentWave} with ${enemies.size} enemies")
        println("First enemy position: ${enemies.firstOrNull()?.position}")

        val newState = state.copy(
            gameStatus = GameStatus.PLAYING,
            enemies = enemies
        )

        repository.saveGameState(newState)
        return Result.success(newState)
    }

    private fun createEnemiesFromWave(wave: Wave, startPosition: Position): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var enemyId = 1

        wave.enemies.forEach { spawn ->
            repeat(spawn.count) { index ->
                val enemy = Enemy(
                    id = enemyId++,
                    type = spawn.enemyType,
                    position = startPosition.copy(), // Start at beginning of path
                    health = spawn.enemyType.displayedHealth,
                    maxHealth = spawn.enemyType.displayedHealth,
                    speed = spawn.enemyType.baseSpeed,
                    reward = spawn.enemyType.baseReward,
                    damage = spawn.enemyType.baseDamage,
                    firingRate = spawn.enemyType.baseFiringRate,
                    lastAttackTime = 0L,
                    pathIndex = 0, // Start at beginning of path
                    isAlive = true
                )
                enemies.add(enemy)
                println("Created enemy $enemyId: ${spawn.enemyType} at $startPosition")
            }
        }

        return enemies
    }

    private fun getWaveForLevel(level: Int): Wave {
        return when (level) {
            1 -> Wave(
                waveNumber = 1,
                enemies = listOf(
                    EnemySpawn(EnemyType.BASIC, 5, 1000L)  // Reduced count for testing
                ),
                isCompleted = false
            )
            2 -> Wave(
                waveNumber = 2,
                enemies = listOf(
                    EnemySpawn(EnemyType.BASIC, 4, 800L),
                    EnemySpawn(EnemyType.FAST, 3, 600L),
                    EnemySpawn(EnemyType.TANK, 1, 1500L)
                ),
                isCompleted = false
            )
            3 -> Wave(
                waveNumber = 3,
                enemies = listOf(
                    EnemySpawn(EnemyType.BASIC, 6, 500L),
                    EnemySpawn(EnemyType.FAST, 4, 400L),
                    EnemySpawn(EnemyType.TANK, 2, 1200L),
                    EnemySpawn(EnemyType.BOSS, 1, 2000L)
                ),
                isCompleted = false
            )
            else -> Wave(
                waveNumber = 1,
                enemies = listOf(EnemySpawn(EnemyType.BASIC, 1, 1000L)),
                isCompleted = false
            )
        }
    }
}