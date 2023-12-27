package com.vp.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vp.core.models.ListItem
import com.vp.core.repository.IFavoritesRepository
import javax.inject.Inject

class FavoriteViewModel @Inject constructor(
    private val favoritesRepository: IFavoritesRepository
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesViewState>(FavoritesViewState.Loading)
    val state: LiveData<FavoritesViewState> = _state

    fun refreshList() {
        if (shouldSkipUpdate()) return
        updateStateBasedOnFavorites()
    }

    private fun shouldSkipUpdate(): Boolean {
        val currentState = _state.value

        return currentState is FavoritesViewState.Loaded &&
              favoritesRepository.getFavoriteMoviesIds() == currentState.listOfIds
    }

    private fun updateStateBasedOnFavorites() {
        val favorites = favoritesRepository.getFavoriteMoviesIds()
        if (favorites.isEmpty()) {
            _state.postValue(FavoritesViewState.Loaded(emptyList()))
        } else {
            _state.postValue(FavoritesViewState.Loading)
            favoritesRepository.fetchMovies(
                onSuccess = { movies -> _state.postValue(FavoritesViewState.Loaded(movies)) },
                onFailure = { _state.postValue(FavoritesViewState.Error) }
            )
        }
    }

    sealed class FavoritesViewState {
        object Loading : FavoritesViewState()
        data class Loaded(val movies: List<ListItem>) : FavoritesViewState() {
            val listOfIds = movies.map { it.imdbID }.toSet()
        }

        object Error : FavoritesViewState()
    }
}
