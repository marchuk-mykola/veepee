package com.vp.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MovieListActivity : AppCompatActivity(), HasAndroidInjector {

    companion object {
        private const val IS_SEARCH_VIEW_ICONIFIED = "is_search_view_iconified"
        private const val SEARCH_QUERY_TEXT = "searchQueryText"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    private lateinit var searchView: SearchView
    private lateinit var refreshMenuItem: MenuItem
    private var searchViewExpanded = true
    private var savedQueryText: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        savedInstanceState?.let {
            searchViewExpanded = it.getBoolean(IS_SEARCH_VIEW_ICONIFIED)
            savedQueryText = it.getString(SEARCH_QUERY_TEXT)
        } ?: run {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ListFragment(), ListFragment.TAG)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        searchView = (menu.findItem(R.id.search).actionView as? SearchView) ?: return false
        refreshMenuItem = menu.findItem(R.id.refresh)

        refreshMenuItem.setOnMenuItemClickListener {
            (supportFragmentManager.findFragmentByTag(ListFragment.TAG) as? ListFragment)?.refresh()
            true
        }

        with(searchView) {
            imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
            isIconified = searchViewExpanded
            savedQueryText?.takeIf { it.isNotEmpty() }?.let {
                setQuery(it, false)
                isIconified = false
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    (supportFragmentManager.findFragmentByTag(ListFragment.TAG) as? ListFragment)?.submitSearchQuery(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_SEARCH_VIEW_ICONIFIED, searchView.isIconified)
        outState.putString(SEARCH_QUERY_TEXT, searchView.query.toString())
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

}
