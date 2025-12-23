package com.elasticrock.keepscreenon.ui.glance

import com.elasticrock.keepscreenon.data.repository.KeepScreenOnRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun keepScreenOnRepository(): KeepScreenOnRepository
}