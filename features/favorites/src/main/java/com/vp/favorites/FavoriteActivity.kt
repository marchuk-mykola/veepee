package com.vp.favorites

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vp.core.ui.ListAdapter
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class FavoriteActivity : DaggerAppCompatActivity(), ListAdapter.OnItemClickListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private lateinit var listAdapter: ListAdapter
    private lateinit var viewAnimator: ViewAnimator
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

    private lateinit var favoriteViewModel: FavoriteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoriteViewModel = ViewModelProviders.of(this, factory)[FavoriteViewModel::class.java]

        setContentView(R.layout.activity_favorite)
        setupViews()
        initList()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        favoriteViewModel.refreshList()
    }

    override fun onItemClick(imdbID: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("app://movies/detail?imdbID=$imdbID")
        }
        startActivity(intent)
    }

    private fun observeViewModel() {
        favoriteViewModel
            .state
            .observe(this) { favoritesViewState ->
                handleResult(listAdapter, favoritesViewState)
            }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        viewAnimator = findViewById(R.id.viewAnimator)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorText)
    }

    private fun initList() {
        listAdapter = ListAdapter().apply {
            setOnItemClickListener(this@FavoriteActivity)
        }
        recyclerView.apply {
            adapter = listAdapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(
                context,
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
            )
        }
    }

    private fun handleResult(listAdapter: ListAdapter, favoritesViewState: FavoriteViewModel.FavoritesViewState) {
        when (favoritesViewState) {
            is FavoriteViewModel.FavoritesViewState.Loaded -> {
                handleLoadedState(listAdapter, favoritesViewState)
            }

            FavoriteViewModel.FavoritesViewState.Loading -> showProgressBar()
            FavoriteViewModel.FavoritesViewState.Error -> showError()
        }
    }

    private fun handleLoadedState(
        listAdapter: ListAdapter,
        favoritesViewState: FavoriteViewModel.FavoritesViewState.Loaded
    ) {
        supportActionBar?.title = getTitleBarText(favoritesViewState.movies.isEmpty())
        listAdapter.setItems(favoritesViewState.movies)
        showList()
    }

    private fun showProgressBar() {
        viewAnimator.setDisplayedChild(viewAnimator.indexOfChild(progressBar))
    }

    private fun showList() {
        viewAnimator.setDisplayedChild(viewAnimator.indexOfChild(recyclerView))
    }

    private fun showError() {
        viewAnimator.setDisplayedChild(viewAnimator.indexOfChild(errorTextView))
    }

    private fun getTitleBarText(isEmptyList: Boolean): String {
        return if (isEmptyList) {
            getString(R.string.no_favorites)
        } else {
            getString(R.string.favorites)
        }
    }

}