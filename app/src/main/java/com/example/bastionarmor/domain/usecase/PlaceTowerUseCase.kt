package com.example.bastionarmor.domain.usecase

import com.example.bastionarmor.domain.model.GameBoard
import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.model.Position
import com.example.bastionarmor.domain.model.Tower
import com.example.bastionarmor.domain.model.TowerType
import java.util.UUID
import javax.inject.Inject

class PlaceTowerUseCase @Inject constructor() {
    operator fun invoke(state: GameState, gameBoard: GameBoard, type: TowerType, position: Position): Result<GameState> {
        if (!state.canAfford(type.baseCost)) {
            return Result.failure(Exception("Insufficient gold"))
        }

        if (!gameBoard.isValidTowerPosition(position)) {
            return Result.failure(Exception("Invalid position"))
        }

        val snappedPosition = gameBoard.snapToGrid(position)

        // Check if there's already a tower at this position
        val existingTower = state.towers.find {
            it.position.distanceTo(snappedPosition) < 30f 
        }

        if (existingTower != null) {
            return Result.failure(Exception("Tower already exists here"))
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

        return Result.success(state.copy(
            towers = state.towers + newTower,
            playerGold = state.playerGold - type.baseCost,
            selectedTowerType = null // Clear selection after successful placement
        ))
    }
}
