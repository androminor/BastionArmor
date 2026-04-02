package com.example.bastionarmor.data.repository

import com.example.bastionarmor.data.db.GameStateDao
import com.example.bastionarmor.data.db.GameStateEntity
import com.example.bastionarmor.domain.model.Enemy
import com.example.bastionarmor.domain.model.GameBoard
import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.model.GameStatus
import com.example.bastionarmor.domain.model.Position
import com.example.bastionarmor.domain.model.Tower
import com.example.bastionarmor.domain.model.TowerType
import com.example.bastionarmor.domain.repository.GameRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject


class GameRepositoryImpl @Inject constructor(
    private val dao: GameStateDao,
    private val gson: Gson
) : GameRepository {

    override suspend fun getGameState(): GameState {
        val entity = dao.getGameState()
        return if (entity != null) {
            try {
                GameState(
                    currentWave = entity.currentWave,
                    playerGold = entity.playerGold,
                    playerLives = entity.playerLives,
                    score = entity.score,
                    towers = gson.fromJson(entity.towersJson, object : TypeToken<List<Tower>>() {}.type) ?: emptyList(),
                    enemies = gson.fromJson(entity.enemiesJson, object : TypeToken<List<Enemy>>() {}.type) ?: emptyList(),
                    gameStatus = try { GameStatus.valueOf(entity.gameStatus) } catch (e: Exception) { GameStatus.WAITING_TO_START },
                    gameSpeed = entity.gameSpeed,
                    selectedTowerType = entity.selectedTowerType?.let { try { TowerType.valueOf(it) } catch(e: Exception) { null } }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                GameState.default()
            }
        } else {
            GameState.default()
        }
    }

    override suspend fun saveGameState(state: GameState) {
        try {
            dao.saveGameState(
                GameStateEntity(
                    currentWave = state.currentWave,
                    playerGold = state.playerGold,
                    playerLives = state.playerLives,
                    score = state.score,
                    towersJson = gson.toJson(state.towers),
                    enemiesJson = gson.toJson(state.enemies),
                    gameStatus = state.gameStatus.name,
                    gameSpeed = state.gameSpeed,
                    selectedTowerType = state.selectedTowerType?.name
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getGameBoard(): GameBoard {
        return GameBoard(
            path = listOf(
                Position(50f, 100f),
                Position(200f, 100f),
                Position(200f, 300f),
                Position(400f, 300f),
                Position(400f, 500f),
                Position(600f, 500f)
            )
        )
    }
}