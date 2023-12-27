package com.vp.detail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vp.core.models.MovieDetail
import com.vp.core.repository.IFavoritesRepository
import com.vp.core.service.DetailService
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.security.auth.callback.Callback

class DetailsViewModel @Inject constructor(
    private val detailService: DetailService,
    private val favoritesRepository: IFavoritesRepository
) : ViewModel() {

    private val _details = MutableLiveData<MovieDetail>()
    val details: LiveData<MovieDetail> = _details

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _loadingState = MutableLiveData(LoadingState.LOADED)
    val loadingState: LiveData<LoadingState> = _loadingState

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    fun toggleFavorite(movieId: String) {
        val currentStatus = _isFavorite.value ?: false
        if (currentStatus) {
            favoritesRepository.removeFavorite(movieId)
        } else {
            favoritesRepository.addFavorite(movieId)
        }
        _isFavorite.value = !currentStatus
    }

    fun fetchDetails(movieId: String) {
        _loadingState.value = LoadingState.IN_PROGRESS
        detailService
            .getMovie(movieId)
            .enqueue(object : Callback, retrofit2.Callback<MovieDetail> {
                override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                    _details.postValue(response.body())

                    response.body()?.title?.let {
                        _title.postValue(it)
                    }
                    _isFavorite.value = favoritesRepository.isFavorite(movieId)

                    _loadingState.value = LoadingState.LOADED
                }

                override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                    _details.postValue(null)
                    _loadingState.value = LoadingState.ERROR
                }
            })
    }

    enum class LoadingState {
        IN_PROGRESS, LOADED, ERROR
    }
}