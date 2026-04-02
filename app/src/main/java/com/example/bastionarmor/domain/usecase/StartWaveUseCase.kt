package com.example.bastionarmor.domain.usecase

import com.example.bastionarmor.domain.model.*
import javax.inject.Inject

class StartWaveUseCase @Inject constructor() {
    operator fun invoke(state: GameState, gameBoard: GameBoard): Result<GameState> {
        if (state.gameStatus == GameStatus.PLAYING && state.enemies.isNotEmpty()) {
            return Result.failure(Exception("Wave already in progress"))
        }

        if (state.currentWave > 3) {
            return Result.failure(Exception("All waves completed"))
        }

        val startPosition = gameBoard.path.firstOrNull() ?: Position(50f, 100f)
        val wave = getWaveForLevel(state.currentWave)
        val enemies = createEnemiesFromWave(wave, startPosition)

        val newState = state.copy(
            gameStatus = GameStatus.PLAYING,
            enemies = enemies
        )

        return Result.success(newState)
    }

    private fun createEnemiesFromWave(wave: Wave, startPosition: Position): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var enemyId = 1
        
        // Stagger spawning by placing enemies behind each other at the start
        // This is a simple way to create a 'line' of enemies
        var spawnOffset = 0f

        wave.enemies.forEach { spawn ->
            repeat(spawn.count) {
                // Determine direction to the next waypoint to know where 'behind' is
                // For simplicity, we just offset them on the X axis negatively
                val offsetPos = Position(startPosition.x - spawnOffset, startPosition.y)
                
                enemies.add(
                    Enemy(
                        id = enemyId++,
                        type = spawn.enemyType,
                        position = offsetPos,
                        health = spawn.enemyType.displayedHealth,
                        maxHealth = spawn.enemyType.displayedHealth,
                        speed = spawn.enemyType.baseSpeed,
                        reward = spawn.enemyType.baseReward,
                        damage = spawn.enemyType.baseDamage,
                        firingRate = spawn.enemyType.baseFiringRate,
                        isAlive = true
                    )
                )
                // Increase offset for the next enemy so they don't overlap
                spawnOffset += 40f 
            }
        }
        return enemies
    }

    private fun getWaveForLevel(level: Int): Wave {
        return when (level) {
            1 -> Wave(
                waveNumber = 1, 
                enemies = listOf(
                    EnemySpawn(EnemyType.BASIC, 12, 1000L) // Increased from 8
                ), 
                isCompleted = false
            )
            2 -> Wave(
                waveNumber = 2, 
                enemies = listOf(
                    EnemySpawn(EnemyType.BASIC, 15, 800L), // Increased from 10
                    EnemySpawn(EnemyType.FAST, 8, 600L)    // Increased from 5
                ), 
                isCompleted = false
            )
            3 -> Wave(
                waveNumber = 3, 
                enemies = listOf(
                    EnemySpawn(EnemyType.BASIC, 10, 500L),
                    EnemySpawn(EnemyType.FAST, 10, 400L),
                    EnemySpawn(EnemyType.TANK, 8, 1200L),  // Increased from 5
                    EnemySpawn(EnemyType.BOSS, 2, 2000L)   // Increased from 1
                ), 
                isCompleted = false
            )
            else -> Wave(1, listOf(EnemySpawn(EnemyType.BASIC, 1, 1000L)), false)
        }
    }
}
