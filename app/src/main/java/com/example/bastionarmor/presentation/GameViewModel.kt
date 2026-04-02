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
import com.example.bastionarmor.utility.UpgradeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            // Load initial state
            _gameState.value = repository.getGameState()
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        if (!gameLoopRunning) {
            gameLoopRunning = true
            viewModelScope.launch {
                while (gameLoopRunning) {
                    try {
                        val currentTime = System.currentTimeMillis()
                        val deltaTime = 1f / 60f // 60 FPS

                        // Only run game updates when actually playing
                        if (_gameState.value.gameStatus == GameStatus.PLAYING) {
                            val updatedState = updateGameUseCase(deltaTime, currentTime)
                            _gameState.value = updatedState

                            // Handle wave completion (but don't auto-start next wave)
                            if (updatedState.gameStatus == GameStatus.WAVE_COMPLETE &&
                                updatedState.currentWave < 3) {
                                val nextWaveState = updatedState.copy(
                                    currentWave = updatedState.currentWave + 1,
                                    gameStatus = GameStatus.WAITING_TO_START // Wait for manual start
                                )
                                repository.saveGameState(nextWaveState)
                                _gameState.value = nextWaveState
                            }
                        }

                        delay(16L) // ~60 FPS
                    } catch (e: Exception) {
                        e.printStackTrace()
                        delay(100L) // Prevent rapid error loops
                    }
                }
            }
        }
    }

    fun getGameBoard(): GameBoard {
        return repository.getGameBoard()
    }

    fun placeTower(type: TowerType, position: Position) {
        viewModelScope.launch {
            try {
                placeTowerUseCase(type, position).onSuccess { newState ->
                    _gameState.value = newState
                }.onFailure { error ->
                    // Handle error - maybe show a toast or log
                    println("Failed to place tower: ${error.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun upgradeTower(towerId: String, option: UpgradeOption) {
        viewModelScope.launch {
            try {
                upgradeTowerUseCase(towerId, option).onSuccess { newState ->
                    _gameState.value = newState
                }.onFailure { error ->
                    println("Failed to upgrade tower: ${error.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startWave() {
        viewModelScope.launch {
            try {
                val currentState = _gameState.value

                // Only start wave if we're not already playing and have valid wave number
                if (currentState.gameStatus != GameStatus.PLAYING &&
                    currentState.currentWave <= 3) {

                    startWaveUseCase().onSuccess { newState ->
                        _gameState.value = newState
                        println("Wave ${newState.currentWave} started successfully!")
                    }.onFailure { error ->
                        println("Failed to start wave: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectTowerType(type: TowerType?) {
        val currentState = _gameState.value
        _gameState.value = currentState.copy(selectedTowerType = type)

        // Also save to repository to persist selection
        viewModelScope.launch {
            repository.saveGameState(_gameState.value)
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            val newState = GameState.default()
            repository.saveGameState(newState)
            _gameState.value = newState
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopRunning = false
    }
}