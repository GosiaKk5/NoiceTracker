package com.example.noicetracker.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.noicetracker.R
import com.example.noicetracker.measurement.MeasurementStats

@Composable
fun StatsRow(stats: MeasurementStats, modifier: Modifier = Modifier) {
    fun valueOrNull(value: Double) = value.takeUnless { stats.isEmpty }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(R.string.stat_min, valueOrNull(stats.min))
        StatItem(R.string.stat_average, valueOrNull(stats.average))
        StatItem(R.string.stat_max, valueOrNull(stats.max))
    }
}

@Composable
private fun StatItem(@StringRes labelRes: Int, valueDb: Double?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valueDb?.let { stringResource(R.string.db_value, it) }
                ?: stringResource(R.string.value_placeholder),
            style = MaterialTheme.typography.titleLarge
        )
    }
}
