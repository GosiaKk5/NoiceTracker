package com.example.noicetracker.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noicetracker.R
import com.example.noicetracker.audio.NoiseMeter
import com.example.noicetracker.measurement.ExponentialSmoother
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoiseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NoiseUiState())
    val uiState: StateFlow<NoiseUiState> = _uiState.asStateFlow()

    private var measurementJob: Job? = null
    private var smoother = ExponentialSmoother(SMOOTHING_ALPHA)

    fun start() {
        if (_uiState.value.isRunning) return
        smoother = ExponentialSmoother(SMOOTHING_ALPHA)
        _uiState.value = NoiseUiState(isRunning = true)
        launchMeasurement()
    }

    fun stop() {
        cancelMeasurement()
        _uiState.update { it.copy(isRunning = false, isPaused = false) }
    }

    fun pause() {
        if (!_uiState.value.isRunning || _uiState.value.isPaused) return
        cancelMeasurement()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resume() {
        if (!_uiState.value.isPaused) return
        _uiState.update { state ->
            state.copy(isPaused = false, history = state.history.appendLimited(null))
        }
        launchMeasurement()
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(errorRes = R.string.error_no_permission) }
    }

    private fun launchMeasurement() {
        measurementJob = viewModelScope.launch {
            NoiseMeter.measure()
                .catch { e ->
                    Log.e(TAG, "Measurement failed", e)
                    _uiState.update {
                        it.copy(isRunning = false, isPaused = false, errorRes = R.string.error_microphone)
                    }
                }
                .collect { db -> onSample(db) }
        }
    }

    private fun onSample(db: Double) {
        _uiState.update { state ->
            state.copy(
                currentDb = smoother.next(db),
                history = state.history.appendLimited(db.toFloat()),
                stats = state.stats.add(db),
            )
        }
    }

    private fun cancelMeasurement() {
        measurementJob?.cancel()
        measurementJob = null
    }

    private fun List<Float?>.appendLimited(value: Float?) = (this + value).takeLast(HISTORY_SIZE)

    companion object {
        const val HISTORY_SIZE = 100
        private const val SMOOTHING_ALPHA = 0.2
        private const val TAG = "NoiseViewModel"
    }
}
