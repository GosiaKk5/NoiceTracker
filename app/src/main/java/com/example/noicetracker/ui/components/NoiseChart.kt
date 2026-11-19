package com.example.noicetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private const val GRID_STEP_DB = 20

@Composable
fun NoiseChart(
    values: List<Float?>,
    maxPoints: Int,
    maxDb: Float,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        fun yFor(db: Float) = size.height * (1f - db.coerceIn(0f, maxDb) / maxDb)

        for (db in GRID_STEP_DB until maxDb.toInt() step GRID_STEP_DB) {
            val y = yFor(db.toFloat())
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        if (values.size < 2) return@Canvas

        val stepX = size.width / (maxPoints - 1)
        val path = Path().apply {
            var segmentStarted = false
            values.forEachIndexed { i, db ->
                if (db == null) {
                    segmentStarted = false
                    return@forEachIndexed
                }
                val x = i * stepX
                if (segmentStarted) lineTo(x, yFor(db)) else moveTo(x, yFor(db))
                segmentStarted = true
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx()))
    }
}
