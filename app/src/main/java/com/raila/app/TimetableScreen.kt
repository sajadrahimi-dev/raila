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
internal fun TimetableScreen(
    repository: TrainRepository,
    lang: AppLanguage,
    direction: Direction,
    onDirectionChange: (Direction) -> Unit,
    activeTripId: String? = null,
    onBoard: ((Trip) -> Unit)? = null,
    padding: PaddingValues
) {
    val trips = remember(direction) { repository.trips(direction) }
    var filterWorkdaysOnly by remember { mutableStateOf(false) }

    val filteredTrips = remember(trips, filterWorkdaysOnly) {
        if (filterWorkdaysOnly) {
            trips.filter { it.daysEn.contains("Workdays", ignoreCase = true) || it.daysFa.contains("اداری") }
        } else {
            trips
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        DirectionToggle(lang, direction, onDirectionChange)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !filterWorkdaysOnly,
                onClick = { filterWorkdaysOnly = false },
                label = { Text(if (lang == AppLanguage.FA) "همه قطارها (${trips.size})" else "All Trains (${trips.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = filterWorkdaysOnly,
                onClick = { filterWorkdaysOnly = true },
                label = { Text(if (lang == AppLanguage.FA) "فقط روزهای اداری" else "Workdays Only") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp)
        ) {
            items(filteredTrips) { trip ->
                TripCard(
                    trip = trip,
                    lang = lang,
                    repository = repository,
                    isBoarded = trip.id == activeTripId,
                    onBoard = onBoard
                )
            }
        }
    }
}

