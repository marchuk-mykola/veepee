package com.vp.core.mappers

import com.vp.core.models.MovieDetail

fun MovieDetail.toListItem(imdbID: String) =
    com.vp.core.models.ListItem(
        title = this.title,
        year = this.year,
        imdbID = imdbID,
        poster = this.poster
    )