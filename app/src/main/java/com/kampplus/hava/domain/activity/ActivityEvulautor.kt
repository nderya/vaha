package com.kampplus.hava.domain.activity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class ActivityType(val displayName: String, val icon: ImageVector) {
    RUNNING("Koşu", Icons.Filled.Star),
    CYCLING("Bisiklet", Icons.Filled.Check),
    GARDENING("Bahçe İşleri", Icons.Filled.Build)
}

enum class ActivityRating(val label: String) {
    EXCELLENT("Çok İyi"),
    GOOD("İyi"),
    FAIR("Orta"),
    POOR("Kötü")
}

data class ActivityEvaluationResult(
    val type: ActivityType,
    val rating: ActivityRating,
    val summary: String
)

fun interface ActivityEvaluator {
    fun evaluate(
        tempC: Double,
        windSpeedKmh: Double,
        humidityPercent: Int,
        rainProb: Int,
        weatherCode: Int
    ): ActivityEvaluationResult
}

object ActivityEvaluatorRegistry {

    // Tehlikeli veya elverişsiz hava durumları (Kar, Fırtına, Dondurucu Yağmur, Şiddetli Sağanak)
    private fun isHazardousWeather(code: Int): Boolean {
        return code in setOf(
            56, 57, // Dondurucu çisenti
            65, 66, 67, // Şiddetli/dondurucu yağmur
            71, 73, 75, 77, // Karlı ve kar taneli
            81, 82, // Şiddetli sağanak
            85, 86, // Kar sağanağı
            95, 96, 99 // Gök gürültülü fırtına ve dolu
        )
    }

    private val evaluators: Map<ActivityType, ActivityEvaluator> = mapOf(
        ActivityType.RUNNING to ActivityEvaluator { tempC, windSpeedKmh, humidityPercent, rainProb, weatherCode ->
            val isHazardous = isHazardousWeather(weatherCode)
            val isTempIdeal = tempC in 10.0..18.0
            val isTempOk = tempC in 6.0..22.0
            val isWindHigh = windSpeedKmh >= 15.0
            val isHumidityHigh = humidityPercent >= 75
            val isRainy = rainProb >= 20

            val rating = when {
                isHazardous -> ActivityRating.POOR
                isTempIdeal && !isWindHigh && !isHumidityHigh && !isRainy -> ActivityRating.EXCELLENT
                isTempOk && windSpeedKmh < 20.0 && humidityPercent < 75 && rainProb < 20 -> ActivityRating.GOOD
                tempC in 5.0..26.0 && windSpeedKmh < 28.0 && rainProb < 45 -> ActivityRating.FAIR
                else -> ActivityRating.POOR
            }

            val summary = when {
                isHazardous -> "Kar, fırtına veya dondurucu yağış nedeniyle dışarıda koşu elverişsiz."
                rating == ActivityRating.EXCELLENT -> "Sıcaklık ve hava koşulları koşu için mükemmel!"
                rating == ActivityRating.GOOD -> "Koşu yapmak için elverişli koşullar."
                rating == ActivityRating.FAIR -> "Koşulabilir ancak hava şartlarına dikkat edin."
                else -> "Sıcaklık, nem veya rüzgâr koşu için uygun değil."
            }

            ActivityEvaluationResult(ActivityType.RUNNING, rating, summary)
        },

        ActivityType.CYCLING to ActivityEvaluator { tempC, windSpeedKmh, _, rainProb, weatherCode ->
            val isHazardous = isHazardousWeather(weatherCode)
            val rating = when {
                isHazardous -> ActivityRating.POOR
                tempC in 14.0..24.0 && windSpeedKmh < 12.0 && rainProb < 10 -> ActivityRating.EXCELLENT
                tempC in 10.0..26.0 && windSpeedKmh < 18.0 && rainProb < 20 -> ActivityRating.GOOD
                tempC in 8.0..30.0 && windSpeedKmh < 25.0 && rainProb < 40 -> ActivityRating.FAIR
                else -> ActivityRating.POOR
            }

            val summary = if (isHazardous) {
                "Zemin kayganlığı, kar veya şiddetli rüzgâr/fırtına nedeniyle bisiklet sürüşü tehlikeli."
            } else when (rating) {
                ActivityRating.EXCELLENT -> "Sürüş için harika ve rüzgârsız bir hava!"
                ActivityRating.GOOD -> "Bisiklet sürmek için elverişli koşullar."
                ActivityRating.FAIR -> "Rüzgâr sürüşü biraz zorlaştırabilir."
                else -> "Elverişsiz hava şartları nedeniyle bisiklet önerilmez."
            }

            ActivityEvaluationResult(ActivityType.CYCLING, rating, summary)
        },

        ActivityType.GARDENING to ActivityEvaluator { tempC, windSpeedKmh, _, rainProb, weatherCode ->
            val isHazardous = isHazardousWeather(weatherCode)
            val rating = when {
                isHazardous -> ActivityRating.POOR
                tempC in 16.0..22.0 && windSpeedKmh < 15.0 && rainProb < 15 -> ActivityRating.EXCELLENT
                tempC in 12.0..26.0 && windSpeedKmh < 22.0 && rainProb < 30 -> ActivityRating.GOOD
                tempC in 8.0..30.0 && rainProb < 50 -> ActivityRating.FAIR
                else -> ActivityRating.POOR
            }

            val summary = if (isHazardous) {
                "Kar, don veya fırtınalı hava nedeniyle açık alanda bahçe işleri yapılamaz."
            } else when (rating) {
                ActivityRating.EXCELLENT -> "Açık havada bahçe işleriyle uğraşmak için ideal zaman."
                ActivityRating.GOOD -> "Bahçe işleri yapılabilir."
                ActivityRating.FAIR -> "Sıcaklığa dikkat ederek çalışabilirsiniz."
                else -> "Uygun olmayan hava koşulları."
            }

            ActivityEvaluationResult(ActivityType.GARDENING, rating, summary)
        }
    )

    fun evaluateAll(
        tempC: Double,
        windSpeedKmh: Double,
        humidityPercent: Int,
        rainProb: Int,
        weatherCode: Int
    ): List<ActivityEvaluationResult> {
        return evaluators.values.map {
            it.evaluate(tempC, windSpeedKmh, humidityPercent, rainProb, weatherCode)
        }
    }
}
