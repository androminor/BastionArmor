package com.example.bastionarmor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bastionarmor.domain.model.GameBoard
import com.example.bastionarmor.domain.model.GameState
import com.example.bastionarmor.domain.model.GameStatus
import com.example.bastionarmor.domain.model.Position
import com.example.bastionarmor.domain.model.TowerType
import com.example.bastionarmor.domain.repository.GameRepository
import com.example.bastionarmor.domain.usecase.PlaceTowerUseCase
import com.example.bastionarmor.domain.usecase.StartWaveUseCase
import com.example.bastionarmor.domain.usecase.UpdateGameUseCase
import com.example.bastionarmor.domain.usecase.UpgradeTowerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val placeTowerUseCase: PlaceTowerUseCase,
    private val upgradeTowerUseCase: UpgradeTowerUseCase,
    private val startWaveUseCase: StartWaveUseCase,
    private val updateGameUseCase: UpdateGameUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState.default())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _board = MutableStateFlow(repository.getGameBoard())
    val board: StateFlow<GameBoard> = _board.asStateFlow()

    private var gameLoopRunning = false

    init {
        loadInitialState()
        startGameLoop()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            _gameState.value = repository.getGameState()
        }
    }

    private fun startGameLoop() {
        if (gameLoopRunning) return
        gameLoopRunning = true
        
        viewModelScope.launch {
            while (gameLoopRunning) {
                try {
                    val currentState = _gameState.value
                    if (currentState.gameStatus == GameStatus.PLAYING) {
                        val currentTime = System.currentTimeMillis()
                        val deltaTime = 1f / 60f
                        
                        val updatedState = updateGameUseCase(
                            currentState,
                            _board.value,
                            deltaTime,
                            currentTime
                        )
                        
                        if (updatedState != currentState) {
                            _gameState.value = updatedState
                            // Don't save to DB every frame in the loop for performance
                            // but save occasionally or on important events
                        }
                    }
                    delay(16L) // ~60 FPS
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(100L)
                }
            }
        }
    }

    fun placeTower(type: TowerType, position: Position) {
        viewModelScope.launch {
            placeTowerUseCase(_gameState.value, _board.value, type, position).onSuccess { newState ->
                _gameState.value = newState
                repository.saveGameState(newState)
            }.onFailure { error ->
                println("Failed to place tower: ${error.message}")
            }
        }
    }

    fun selectTowerType(type: TowerType?) {
        val newState = _gameState.value.copy(selectedTowerType = type)
        _gameState.value = newState
        viewModelScope.launch {
            repository.saveGameState(newState)
        }
    }

    fun startWave() {
        viewModelScope.launch {
            startWaveUseCase(_gameState.value, _board.value).onSuccess { newState ->
                _gameState.value = newState
                repository.saveGameState(newState)
            }.onFailure { error ->
                println("Failed to start wave: ${error.message}")
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            val newState = GameState.default()
            _gameState.value = newState
            repository.saveGameState(newState)
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopRunning = false
    }
}
