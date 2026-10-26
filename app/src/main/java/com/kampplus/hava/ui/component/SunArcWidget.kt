package com.kampplus.hava.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunArcWidget(
    sunrise: LocalTime,
    sunset: LocalTime,
    currentTime: LocalTime,
    modifier: Modifier = Modifier
) {
    val progress = calculateSunProgress(sunrise, sunset, currentTime)

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gün doğumu ve batımı",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val trackColor = MaterialTheme.colorScheme.outlineVariant
                val activeColor = MaterialTheme.colorScheme.primary

                Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                    val arcRadius = size.width / 2.5f
                    val strokePx = 3.dp.toPx()
                    val center = Offset(size.width / 2f, size.height)

                    drawArc(
                        color = trackColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    val angleInDegrees = 180f + (progress * 180f)
                    val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
                    val sunX = center.x + arcRadius * cos(angleInRadians).toFloat()
                    val sunY = center.y + arcRadius * sin(angleInRadians).toFloat()

                    drawArc(
                        color = activeColor,
                        startAngle = 180f,
                        sweepAngle = progress * 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    drawCircle(
                        color = activeColor,
                        radius = 7.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Gün doğumu", style = MaterialTheme.typography.labelSmall)
                    Text(text = sunrise.toString(), style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Gün batımı", style = MaterialTheme.typography.labelSmall)
                    Text(text = sunset.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

private fun calculateSunProgress(sunrise: LocalTime, sunset: LocalTime, current: LocalTime): Float {
    if (current.isBefore(sunrise)) return 0f
    if (current.isAfter(sunset)) return 1f
    val totalMinutes = Duration.between(sunrise, sunset).toMinutes().toFloat()
    val elapsedMinutes = Duration.between(sunrise, current).toMinutes().toFloat()
    return (elapsedMinutes / totalMinutes).coerceIn(0f, 1f)
}
