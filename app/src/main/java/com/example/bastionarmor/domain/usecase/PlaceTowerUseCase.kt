package com.example.bastionarmor.domain.usecase

import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.model.Position
import com.example.bastionarmor.domain.model.Tower
import com.example.bastionarmor.domain.model.TowerType
import com.example.bastionarmor.domain.repository.GameRepository
import java.util.UUID
import javax.inject.Inject

class PlaceTowerUseCase @Inject constructor(private val repository: GameRepository) {
    suspend operator fun invoke(type: TowerType, position: Position): Result<GameState> {
        val state = repository.getGameState()

        println("=== TOWER PLACEMENT DEBUG ===")
        println("Trying to place ${type.displayName} at ($${position.x}, ${position.y})")
        println("Player gold: ${state.playerGold}, Tower cost: ${type.baseCost}")
        println("Can afford: ${state.canAfford(type.baseCost)}")

        if (!state.canAfford(type.baseCost)) {
            println("Failed: Insufficient gold")
            return Result.failure(Exception("Insufficient gold"))
        }

        val gameBoard = repository.getGameBoard()
        val isValidPosition = gameBoard.isValidTowerPosition(position)
        println("Is valid position: $isValidPosition")

        if (!isValidPosition) {
            println("Failed: Invalid position")
            return Result.failure(Exception("Invalid position"))
        }

        val snappedPosition = gameBoard.snapToGrid(position)
        println("Snapped position: (${snappedPosition.x}, ${snappedPosition.y})")

        // Check if there's already a tower at this position
        val existingTower = state.towers.find {
            val distance = it.position.distancessTo(snappedPosition)
            distance < 30f // Within 30 pixels
        }

        if (existingTower != null) {
            println("Failed: Tower already exists at this position")
            return Result.failure(Exception("Tower already exists at this position"))
        }

        val newTower = Tower(
            id = UUID.randomUUID().toString(),
            position = snappedPosition,
            type = type,
            damage = type.baseDamage,
            maxDamage = type.baseDamage,
            range = type.baseRange,
            fireRate = type.baseFireRate,
            cost = type.baseCost,
            health = 100
        )

        val newTowers = state.towers + newTower
        val newGold = state.playerGold - type.baseCost
        val newState = state.copy(
            towers = newTowers,
            playerGold = newGold,
            selectedTowerType = null
        )

        println("Tower placed successfully! New gold: $newGold, Total towers: ${newTowers.size}")
        println("========================")

        repository.saveGameState(newState)
        return Result.success(newState)
    }
}