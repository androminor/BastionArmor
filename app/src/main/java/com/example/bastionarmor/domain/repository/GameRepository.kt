package com.example.bastionarmor.domain.repository

import com.example.bastionarmor.domain.model.GameBoard
import com.example.bastionarmor.domain.model.GameState

interface GameRepository {
    suspend fun getGameState(): GameState
    suspend fun saveGameState(state: GameState)
    fun getGameBoard(): GameBoard
}