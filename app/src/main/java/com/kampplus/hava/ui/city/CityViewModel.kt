package com.kampplus.hava.ui.city

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.FavoriteCityPreferences
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class CityUiState(
    val query: String = "",
    val loadState: LoadState<List<City>> = LoadState.Empty,
    val favoriteCities: Set<City> = emptySet()
)

class CityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: CityRepository = OpenMeteoCityRepository()
    private val favoritePrefs = FavoriteCityPreferences(application)

    private val _query = MutableStateFlow("")
    private val _loadState = MutableStateFlow<LoadState<List<City>>>(LoadState.Empty)

    val uiState: StateFlow<CityUiState> = combine(
        _query,
        _loadState,
        favoritePrefs.favoriteCitiesFlow
    ) { query, loadState, favorites ->
        CityUiState(
            query = query,
            loadState = loadState,
            favoriteCities = favorites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CityUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _loadState.value = LoadState.Empty
        } else if (newQuery.length >= 2) {
            search()
        }
    }

    fun search() {
        if (_query.value.isBlank()) return
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            _loadState.value = try {
                val cities = repository.search(_query.value)
                if (cities.isEmpty()) LoadState.Empty else LoadState.Success(cities)
            } catch (e: IOException) {
                Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
        }
    }

    fun toggleFavorite(city: City) {
        viewModelScope.launch {
            favoritePrefs.toggleFavorite(city)
        }
    }

    private companion object {
        const val TAG = "CityViewModel"
    }
}
