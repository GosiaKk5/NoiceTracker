package com.example.noicetracker.audio

import kotlin.math.log10
import kotlin.math.sqrt

object Decibels {
    const val MIN_DB = 0.0
    const val MAX_DB = 120.0
    private const val OFFSET_DB = 90.0

    fun fromPcm16(samples: ShortArray, count: Int = samples.size): Double {
        if (count <= 0) return MIN_DB

        var sumSquares = 0.0
        for (i in 0 until count) {
            val sample = samples[i].toDouble()
            sumSquares += sample * sample
        }
        val rms = sqrt(sumSquares / count)
        if (rms < 1.0) return MIN_DB

        val dbfs = 20 * log10(rms / Short.MAX_VALUE)
        return (dbfs + OFFSET_DB).coerceIn(MIN_DB, MAX_DB)
    }
}
