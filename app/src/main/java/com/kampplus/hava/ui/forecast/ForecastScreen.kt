package com.kampplus.hava.ui.forecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.domain.activity.ActivityEvaluatorRegistry
import com.kampplus.hava.domain.activity.ActivityType
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.ActivityPagerWidget
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.HourlyTemperatureTrendChart
import com.kampplus.hava.ui.component.HourlyTrendPoint
import com.kampplus.hava.ui.component.SunArcWidget
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(cityName.ifEmpty { stringResource(R.string.detail_title) }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                LoadState.Loading -> CircularProgressIndicator()
                LoadState.Empty -> ErrorView(message = "Bu şehir için tahmin bulunamadı.")
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(subtitle = subtitle, forecast = uiState.data)
            }
        }
    }
}

@Composable
private fun ForecastContent(subtitle: String, forecast: Forecast, modifier: Modifier = Modifier) {
    val currentTemp = forecast.current.temperatureC
    val currentWind = forecast.current.windSpeedKmh ?: 0.0
    val currentHumidity = forecast.current.humidityPercent ?: 0
    val rainProbability = forecast.hourly.firstOrNull()?.precipitationProbability ?: 0
    val currentWeatherCode = forecast.current.weatherCode

    var selectedActivities by remember {
        mutableStateOf(setOf(ActivityType.RUNNING, ActivityType.CYCLING, ActivityType.GARDENING))
    }

    var showDialog by remember { mutableStateOf(false) }

    // Gerçek hava şartlarına ve WMO hava durum koduna göre hassas aktivite değerlendirmesi
    val activityResults = remember(currentTemp, currentWind, currentHumidity, rainProbability, currentWeatherCode, selectedActivities) {
        ActivityEvaluatorRegistry.evaluateAll(
            tempC = currentTemp,
            windSpeedKmh = currentWind,
            humidityPercent = currentHumidity,
            rainProb = rainProbability,
            weatherCode = currentWeatherCode
        ).filter { selectedActivities.contains(it.type) }
    }

    // API'den gelen gerçek Gün Doğumu & Gün Batımı saatleri
    val sunriseTime = forecast.sunriseTime?.toLocalTime() ?: LocalTime.of(6, 30)
    val sunsetTime = forecast.sunsetTime?.toLocalTime() ?: LocalTime.of(18, 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurrentWeatherHeader(
            subtitle = subtitle,
            current = forecast.current,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SectionTitle(stringResource(R.string.detail_hourly))
        HourlyRow(hours = forecast.hourly)

        val points = remember(forecast.hourly) {
            forecast.hourly.map { HourlyTrendPoint(it.time.toString(), it.temperatureC) }
        }
        HourlyTemperatureTrendChart(
            points = points,
            lineColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SunArcWidget(
            sunrise = sunriseTime,
            sunset = sunsetTime,
            currentTime = LocalTime.now(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        ActivityPagerWidget(
            evaluations = activityResults,
            onActivityClick = { showDialog = true },
            onSettingsClick = { showDialog = true },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SectionTitle(stringResource(R.string.detail_daily))
        Card(modifier = Modifier.padding(horizontal = 16.dp)) {
            forecast.daily.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider()
                DailyRow(day = day, isToday = index == 0)
            }
        }
    }

    if (showDialog) {
        ActivitySelectionDialog(
            currentlySelected = selectedActivities,
            onDismiss = { showDialog = false },
            onSave = { updatedSet ->
                selectedActivities = updatedSet
                showDialog = false
            }
        )
    }
}

@Composable
private fun ActivitySelectionDialog(
    currentlySelected: Set<ActivityType>,
    onDismiss: () -> Unit,
    onSave: (Set<ActivityType>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(currentlySelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aktivite Tahminleri") },
        text = {
            Column {
                Text(
                    text = "Ekranınızda görünecek favori aktivitelerinizi seçin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(ActivityType.entries) { activity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = tempSelected.contains(activity),
                                onCheckedChange = { isChecked ->
                                    tempSelected = if (isChecked) {
                                        tempSelected + activity
                                    } else {
                                        tempSelected - activity
                                    }
                                }
                            )
                            Icon(
                                imageVector = activity.icon,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp, end = 12.dp)
                            )
                            Text(text = activity.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(tempSelected) }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun CurrentWeatherHeader(subtitle: String, current: CurrentWeather, modifier: Modifier = Modifier) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = condition.emoji, style = MaterialTheme.typography.displayMedium)
            Column {
                Text(text = degrees(current.temperatureC), style = MaterialTheme.typography.displaySmall)
                Text(text = condition.label, style = MaterialTheme.typography.titleMedium)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                current.apparentTemperatureC?.let {
                    Metric(stringResource(R.string.detail_feels_like), degrees(it))
                }
                current.humidityPercent?.let {
                    Metric(stringResource(R.string.detail_humidity), "%$it")
                }
                current.windSpeedKmh?.let {
                    Metric(stringResource(R.string.detail_wind), "${it.roundToInt()} km/sa")
                }
            }
        }
    }
}

@Composable
private fun HourlyRow(hours: List<HourlyForecast>, modifier: Modifier = Modifier) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT)
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            Card(modifier = Modifier.width(76.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = hour.time.format(HOUR_FORMAT), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = WeatherCondition.fromWeatherCode(hour.weatherCode).emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = degrees(hour.temperatureC), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = percent(hour.precipitationProbability) ?: " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyForecast, isToday: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percent(day.precipitationProbability).orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = WeatherCondition.fromWeatherCode(day.weatherCode).emoji,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"
private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private const val HOURLY_COUNT = 24
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))
