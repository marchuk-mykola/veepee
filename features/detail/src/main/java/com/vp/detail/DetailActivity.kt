package com.vp.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.vp.detail.databinding.ActivityDetailBinding
import com.vp.detail.viewmodel.DetailsViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class DetailActivity : DaggerAppCompatActivity() {

    companion object {
        const val IMDB_ID = "imdbID"
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private lateinit var detailViewModel: DetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        detailViewModel = ViewModelProviders.of(this, factory)[DetailsViewModel::class.java]
        binding.viewModel = detailViewModel
        binding.lifecycleOwner = this
        detailViewModel.fetchDetails(getMovieId())
        detailViewModel.title.observe(this) {
            supportActionBar?.title = it
        }
        detailViewModel.isFavorite.observe(this) {
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        val favoriteItem = menu?.findItem(R.id.star)
        val isFavorite = detailViewModel.isFavorite.value ?: false
        favoriteItem?.setIcon(if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.star -> {
                detailViewModel.toggleFavorite(getMovieId())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getMovieId(): String {
        return intent?.data?.getQueryParameter(IMDB_ID) ?: run {
            throw IllegalStateException("You must provide movie id to display details")
        }
    }

}
