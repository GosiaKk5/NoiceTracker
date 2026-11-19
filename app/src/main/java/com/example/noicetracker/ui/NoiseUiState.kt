package com.example.noicetracker.ui

import androidx.annotation.StringRes
import com.example.noicetracker.measurement.MeasurementStats

data class NoiseUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentDb: Double = 0.0,
    val history: List<Float?> = emptyList(),
    val stats: MeasurementStats = MeasurementStats(),
    @StringRes val errorRes: Int? = null,
)
