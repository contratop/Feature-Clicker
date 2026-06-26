package com.contratop.featureclicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class GameState(
    val isLoaded: Boolean = false,
    val commits: Long = 0,
    val commitsPerClick: Long = 1,
    val commitsPerSecond: Long = 0,
    val soundEnabled: Boolean = true,
    val vfxEnabled: Boolean = true,
    val upgrades: List<Upgrade> = listOf(
        Upgrade(id = "coffee", name = "Café", description = "+1 commit por clic", iconEmoji = "☕", baseCost = 15, commitsPerClickAdded = 1),
        Upgrade(id = "intern", name = "Becario", description = "+2 commits por segundo", iconEmoji = "🧑‍💻", baseCost = 50, commitsPerSecondAdded = 2),
        Upgrade(id = "keyboard", name = "Teclado Mecánico", description = "+5 commits por clic", iconEmoji = "⌨️", baseCost = 100, commitsPerClickAdded = 5),
        Upgrade(id = "stackoverflow", name = "StackOverflow", description = "+10 commits por segundo", iconEmoji = "📚", baseCost = 500, commitsPerSecondAdded = 10),
        Upgrade(id = "copilot", name = "Copilot", description = "+50 commits por segundo", iconEmoji = "🤖", baseCost = 2500, commitsPerSecondAdded = 50)
    )
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var timeSinceLastSave = 0

    init {
        loadInitialData()
        startGameLoop()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val snapshot = dataStoreManager.gameStateSnapshot.first()
            val savedCommits = snapshot.first
            val savedLevels = snapshot.second
            
            val sound = dataStoreManager.soundEnabledFlow.first()
            val vfx = dataStoreManager.vfxEnabledFlow.first()

            _uiState.update { state ->
                var currentCps = 0L
                var currentCpc = 1L
                val loadedUpgrades = state.upgrades.map { upgrade ->
                    val level = savedLevels[upgrade.id] ?: 0
                    currentCps += (upgrade.commitsPerSecondAdded * level)
                    currentCpc += (upgrade.commitsPerClickAdded * level)
                    upgrade.copy(level = level)
                }

                state.copy(
                    isLoaded = true,
                    commits = savedCommits,
                    commitsPerSecond = currentCps,
                    commitsPerClick = currentCpc,
                    upgrades = loadedUpgrades,
                    soundEnabled = sound,
                    vfxEnabled = vfx
                )
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isLoaded) continue

                val cps = _uiState.value.commitsPerSecond
                if (cps > 0) {
                    _uiState.update { it.copy(commits = it.commits + cps) }
                }

                timeSinceLastSave++
                // Guardamos en disco cada 3 segundos si ha habido cambios
                if (timeSinceLastSave >= 3) {
                    dataStoreManager.saveCommits(_uiState.value.commits)
                    timeSinceLastSave = 0
                }
            }
        }
    }

    fun onMainButtonClick() {
        if (!_uiState.value.isLoaded) return
        _uiState.update { it.copy(commits = it.commits + it.commitsPerClick) }
    }

    fun buyUpgrade(upgradeId: String) {
        _uiState.update { state ->
            val upgradeIndex = state.upgrades.indexOfFirst { it.id == upgradeId }
            if (upgradeIndex == -1) return@update state

            val upgrade = state.upgrades[upgradeIndex]
            if (state.commits >= upgrade.currentCost) {
                val newCommits = state.commits - upgrade.currentCost
                val newLevel = upgrade.level + 1
                val updatedUpgrade = upgrade.copy(level = newLevel)
                
                val newUpgrades = state.upgrades.toMutableList().apply {
                    set(upgradeIndex, updatedUpgrade)
                }

                // Guardado inmediato en DataStore al hacer una compra
                viewModelScope.launch {
                    dataStoreManager.saveCommits(newCommits)
                    dataStoreManager.saveUpgradeLevel(upgradeId, newLevel)
                }

                state.copy(
                    commits = newCommits,
                    commitsPerClick = state.commitsPerClick + upgrade.commitsPerClickAdded,
                    commitsPerSecond = state.commitsPerSecond + upgrade.commitsPerSecondAdded,
                    upgrades = newUpgrades
                )
            } else {
                state // Not enough commits
            }
        }
    }

    // Funciones de Ajustes
    fun toggleSound() {
        viewModelScope.launch {
            val newState = !_uiState.value.soundEnabled
            dataStoreManager.setSoundEnabled(newState)
            _uiState.update { it.copy(soundEnabled = newState) }
        }
    }

    fun toggleVfx() {
        viewModelScope.launch {
            val newState = !_uiState.value.vfxEnabled
            dataStoreManager.setVfxEnabled(newState)
            _uiState.update { it.copy(vfxEnabled = newState) }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            dataStoreManager.clearAllData()
            
            // Reiniciar estado en memoria
            _uiState.update { state ->
                val resetUpgrades = state.upgrades.map { it.copy(level = 0) }
                state.copy(
                    commits = 0,
                    commitsPerClick = 1,
                    commitsPerSecond = 0,
                    upgrades = resetUpgrades,
                    soundEnabled = true,
                    vfxEnabled = true
                )
            }
            timeSinceLastSave = 0
        }
    }
}
