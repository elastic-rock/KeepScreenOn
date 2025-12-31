package com.elasticrock.keepscreenon.tasker

import com.elasticrock.keepscreenon.data.repository.KeepScreenOnRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TaskerEntryPoint {
    fun keepScreenOnRepository(): KeepScreenOnRepository
}