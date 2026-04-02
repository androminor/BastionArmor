package com.example.bastionarmor.domain.usecase

import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.repository.GameRepository
import com.example.bastionarmor.utility.UpgradeOption
import com.example.bastionarmor.utility.UpgradeType
import jakarta.inject.Inject

class UpgradeTowerUseCase @Inject constructor(private val repository: GameRepository) {
    suspend operator fun invoke(towerId: String, option: UpgradeOption): Result<GameState> {
        val state = repository.getGameState()
        val tower = state.towers.find { it.id == towerId }
            ?: return Result.failure(Exception("Tower not found"))
        if (!state.canAfford(option.cost)) return Result.failure(Exception("Insufficient gold"))

        val updatedTower = when (option.type) {
            UpgradeType.LEVEL_UP -> tower.upgrade()
            UpgradeType.TYPE_SWAP -> tower.upgradeToType(option.targetType!!)
            UpgradeType.REPAIR -> tower.repair().first
        }
        val newTowers = state.towers.map { if (it.id == towerId) updatedTower else it }
        val newGold = state.playerGold - option.cost
        val newState = state.copy(towers = newTowers, playerGold = newGold)
        repository.saveGameState(newState)
        return Result.success(newState)
    }
}