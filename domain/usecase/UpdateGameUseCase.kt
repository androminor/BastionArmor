package com.example.bastionarmor.domain.usecase

import com.example.bastionarmor.domain.model.GameBoard
import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.model.GameStatus
import javax.inject.Inject

class UpdateGameUseCase @Inject constructor() {
    operator fun invoke(state: GameState, gameBoard: GameBoard, deltaTime: Float, currentTime: Long): GameState {
        if (state.gameStatus != GameStatus.PLAYING) {
            return state
        }

        var playerGold = state.playerGold
        var playerLives = state.playerLives
        var score = state.score

        // 1. Move enemies
        val movedEnemies = state.enemies.map { enemy ->
            enemy.moveAlongPath(gameBoard.path, deltaTime * state.gameSpeed)
        }

        // 2. Filter enemies that reached the end
        val pathSize = gameBoard.path.size
        val activeEnemies = movedEnemies.filter { enemy ->
            if (enemy.pathIndex >= pathSize - 1) {
                playerLives = (playerLives - 1).coerceAtLeast(0)
                false
            } else {
                true
            }
        }.toMutableList()

        // 3. Tower Logic: Shooting
        val updatedTowers = state.towers.map { tower ->
            var updatedTower = tower
            if (tower.isOperational() && tower.canShoot(currentTime)) {
                // Find first target in range
                val target = activeEnemies.firstOrNull { enemy ->
                    tower.position.distanceTo(enemy.position) <= tower.range
                }

                if (target != null) {
                    val index = activeEnemies.indexOfFirst { it.id == target.id }
                    if (index != -1) {
                        val damagedEnemy = activeEnemies[index].takeDamage(tower.damage)
                        if (!damagedEnemy.isAlive) {
                            activeEnemies.removeAt(index)
                            playerGold += target.reward
                            score += target.reward
                        } else {
                            activeEnemies[index] = damagedEnemy
                        }
                    }
                    updatedTower = tower.copy(
                        lastShotTime = currentTime,
                        lastShotTargetPos = target.position
                    )
                } else {
                    // No target found, check if we should clear the visual beam
                    if (currentTime - tower.lastShotTime > 150) {
                        updatedTower = tower.copy(lastShotTargetPos = null)
                    }
                }
            } else {
                // Cooling down, check if we should clear the visual beam
                if (currentTime - tower.lastShotTime > 150) {
                    updatedTower = tower.copy(lastShotTargetPos = null)
                }
            }
            updatedTower
        }

        // 4. Status and Wave Transition
        var currentWave = state.currentWave
        var nextStatus = GameStatus.PLAYING

        if (activeEnemies.isEmpty()) {
            if (currentWave >= 3) {
                nextStatus = GameStatus.VICTORY
            } else {
                nextStatus = GameStatus.WAVE_COMPLETE
                currentWave += 1 // Increment wave for the next start
            }
        } else if (playerLives <= 0) {
            nextStatus = GameStatus.GAME_OVER
        }

        return state.copy(
            enemies = activeEnemies,
            towers = updatedTowers,
            playerGold = playerGold,
            playerLives = playerLives,
            score = score,
            gameStatus = nextStatus,
            currentWave = currentWave
        )
    }
}
