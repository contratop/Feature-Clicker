package com.contratop.featureclicker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_settings")

class DataStoreManager(private val context: Context) {
    
    companion object {
        val COMMITS_KEY = longPreferencesKey("commits")
        
        // Settings
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VFX_ENABLED = booleanPreferencesKey("vfx_enabled")
        
        fun getUpgradeLevelKey(upgradeId: String): Preferences.Key<Int> {
            return intPreferencesKey("upgrade_level_$upgradeId")
        }
    }

    // Commits logic
    val commitsFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[COMMITS_KEY] ?: 0L
        }

    suspend fun saveCommits(commits: Long) {
        context.dataStore.edit { preferences ->
            preferences[COMMITS_KEY] = commits
        }
    }

    // Upgrades logic
    fun getUpgradeLevelFlow(upgradeId: String): Flow<Int> {
        val key = getUpgradeLevelKey(upgradeId)
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    suspend fun saveUpgradeLevel(upgradeId: String, level: Int) {
        val key = getUpgradeLevelKey(upgradeId)
        context.dataStore.edit { preferences ->
            preferences[key] = level
        }
    }
    
    // Initial Load - Get all data as a single object snapshot
    val gameStateSnapshot: Flow<Pair<Long, Map<String, Int>>> = context.dataStore.data
        .map { preferences ->
            val commits = preferences[COMMITS_KEY] ?: 0L
            
            // Reconstruct the map of levels. We need a list of IDs to check.
            val upgradeIds = listOf("coffee", "intern", "keyboard", "stackoverflow", "copilot")
            val levelsMap = upgradeIds.associateWith { id ->
                preferences[getUpgradeLevelKey(id)] ?: 0
            }
            Pair(commits, levelsMap)
        }

    // Settings
    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }
    val vfxEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[VFX_ENABLED] ?: true }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = enabled }
    }

    suspend fun setVfxEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VFX_ENABLED] = enabled }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
