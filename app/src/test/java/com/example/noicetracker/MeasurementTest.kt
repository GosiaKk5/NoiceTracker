package com.example.noicetracker

import com.example.noicetracker.audio.Decibels
import com.example.noicetracker.measurement.ExponentialSmoother
import com.example.noicetracker.measurement.MeasurementStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasurementTest {

    @Test
    fun silenceIsMinDb() {
        assertEquals(Decibels.MIN_DB, Decibels.fromPcm16(ShortArray(100)), 0.0)
    }

    @Test
    fun fullScaleSignalIsOffsetDb() {
        val samples = ShortArray(100) { if (it % 2 == 0) Short.MAX_VALUE else (-Short.MAX_VALUE).toShort() }
        assertEquals(90.0, Decibels.fromPcm16(samples), 0.01)
    }

    @Test
    fun louderSignalGivesHigherDb() {
        val quiet = ShortArray(100) { 100 }
        val loud = ShortArray(100) { 10_000 }
        assertTrue(Decibels.fromPcm16(loud) > Decibels.fromPcm16(quiet))
    }

    @Test
    fun statsTrackMinMaxAndEnergyAverage() {
        val stats = listOf(50.0, 60.0, 70.0).fold(MeasurementStats()) { s, db -> s.add(db) }
        assertEquals(50.0, stats.min, 0.0)
        assertEquals(70.0, stats.max, 0.0)
        assertTrue(stats.average > 60.0 && stats.average < 70.0)
    }

    @Test
    fun smootherMovesTowardsSample() {
        val smoother = ExponentialSmoother(alpha = 0.5)
        assertEquals(40.0, smoother.next(40.0), 0.0)
        assertEquals(50.0, smoother.next(60.0), 0.0)
    }
}
