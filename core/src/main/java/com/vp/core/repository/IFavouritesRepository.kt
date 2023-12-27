package com.vp.core.repository

import com.vp.core.service.DetailService
import android.content.Context
import com.vp.core.mappers.toListItem
import com.vp.core.models.ListItem
import com.vp.core.models.MovieDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

interface IFavoritesRepository {
    fun isFavorite(movieId: String): Boolean
    fun addFavorite(movieId: String)
    fun removeFavorite(movieId: String)
    fun getFavoriteMoviesIds(): Set<String>

    fun fetchMovies(
        onSuccess: (List<ListItem>) -> Unit,
        onFailure: () -> Unit
    )
}

internal class FavoritesRepository @Inject constructor(
    context: Context,
    private val detailService: DetailService
) : IFavoritesRepository {

    private val sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)

    override fun isFavorite(movieId: String): Boolean {
        return getFavorites().contains(movieId)
    }

    override fun addFavorite(movieId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(movieId)
        saveFavorites(favorites)
    }

    override fun removeFavorite(movieId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(movieId)
        saveFavorites(favorites)
    }

    override fun getFavoriteMoviesIds(): Set<String> {
        return getFavorites()
    }

    private fun getFavorites(): Set<String> {
        return sharedPreferences.getStringSet("FavoriteMovies", emptySet()) ?: emptySet()
    }

    private fun saveFavorites(favorites: Set<String>) {
        sharedPreferences.edit().putStringSet("FavoriteMovies", favorites).apply()
    }

    override fun fetchMovies(
        onSuccess: (List<ListItem>) -> Unit,
        onFailure: () -> Unit
    ) {
        val movieIds = getFavoriteMoviesIds()
        val countDownLatch = CountDownLatch(movieIds.size)
        val allMoviesDetails: MutableList<ListItem> = Collections.synchronizedList(mutableListOf())
        var failed = false

        movieIds.forEach { movieId ->
            detailService.getMovie(movieId).enqueue(object : Callback<MovieDetail> {
                override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                    if (response.isSuccessful) {
                        response.body()?.let { movieDetail ->
                            allMoviesDetails.add(movieDetail.toListItem(movieId))
                        }
                    } else {
                        failed = true
                    }
                    countDownLatch.countDown()
                }

                override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                    failed = true
                    countDownLatch.countDown()
                }
            })
        }

        Thread {
            countDownLatch.await()
            if (!failed) {
                onSuccess(allMoviesDetails)
            } else {
                onFailure()
            }
        }.start()
    }

}
