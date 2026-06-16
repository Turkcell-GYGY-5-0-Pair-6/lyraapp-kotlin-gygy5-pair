package com.turkcell.lyraapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

//datastore oluşturma Telefon içinde oluşan dosya:theme_preferences.preferences_pb

//DataStore'da değerler key ile tutulur.
private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

interface ThemePreferenceRepository {
    val isDarkTheme: Flow<Boolean>
    suspend fun setDarkTheme(enabled: Boolean)
}

@Singleton
class ThemePreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ThemePreferenceRepository {

    //DataStore'daki veriyi dinler.
    override val isDarkTheme: Flow<Boolean> = //flow: değer değişirse haber ver
        context.themeDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DARK_THEME_KEY] ?: true
            }

    //Kullanıcı koyu tema seçerse DataStore'a kaydediliyor.
    override suspend fun setDarkTheme(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }
}
