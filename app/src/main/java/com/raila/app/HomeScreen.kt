package com.raila.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Calendar

@Composable
internal fun HomeScreen(
    repository: TrainRepository,
    lang: AppLanguage,
    direction: Direction,
    defaultStation: String,
    activeTripId: String?,
    activeDestStation: String?,
    activeBoardingStation: String? = null,
    onDirectionChange: (Direction) -> Unit,
    onNavigateToSettings: () -> Unit,
    onBoard: (Trip, String, String) -> Unit,
    onFinishTrip: () -> Unit,
    padding: PaddingValues
) {
    var currentTimeTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTimeTick = System.currentTimeMillis()
        }
    }

    val allStationsOrder = remember {
        listOf(
            "تهران",
            "تپه سفید",
            "اسلامشهر",
            "نسیم شهر",
            "گلستان",
            "نصیر شهر",
            "رباط کریم",
            "پرند"
        )
    }

    var isCustomRouteActive by rememberSaveable { mutableStateOf(false) }
    var customOriginFa by rememberSaveable { mutableStateOf("تهران") }
    var customDestFa by rememberSaveable { mutableStateOf("پرند") }
    var stationSearchDialogTarget by remember { mutableStateOf<String?>(null) }

    val liveTimeStr = remember(currentTimeTick) {
        val cal = Calendar.getInstance()
        String.format(java.util.Locale.US, "%02d:%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
    }

    val activeTrip = remember(activeTripId, currentTimeTick) {
        activeTripId?.let { repository.findTripById(it) }
    }

    val boardingStationName = remember(direction, defaultStation) {
        getBoardingStationName(direction, defaultStation)
    }

    val effectiveOrigin = if (isCustomRouteActive) {
        customOriginFa
    } else {
        boardingStationName
    }

    val effectiveDest = if (isCustomRouteActive) {
        customDestFa
    } else {
        if (direction == Direction.TEHRAN_TO_PARAND) {
            if (defaultStation != "تهران") defaultStation else "پرند"
        } else {
            "تهران"
        }
    }

    val nextTrips = remember(isCustomRouteActive, customOriginFa, customDestFa, direction, defaultStation, boardingStationName, currentTimeTick) {
        if (!isCustomRouteActive) {
            repository.nextTrips(direction = direction, count = 3, fromStationFa = boardingStationName)
        } else {
            val orgIdx = allStationsOrder.indexOf(customOriginFa)
            val dstIdx = allStationsOrder.indexOf(customDestFa)
            val dir = if (orgIdx != -1 && dstIdx != -1 && orgIdx > dstIdx) Direction.PARAND_TO_TEHRAN else Direction.TEHRAN_TO_PARAND
            val now = Calendar.getInstance()
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            repository.trips(dir).filter { trip ->
                repository.runsOn(trip, now)
            }.filter { trip ->
                val stations = trip.stations
                val oIdx = stations.indexOfFirst { it.nameFa == customOriginFa }
                val dIdx = stations.indexOfFirst { it.nameFa == customDestFa }
                if (oIdx != -1 && dIdx != -1 && oIdx < dIdx) {
                    val depMins = repository.toMinutes(stations[oIdx].time)
                    depMins >= nowMinutes
                } else {
                    false
                }
            }.sortedBy { trip ->
                val oIdx = trip.stations.indexOfFirst { it.nameFa == customOriginFa }
                repository.toMinutes(trip.stations[oIdx].time)
            }.take(3)
        }
    }

    if (stationSearchDialogTarget != null) {
        val isOrigin = stationSearchDialogTarget == "origin"
        StationSearchDialog(
            title = if (isOrigin) {
                if (lang == AppLanguage.FA) "انتخاب ایستگاه مبدا" else "Select Origin Station"
            } else {
                if (lang == AppLanguage.FA) "انتخاب ایستگاه مقصد" else "Select Destination Station"
            },
            currentSelectedFa = if (isOrigin) effectiveOrigin else effectiveDest,
            excludedStationFa = if (isOrigin) effectiveDest else effectiveOrigin,
            lang = lang,
            onSelectStation = { chosen ->
                if (isOrigin) {
                    customOriginFa = chosen
                    if (chosen == customDestFa) {
                        customDestFa = if (chosen == "تهران") "پرند" else "تهران"
                    }
                } else {
                    customDestFa = chosen
                    if (chosen == customOriginFa) {
                        customOriginFa = if (chosen == "پرند") "تهران" else "پرند"
                    }
                }
                isCustomRouteActive = true
                val orgIdx = allStationsOrder.indexOf(if (isOrigin) chosen else customOriginFa)
                val dstIdx = allStationsOrder.indexOf(if (!isOrigin) chosen else customDestFa)
                if (orgIdx != -1 && dstIdx != -1) {
                    val newDir = if (orgIdx < dstIdx) Direction.TEHRAN_TO_PARAND else Direction.PARAND_TO_TEHRAN
                    onDirectionChange(newDir)
                }
            },
            onDismiss = { stationSearchDialogTarget = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        DirectionToggle(lang, direction, onDirectionChange)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (activeTrip != null && !activeDestStation.isNullOrBlank()) {
                item {
                    LiveTripTrackerCard(
                        trip = activeTrip,
                        boardingStationFa = activeBoardingStation,
                        destStationFa = activeDestStation,
                        lang = lang,
                        currentTimeTick = currentTimeTick,
                        onFinishTrip = onFinishTrip
                    )
                }
            }



            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(
                        1.dp,
                        if (isCustomRouteActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == AppLanguage.FA) "جستجوی مسیر" else "Route Search",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (isCustomRouteActive) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable {
                                        isCustomRouteActive = false
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.RestartAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (lang == AppLanguage.FA) "حالت پیش‌فرض" else "Reset",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        stationSearchDialogTarget = "origin"
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (lang == AppLanguage.FA) "مبدا" else "Origin",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stationNameByFa(effectiveOrigin, lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    val tmp = customOriginFa
                                    customOriginFa = customDestFa
                                    customDestFa = tmp
                                    isCustomRouteActive = true
                                    val orgIdx = allStationsOrder.indexOf(customOriginFa)
                                    val dstIdx = allStationsOrder.indexOf(customDestFa)
                                    if (orgIdx != -1 && dstIdx != -1) {
                                        val newDir = if (orgIdx < dstIdx) Direction.TEHRAN_TO_PARAND else Direction.PARAND_TO_TEHRAN
                                        onDirectionChange(newDir)
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(36.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.SwapHoriz,
                                            contentDescription = "Swap",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        stationSearchDialogTarget = "dest"
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (lang == AppLanguage.FA) "مقصد" else "Destination",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stationNameByFa(effectiveDest, lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (isCustomRouteActive) {
                        if (lang == AppLanguage.FA)
                            "۳ قطار بعدی • ${stationNameByFa(effectiveOrigin, lang)} به ${stationNameByFa(effectiveDest, lang)}"
                        else
                            "Next 3 Trains • ${stationNameByFa(effectiveOrigin, lang)} to ${stationNameByFa(effectiveDest, lang)}"
                    } else {
                        if (lang == AppLanguage.FA) "۳ قطار بعدی • ${if (direction == Direction.TEHRAN_TO_PARAND) "تهران به پرند" else "پرند به تهران"}" else "Next 3 Trains Today"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (nextTrips.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (lang == AppLanguage.FA) "برای امروز قطار دیگری برنامه‌ریزی نشده است" else "No more trains scheduled for today",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.FA) "می‌توانید برنامه کامل روزانه را در تب «برنامه زمانی» بررسی کنید." else "Check all departures in the Timetable tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(nextTrips) { trip ->
                    TripHeroCard(
                        trip = trip,
                        direction = direction,
                        lang = lang,
                        repository = repository,
                        defaultStation = defaultStation,
                        customOriginFa = effectiveOrigin,
                        customDestFa = effectiveDest,
                        isBoarded = trip.id == activeTripId,
                        onBoard = { tripToBoard, bFa, dFa -> onBoard(tripToBoard, bFa, dFa) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveTripTrackerCard(
    trip: Trip,
    boardingStationFa: String? = null,
    destStationFa: String,
    lang: AppLanguage,
    currentTimeTick: Long = System.currentTimeMillis(),
    onFinishTrip: () -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentTimeTick }
    val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val nowSeconds = nowMinutes * 60 + cal.get(Calendar.SECOND)

    fun toMins(t: String): Int {
        val p = t.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    val boardingStation = trip.stations.firstOrNull { it.nameFa == boardingStationFa } ?: trip.stations.first()
    val boardingIndex = trip.stations.indexOf(boardingStation).takeIf { it >= 0 } ?: 0
    val destStation = trip.stations.firstOrNull { it.nameFa == destStationFa } ?: trip.stations.last()
    val destIndex = trip.stations.indexOf(destStation).takeIf { it >= 0 } ?: trip.stations.lastIndex

    val boardingMins = toMins(boardingStation.time)
    val destMins = toMins(destStation.time)
    val boardingSeconds = boardingMins * 60
    val destSeconds = destMins * 60

    val isBeforeDeparture = nowMinutes < boardingMins
    val isArrived = destIndex > boardingIndex && nowMinutes >= destMins

    var currentStopIndex = 0
    for (i in 0 until trip.stations.size) {
        if (toMins(trip.stations[i].time) <= nowMinutes) {
            currentStopIndex = i
        } else {
            break
        }
    }
    val currentStop = trip.stations[currentStopIndex]
    val nextStop = if (currentStopIndex < trip.stations.lastIndex) trip.stations[currentStopIndex + 1] else null
    val stopsRemaining = maxOf(0, destIndex - currentStopIndex)

    val totalJourneySeconds = maxOf(1, destSeconds - boardingSeconds)
    val elapsedSeconds = (nowSeconds - boardingSeconds).coerceIn(0, totalJourneySeconds)
    val progress = (elapsedSeconds.toFloat() / totalJourneySeconds.toFloat()).coerceIn(0f, 1f)

    var showFinishConfirmDialog by remember { mutableStateOf(false) }

    if (showFinishConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            title = {
                Text(
                    text = if (lang == AppLanguage.FA) "اتمام سفر" else "End Journey",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.FA)
                        "آیا از قطار پیاده شده‌اید و می‌خواهید وضعیت سفر به پایان برسد؟"
                    else
                        "Have you exited the train and want to end this journey?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmDialog = false
                        onFinishTrip()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (lang == AppLanguage.FA) "بله، پیاده شدم" else "Yes, I got off")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text(if (lang == AppLanguage.FA) "انصراف" else "Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == AppLanguage.FA) "همراه سفر شما" else "Trip Companion",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${trip.name(lang)} • ${trip.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == AppLanguage.FA) "موقعیت کنونی قطار:" else "Current Train Position:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when {
                            isBeforeDeparture -> {
                                if (lang == AppLanguage.FA)
                                    "در انتظار شروع حرکت در ایستگاه ${boardingStation.name(lang)}"
                                else
                                    "Waiting to depart at ${boardingStation.name(lang)}"
                            }
                            isArrived -> {
                                if (lang == AppLanguage.FA)
                                    "قطار به مقصد رسید • ایستگاه ${destStation.name(lang)}"
                                else
                                    "Train has reached destination • ${destStation.name(lang)}"
                            }
                            toMins(currentStop.time) == nowMinutes -> {
                                if (lang == AppLanguage.FA)
                                    "هم‌اکنون در ایستگاه ${currentStop.name(lang)} • توقف در سکو"
                                else
                                    "Currently stopped at ${currentStop.name(lang)}"
                            }
                            nextStop != null -> {
                                if (lang == AppLanguage.FA)
                                    "در مسیر بین ${currentStop.name(lang)} و ${nextStop.name(lang)}"
                                else
                                    "En route between ${currentStop.name(lang)} and ${nextStop.name(lang)}"
                            }
                            else -> currentStop.name(lang)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!isArrived && nextStop != null && !isBeforeDeparture) {
                        val minsToNext = maxOf(1, toMins(nextStop.time) - nowMinutes)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == AppLanguage.FA)
                                "ایستگاه بعدی: ${nextStop.name(lang)} • ساعت ${nextStop.time} • ${formatRemainingTime(minsToNext, lang)} دیگر"
                            else
                                "Next stop: ${nextStop.name(lang)} • ${nextStop.time} • in ${formatRemainingTime(minsToNext, lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (stopsRemaining <= 1 && !isBeforeDeparture) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (stopsRemaining <= 1 && !isBeforeDeparture) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == AppLanguage.FA) "ایستگاه پیاده شدن شما:" else "Your Exit Station:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = destStation.name(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (stopsRemaining <= 1 && !isBeforeDeparture) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = when {
                                    isArrived -> {
                                        if (lang == AppLanguage.FA) "رسیدید! سفر بخیر." else "Arrived! Have a safe journey."
                                    }
                                    stopsRemaining == 1 -> {
                                        if (lang == AppLanguage.FA) "آماده باشید • ایستگاه بعدی پیاده شوید" else "Prepare to exit • Next stop is your destination"
                                    }
                                    else -> {
                                        if (lang == AppLanguage.FA) "$stopsRemaining ایستگاه دیگر باقی مانده است" else "$stopsRemaining stops remaining"
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (lang == AppLanguage.FA) "ساعت رسیدن" else "Arrival Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = destStation.time,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!isArrived) {
                            val minsToDest = maxOf(0, destMins - nowMinutes)
                            Text(
                                text = if (lang == AppLanguage.FA) "${formatRemainingTime(minsToDest, lang)} دیگر" else "in ${formatRemainingTime(minsToDest, lang)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showFinishConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (lang == AppLanguage.FA) "پیاده شدم" else "I got off",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TripHeroCard(
    trip: Trip,
    direction: Direction,
    lang: AppLanguage,
    repository: TrainRepository,
    defaultStation: String = "تهران",
    customOriginFa: String? = null,
    customDestFa: String? = null,
    isBoarded: Boolean = false,
    onBoard: ((Trip, String, String) -> Unit)? = null
) {
    var isFavorite by remember(trip.id) { mutableStateOf(repository.isFavorite(trip.id)) }
    DisposableEffect(repository, trip.id) {
        val listener = {
            isFavorite = repository.isFavorite(trip.id)
        }
        repository.addFavoritesListener(listener)
        onDispose {
            repository.removeFavoritesListener(listener)
        }
    }
    var isExpanded by remember { mutableStateOf(false) }
    val canBoard = remember(trip) { repository.canBoardTrip(trip) }

    val boardingStation = remember(trip, direction, defaultStation, customOriginFa) {
        if (!customOriginFa.isNullOrBlank()) {
            trip.stations.firstOrNull { it.nameFa == customOriginFa } ?: getBoardingStation(trip, direction, defaultStation)
        } else {
            getBoardingStation(trip, direction, defaultStation)
        }
    }
    val destStation = remember(trip, direction, defaultStation, customDestFa) {
        if (!customDestFa.isNullOrBlank()) {
            trip.stations.firstOrNull { it.nameFa == customDestFa } ?: getDestinationStation(trip, direction, defaultStation)
        } else {
            getDestinationStation(trip, direction, defaultStation)
        }
    }
    val effectiveDepTime = boardingStation.time

    val countdownText = remember(effectiveDepTime, boardingStation.nameFa, lang) {
        try {
            val now = Calendar.getInstance()
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val parts = effectiveDepTime.split(":").map { it.toInt() }
            val depMinutes = parts[0] * 60 + parts[1]
            val diff = depMinutes - nowMinutes
            if (diff > 0) {
                if (lang == AppLanguage.FA) "${formatRemainingTime(diff, lang)} تا حرکت از ${boardingStation.name(lang)}" else "Departs ${boardingStation.name(lang)} in ${formatRemainingTime(diff, lang)}"
            } else if (diff == 0) {
                if (lang == AppLanguage.FA) "هم‌اکنون در سکوی ${boardingStation.name(lang)}" else "Boarding now at ${boardingStation.name(lang)}"
            } else null
        } catch (_: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = trip.id,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = trip.name(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = trip.days(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = {
                    repository.toggleFavorite(trip.id)
                    isFavorite = repository.isFavorite(trip.id)
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (countdownText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = if (lang == AppLanguage.FA) "مبدا" else "Origin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = boardingStation.name(lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = boardingStation.time,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsTransit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (lang == AppLanguage.FA) "مقصد" else "Destination",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = destStation.name(lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = destStation.time,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val showBoardButton = onBoard != null && (isBoarded || canBoard)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBoardButton && onBoard != null) {
                    if (isBoarded) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == AppLanguage.FA) "سوار قطارید" else "On Board",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { onBoard?.invoke(trip, boardingStation.nameFa, destStation.nameFa) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsTransit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == AppLanguage.FA) "سوار شدم" else "I'm on train",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) {
                                if (lang == AppLanguage.FA) "بستن جزئیات" else "Hide Details"
                            } else {
                                if (lang == AppLanguage.FA) "جزئیات ایستگاه‌ها" else "Station Details"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val originFa = boardingStation.nameFa
                    val destFa = destStation.nameFa
                    val bIdx = trip.stations.indexOfFirst { it.nameFa == originFa }
                    val dIdx = trip.stations.indexOfFirst { it.nameFa == destFa }
                    val minIdx = if (bIdx != -1 && dIdx != -1) minOf(bIdx, dIdx) else -1
                    val maxIdx = if (bIdx != -1 && dIdx != -1) maxOf(bIdx, dIdx) else -1

                    trip.stations.forEachIndexed { idx, st ->
                        val isOrigin = st.nameFa == originFa
                        val isDest = st.nameFa == destFa
                        val isInSegment = minIdx != -1 && idx in minIdx..maxIdx

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isOrigin -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                        isDest -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                        isInSegment -> MaterialTheme.colorScheme.surface
                                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        when {
                                            isOrigin -> 1.5.dp
                                            isDest -> 1.5.dp
                                            isInSegment -> 0.75.dp
                                            else -> 0.5.dp
                                        },
                                        when {
                                            isOrigin -> MaterialTheme.colorScheme.primary
                                            isDest -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                            isInSegment -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                        }
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isOrigin -> MaterialTheme.colorScheme.primary
                                        isDest -> MaterialTheme.colorScheme.primary
                                        isInSegment -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isOrigin) {
                                            Icon(
                                                imageVector = Icons.Filled.TripOrigin,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else if (isDest) {
                                            Icon(
                                                imageVector = Icons.Filled.Place,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Text(
                                                text = "${idx + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isInSegment) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = st.name(lang),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isOrigin || isDest) FontWeight.ExtraBold else if (isInSegment) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isOrigin || isDest) MaterialTheme.colorScheme.primary else if (isInSegment) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        if (isOrigin) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = if (lang == AppLanguage.FA) "مبدا شما" else "Origin",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        } else if (isDest) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = if (lang == AppLanguage.FA) "مقصد شما" else "Destination",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Text(
                                text = st.time,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isOrigin || isDest) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isOrigin || isDest) MaterialTheme.colorScheme.primary else if (isInSegment) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

