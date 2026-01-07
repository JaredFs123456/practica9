package com.example.practica9

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "hidratacion_prefs")

object HidratacionStore {
    private val KEY_VASOS = intPreferencesKey("vasos_hoy")
    private val KEY_EVENTOS = stringPreferencesKey("eventos")

    fun vasosFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[KEY_VASOS] ?: 0 }

    fun eventosFlow(context: Context): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[KEY_EVENTOS] ?: ""
            if (raw.isBlank()) emptyList() else raw.split("\n")
        }

    suspend fun setVasos(context: Context, value: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_VASOS] = value }
    }

    suspend fun setEventos(context: Context, eventos: List<String>) {
        context.dataStore.edit { prefs -> prefs[KEY_EVENTOS] = eventos.joinToString("\n") }
    }

    suspend fun clearAll(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VASOS] = 0
            prefs[KEY_EVENTOS] = ""
        }
    }
}
