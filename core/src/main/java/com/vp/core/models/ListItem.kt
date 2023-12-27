package com.vp.core.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ListItem(
    @SerializedName("Title")
    val title: String,
    @SerializedName("Year")
    val year: String,
    @SerializedName("imdbID")
    val imdbID: String,
    @SerializedName("Poster")
    val poster: String
) : Serializable
