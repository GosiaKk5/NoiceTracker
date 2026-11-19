package com.example.noicetracker.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

object NoiseMeter {
    private const val SAMPLE_RATE = 44_100
    private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
    private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private const val SAMPLES_PER_READ = SAMPLE_RATE / 10
    private const val BYTES_PER_SAMPLE = 2

    @SuppressLint("MissingPermission")
    fun measure(): Flow<Double> = flow {
        val minBufferBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        val bufferBytes = maxOf(minBufferBytes, SAMPLES_PER_READ * BYTES_PER_SAMPLE * 2)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL,
            ENCODING,
            bufferBytes
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            throw IllegalStateException("AudioRecord initialization failed")
        }

        val buffer = ShortArray(SAMPLES_PER_READ)
        try {
            recorder.startRecording()
            while (currentCoroutineContext().isActive) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) emit(Decibels.fromPcm16(buffer, read))
            }
        } finally {
            if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop()
            recorder.release()
        }
    }.flowOn(Dispatchers.IO)
}
