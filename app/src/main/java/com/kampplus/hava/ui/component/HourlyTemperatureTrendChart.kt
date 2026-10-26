package com.kampplus.hava.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class HourlyTrendPoint(
    val hourLabel: String,
    val temperatureC: Double
)

@Composable
fun HourlyTemperatureTrendChart(
    points: List<HourlyTrendPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val minTemp = remember(points) { points.minOf { it.temperatureC } }
    val maxTemp = remember(points) { points.maxOf { it.temperatureC } }
    val tempRange = remember(minTemp, maxTemp) {
        if (maxTemp == minTemp) 1.0 else maxTemp - minTemp
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val widthPerItem = size.width / points.size.coerceAtLeast(1)
        val strokeWidthPx = 2.dp.toPx()
        val path = Path()

        points.forEachIndexed { index, point ->
            val x = (index * widthPerItem) + (widthPerItem / 2f)
            val normalizedTemp = (point.temperatureC - minTemp) / tempRange
            val y = size.height - (normalizedTemp * (size.height - 24.dp.toPx()) + 12.dp.toPx()).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = ((index - 1) * widthPerItem) + (widthPerItem / 2f)
                val prevNormalizedTemp = (points[index - 1].temperatureC - minTemp) / tempRange
                val prevY = size.height - (prevNormalizedTemp * (size.height - 24.dp.toPx()) + 12.dp.toPx()).toFloat()

                val controlX1 = prevX + (x - prevX) / 2f
                val controlY1 = prevY
                val controlX2 = prevX + (x - prevX) / 2f
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }

            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidthPx)
        )
    }
}
