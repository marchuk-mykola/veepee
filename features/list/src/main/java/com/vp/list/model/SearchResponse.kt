package com.vp.list.model

import com.google.gson.annotations.SerializedName
import com.vp.core.models.ListItem

data class SearchResponse(
    @SerializedName("Response")
    private val response: String,
    @SerializedName("Search")
    val search: List<ListItem>? = null
) {
    val totalResults: Int = 0

    fun hasResponse(): Boolean = response == POSITIVE_RESPONSE

    companion object {
        private const val POSITIVE_RESPONSE = "True"
    }
}

