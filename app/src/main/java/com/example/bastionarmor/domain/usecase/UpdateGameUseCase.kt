package com.example.bastionarmor.domain.usecase

import com.example.bastionarmor.domain.model.GameBoard
import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.model.GameStatus
import javax.inject.Inject

class UpdateGameUseCase @Inject constructor() {
    operator fun invoke(state: GameState, gameBoard: GameBoard, deltaTime: Float, currentTime: Long): GameState {
        if (state.gameStatus != GameStatus.PLAYING || state.enemies.isEmpty()) {
            return state
        }

        var playerGold = state.playerGold
        var playerLives = state.playerLives
        var score = state.score

        // Step 1: Move enemies
        var aliveEnemies = state.enemies.filter { it.isAlive }.map { enemy ->
            enemy.moveAlongPath(gameBoard.path, deltaTime * state.gameSpeed)
        }.toMutableList()

        // Step 2: Remove enemies that reached end
        val reachingEnd = aliveEnemies.filter { it.pathIndex >= gameBoard.path.size - 1 }
        playerLives -= reachingEnd.size
        aliveEnemies.removeAll(reachingEnd)

        // Step 3: Tower shooting logic
        val updatedTowers = state.towers.map { tower ->
            if (tower.isOperational() && tower.canShoot(currentTime)) {
                // Find first target in range
                val target = aliveEnemies.firstOrNull { enemy ->
                    tower.position.distanceTo(enemy.position) <= tower.range
                }

                if (target != null) {
                    // Deal damage
                    val newHealth = (target.health - tower.damage).coerceAtLeast(0)
                    
                    // Update enemy in our list
                    val index = aliveEnemies.indexOfFirst { it.id == target.id }
                    if (index != -1) {
                        if (newHealth <= 0) {
                            aliveEnemies.removeAt(index)
                            playerGold += target.reward
                            score += target.reward
                        } else {
                            aliveEnemies[index] = target.copy(
                                health = newHealth,
                                isAlive = true
                            )
                        }
                    }
                    tower.copy(lastShotTime = currentTime)
                } else {
                    tower
                }
            } else {
                tower
            }
        }

        // Determine new game status
        val newGameStatus = when {
            playerLives <= 0 -> GameStatus.GAME_OVER
            aliveEnemies.isEmpty() && state.currentWave >= 3 -> GameStatus.VICTORY
            aliveEnemies.isEmpty() -> GameStatus.WAVE_COMPLETE
            else -> GameStatus.PLAYING
        }

        return state.copy(
            enemies = aliveEnemies,
            towers = updatedTowers,
            playerGold = playerGold,
            playerLives = playerLives,
            score = score,
            gameStatus = newGameStatus
        )
    }
}
