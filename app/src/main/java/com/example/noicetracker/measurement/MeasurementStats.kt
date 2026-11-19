package com.example.noicetracker.measurement

import kotlin.math.log10
import kotlin.math.pow

data class MeasurementStats(
    val min: Double = 0.0,
    val max: Double = 0.0,
    val sampleCount: Int = 0,
    private val energySum: Double = 0.0,
) {
    val isEmpty: Boolean get() = sampleCount == 0

    val average: Double
        get() = if (isEmpty) 0.0 else 10 * log10(energySum / sampleCount)

    fun add(db: Double): MeasurementStats = MeasurementStats(
        min = if (isEmpty) db else minOf(min, db),
        max = if (isEmpty) db else maxOf(max, db),
        sampleCount = sampleCount + 1,
        energySum = energySum + 10.0.pow(db / 10),
    )
}
