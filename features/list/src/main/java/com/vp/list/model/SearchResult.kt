package com.vp.list.model

import com.vp.core.models.ListItem

data class SearchResult(
    val items: List<ListItem>,
    val totalResult: Int,
    val listState: ListState
) {
    companion object {
        fun error(): SearchResult = SearchResult(emptyList(), 0, ListState.ERROR)

        fun success(items: List<ListItem>, totalResult: Int): SearchResult =
            SearchResult(items, totalResult, ListState.LOADED)

        @JvmStatic
        fun inProgress(): SearchResult = SearchResult(emptyList(), 0, ListState.IN_PROGRESS)
    }
}
