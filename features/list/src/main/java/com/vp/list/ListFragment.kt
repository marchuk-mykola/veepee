package com.vp.list

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vp.core.ui.ListAdapter
import com.vp.list.GridPagingScrollListener.LoadMoreItemsListener
import com.vp.list.model.ListState
import com.vp.list.viewmodel.ListViewModel
import com.vp.list.model.SearchResult
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ListFragment : Fragment(), LoadMoreItemsListener, ListAdapter.OnItemClickListener {

    companion object {
        const val TAG = "ListFragment"
        private const val CURRENT_QUERY = "current_query"
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var listViewModel: ListViewModel
    private lateinit var gridPagingScrollListener: GridPagingScrollListener
    private lateinit var listAdapter: ListAdapter
    private lateinit var viewAnimator: ViewAnimator
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private var currentQuery: String = "Interview"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        listViewModel = ViewModelProvider(this, factory)[ListViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        currentQuery = savedInstanceState?.getString(CURRENT_QUERY) ?: currentQuery
        initBottomNavigation(view)
        initList()
        observeMovies()
        listViewModel.searchMoviesByTitle(currentQuery, 1)
        showProgressBar()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_QUERY, currentQuery)
    }

    override fun loadMoreItems(page: Int) {
        gridPagingScrollListener.markLoading(true)
        listViewModel.searchMoviesByTitle(currentQuery, page)
        listAdapter.showFooter(true)
    }

    override fun onItemClick(imdbID: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("app://movies/detail?imdbID=$imdbID")
        }
        startActivity(intent)
    }

    fun submitSearchQuery(query: String) {
        currentQuery = query
        listAdapter.clearItems()
        listViewModel.searchMoviesByTitle(query, 1)
        showProgressBar()
    }

    fun refresh() {
        listAdapter.clearItems()
        listViewModel.searchMoviesByTitle(currentQuery, 1)
        showProgressBar()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        viewAnimator = view.findViewById(R.id.viewAnimator)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.errorText)
    }

    private fun initBottomNavigation(view: View) {
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.favorites -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://movies/favorites"))
                    intent.setPackage(requireContext().packageName)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun initList() {
        listAdapter = ListAdapter().apply {
            setOnItemClickListener(this@ListFragment)
        }
        recyclerView.apply {
            adapter = listAdapter
            setHasFixedSize(true)
            layoutManager = setupGridLayoutManager().also {
                gridPagingScrollListener = GridPagingScrollListener(it).apply {
                    setLoadMoreItemsListener(this@ListFragment)
                }
                addOnScrollListener(gridPagingScrollListener)
            }
        }
    }

    private fun setupGridLayoutManager(): GridLayoutManager {
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3

        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (listAdapter.getItemViewType(position) == ListAdapter.VIEW_TYPE_FOOTER) {
                    spanCount
                } else {
                    1
                }
            }
        }

        return gridLayoutManager
    }


    private fun observeMovies() {
        listViewModel.observeMovies().observe(viewLifecycleOwner) { searchResult ->
            searchResult?.let { handleResult(listAdapter, it) }
        }
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

    private fun handleResult(listAdapter: ListAdapter, searchResult: SearchResult) {
        when (searchResult.listState) {
            ListState.LOADED -> {
                setItemsData(listAdapter, searchResult)
                listAdapter.showFooter(false)
                showList()
            }

            ListState.IN_PROGRESS -> {
                showProgressBar()
                listAdapter.showFooter(true)
            }

            ListState.ERROR -> {
                listAdapter.showFooter(false)
                showError()
            }
        }
        gridPagingScrollListener.markLoading(false)
    }

    private fun setItemsData(listAdapter: ListAdapter, searchResult: SearchResult) {
        listAdapter.setItems(searchResult.items)
        if (searchResult.totalResult <= listAdapter.itemCount) {
            gridPagingScrollListener.markLastPage(true)
        }
    }

}
