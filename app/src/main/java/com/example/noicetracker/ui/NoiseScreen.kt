package com.example.noicetracker.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noicetracker.R
import com.example.noicetracker.audio.Decibels
import com.example.noicetracker.measurement.MeasurementStats
import com.example.noicetracker.ui.components.NoiseChart
import com.example.noicetracker.ui.components.StatsRow
import com.example.noicetracker.ui.theme.NoiceTrackerTheme
import kotlin.math.sin

@Composable
fun NoiseScreen(
    modifier: Modifier = Modifier,
    viewModel: NoiseViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.start() else viewModel.onPermissionDenied()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.pause() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.resume() }

    NoiseContent(
        state = state,
        onStartClick = {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.start()
            else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        },
        onStopClick = viewModel::stop,
        modifier = modifier,
    )
}

@Composable
fun NoiseContent(
    state: NoiseUiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = NoiseLevel.fromDb(state.currentDb)
    val readingColor =
        if (state.isRunning) level.color else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.db_value, state.currentDb),
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = readingColor
        )
        Text(
            text = stringResource(
                if (state.isRunning) level.labelRes else R.string.measurement_stopped
            ),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { (state.currentDb / Decibels.MAX_DB).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = readingColor
        )

        Spacer(Modifier.height(24.dp))

        StatsRow(stats = state.stats)

        Spacer(Modifier.height(24.dp))

        NoiseChart(
            values = state.history,
            maxPoints = NoiseViewModel.HISTORY_SIZE,
            maxDb = Decibels.MAX_DB.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(8.dp)
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onStartClick, enabled = !state.isRunning) {
                Text(stringResource(R.string.button_start))
            }
            OutlinedButton(onClick = onStopClick, enabled = state.isRunning) {
                Text(stringResource(R.string.button_stop))
            }
        }

        state.errorRes?.let {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(it), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoiseContentPreview() {
    val samples = List(60) { i -> 50.0 + 15 * sin(i / 5.0) }
    NoiceTrackerTheme {
        NoiseContent(
            state = NoiseUiState(
                isRunning = true,
                currentDb = samples.last(),
                history = samples.map { it.toFloat() },
                stats = samples.fold(MeasurementStats()) { stats, db -> stats.add(db) },
            ),
            onStartClick = {},
            onStopClick = {},
        )
    }
}
