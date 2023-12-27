package com.vp.core.di

import android.app.Application
import com.vp.core.service.DetailService
import com.vp.core.repository.FavoritesRepository
import com.vp.core.repository.IFavoritesRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FavoritesRepositoryModule {

    @Singleton
    @Provides
    fun provideFavoritesRepository(
        application: Application,
        detailService: DetailService
    ): IFavoritesRepository {
        return FavoritesRepository(application, detailService)
    }

}