package com.productivityapp.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val isDarkMode: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.DARK_MODE] ?: false
        }

    val lastGuidedProjectId: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_GUIDED_PROJECT_ID]
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun setLastGuidedProjectId(projectId: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_GUIDED_PROJECT_ID] = projectId
        }
    }

    // Template customizations
    val customDurations: Flow<Map<String, Int>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> 
            val durationsJson = prefs[PreferencesKeys.TEMPLATE_CUSTOM_DURATIONS] ?: "{}"
            try {
                val obj = org.json.JSONObject(durationsJson)
                val map = mutableMapOf<String, Int>()
                obj.keys().forEach { key -> map[key] = obj.getInt(key) }
                map
            } catch (e: Exception) { emptyMap() }
        }

    val customPrerequisites: Flow<Map<String, List<Int>>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val prereqsJson = prefs[PreferencesKeys.TEMPLATE_CUSTOM_PREREQUISITES] ?: "{}"
            try {
                val obj = org.json.JSONObject(prereqsJson)
                val map = mutableMapOf<String, List<Int>>()
                obj.keys().forEach { key -> 
                    val arr = obj.getJSONArray(key)
                    val list = (0 until arr.length()).map { arr.getInt(it) }
                    map[key] = list
                }
                map
            } catch (e: Exception) { emptyMap() }
        }

    suspend fun saveTemplateCustomizations(durations: Map<String, Int>, prerequisites: Map<String, List<Int>>) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.TEMPLATE_CUSTOM_DURATIONS] = org.json.JSONObject(durations.mapValues { it.value }).toString()
            prefs[PreferencesKeys.TEMPLATE_CUSTOM_PREREQUISITES] = org.json.JSONObject(prerequisites.mapValues { 
                org.json.JSONArray(it.value) 
            }).toString()
        }
    }

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LAST_GUIDED_PROJECT_ID = longPreferencesKey("last_guided_project_id")
        val TEMPLATE_CUSTOM_DURATIONS = stringPreferencesKey("template_custom_durations")
        val TEMPLATE_CUSTOM_PREREQUISITES = stringPreferencesKey("template_custom_prerequisites")
    }
}
