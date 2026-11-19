package com.example.noicetracker.measurement

class ExponentialSmoother(private val alpha: Double) {
    init {
        require(alpha in 0.0..1.0) { "alpha must be in [0, 1], was $alpha" }
    }

    private var current: Double? = null

    fun next(sample: Double): Double {
        val smoothed = current?.let { it + alpha * (sample - it) } ?: sample
        current = smoothed
        return smoothed
    }
}
