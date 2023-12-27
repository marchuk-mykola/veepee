package com.vp.core.di

import com.vp.core.service.DetailService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class DetailNetworkModule {
    @Provides
    fun providesDetailService(retrofit: Retrofit): DetailService {
        return retrofit.create(DetailService::class.java)
    }
}