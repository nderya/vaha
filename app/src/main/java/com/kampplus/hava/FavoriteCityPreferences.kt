package com.kampplus.hava.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.kampplus.hava.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "favorite_cities_prefs")

class FavoriteCityPreferences(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val FAVORITE_CITIES_KEY = stringSetPreferencesKey("favorite_cities_json")
    }

    val favoriteCitiesFlow: Flow<Set<City>> = context.dataStore.data.map { prefs ->
        val jsonSet = prefs[FAVORITE_CITIES_KEY] ?: emptySet()
        jsonSet.mapNotNull { json ->
            try {
                gson.fromJson(json, City::class.java)
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    suspend fun toggleFavorite(city: City) {
        context.dataStore.edit { prefs ->
            val currentSet = prefs[FAVORITE_CITIES_KEY] ?: emptySet()
            val cityJson = gson.toJson(city)

            // Şehir id'sine göre kontrol edilir
            val existingJson = currentSet.find {
                try {
                    gson.fromJson(it, City::class.java).id == city.id
                } catch (e: Exception) {
                    false
                }
            }

            val updatedSet = if (existingJson != null) {
                currentSet - existingJson
            } else {
                currentSet + cityJson
            }

            prefs[FAVORITE_CITIES_KEY] = updatedSet
        }
    }
}
