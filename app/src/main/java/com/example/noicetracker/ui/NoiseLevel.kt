package com.example.noicetracker.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.noicetracker.R
import com.example.noicetracker.ui.theme.NoiseHigh
import com.example.noicetracker.ui.theme.NoiseLow
import com.example.noicetracker.ui.theme.NoiseMedium

enum class NoiseLevel(
    val upperBoundDb: Double,
    @StringRes val labelRes: Int,
    val color: Color,
) {
    QUIET(40.0, R.string.level_quiet, NoiseLow),
    MODERATE(60.0, R.string.level_moderate, NoiseLow),
    LOUD(80.0, R.string.level_loud, NoiseMedium),
    VERY_LOUD(Double.POSITIVE_INFINITY, R.string.level_very_loud, NoiseHigh);

    companion object {
        fun fromDb(db: Double): NoiseLevel = entries.first { db < it.upperBoundDb }
    }
}
