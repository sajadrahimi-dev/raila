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

internal enum class Screen(
    val labelFa: String,
    val labelEn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("خانه", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TIMETABLE("برنامه حرکت", "Timetable", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    FAVORITES("نشان‌شده‌ها", "Favorites", Icons.Filled.Star, Icons.Outlined.StarBorder),
    SETTINGS("تنظیمات", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RailaApp(
    repository: TrainRepository,
    currentColorTheme: AppColorTheme = repository.savedColorTheme(),
    onColorThemeChange: (AppColorTheme) -> Unit = { repository.saveColorTheme(it) }
) {
    var lang by rememberSaveable { mutableStateOf(repository.savedLanguage()) }
    var fontScale by rememberSaveable { mutableStateOf(repository.savedFontScale()) }
    var defaultStation by rememberSaveable { mutableStateOf(repository.savedDefaultStation()) }
    var activeTripId by rememberSaveable { mutableStateOf(repository.activeTripId()) }
    var activeDestStation by rememberSaveable { mutableStateOf(repository.activeDestStation()) }
    var activeBoardingStation by rememberSaveable { mutableStateOf(repository.activeBoardingStation()) }
    var tripToBoard by remember { mutableStateOf<Trip?>(null) }
    var boardingStationForTrip by remember { mutableStateOf<String?>(null) }
    var direction by rememberSaveable { mutableStateOf(Direction.TEHRAN_TO_PARAND) }
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var showOnboarding by rememberSaveable { mutableStateOf(!repository.isOnboardingCompleted()) }
    var isSplashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200L)
        isSplashVisible = false
    }

    var scheduleVersion by remember { mutableLongStateOf(0L) }
    DisposableEffect(repository) {
        val listener = {
            scheduleVersion = System.currentTimeMillis()
        }
        repository.addScheduleUpdateListener(listener)
        onDispose {
            repository.removeScheduleUpdateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        repository.checkForDatabaseUpdate()
    }

    BackHandler(enabled = screen != Screen.HOME) {
        screen = Screen.HOME
    }

    val layoutDirection = if (lang == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
    val currentDensity = LocalDensity.current
    val scaledDensity = remember(currentDensity, fontScale) {
        Density(density = currentDensity.density, fontScale = fontScale.scale)
    }

    if (isSplashVisible) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection,
            LocalDensity provides scaledDensity
        ) {
            SplashScreen(lang = lang)
        }
        return
    }

    if (showOnboarding) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection,
            LocalDensity provides scaledDensity
        ) {
            OnboardingScreen(
                currentLang = lang,
                defaultStation = defaultStation,
                onSelectLanguage = { newLang ->
                    lang = newLang
                    repository.saveLanguage(newLang)
                },
                onSelectDefaultStation = { st ->
                    defaultStation = st
                    repository.saveDefaultStation(st)
                },
                onCompleteOnboarding = {
                    repository.setOnboardingCompleted(true)
                    showOnboarding = false
                }
            )
        }
        return
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalDensity provides scaledDensity
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        if (screen == Screen.SETTINGS) {
                            IconButton(onClick = { screen = Screen.HOME }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = if (lang == AppLanguage.FA) "بازگشت" else "Back"
                                )
                            }
                        }
                    },
                    title = {
                        if (screen == Screen.SETTINGS) {
                            Text(
                                text = if (lang == AppLanguage.FA) "تنظیمات برنامه" else "Settings",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.DirectionsTransit,
                                            contentDescription = "Raila",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.FA) "ریلا" else "Raila",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.FA) "قطار حومه‌ای تهران - پرند" else "Tehran - Parand Suburban Rail",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (screen != Screen.SETTINGS) {
                            IconButton(
                                onClick = { screen = Screen.SETTINGS },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = if (lang == AppLanguage.FA) "تنظیمات" else "Settings",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 6.dp
                ) {
                    val bottomScreens = listOf(Screen.HOME, Screen.TIMETABLE, Screen.FAVORITES)
                    bottomScreens.forEach { s ->
                        val selected = screen == s
                        NavigationBarItem(
                            selected = selected,
                            onClick = { screen = s },
                            icon = {
                                Icon(
                                    imageVector = if (selected) s.selectedIcon else s.unselectedIcon,
                                    contentDescription = if (lang == AppLanguage.FA) s.labelFa else s.labelEn
                                )
                            },
                            label = {
                                Text(
                                    text = if (lang == AppLanguage.FA) s.labelFa else s.labelEn,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        ) { padding ->
            androidx.compose.runtime.key(scheduleVersion) {
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        repository = repository,
                        lang = lang,
                        direction = direction,
                        defaultStation = defaultStation,
                        activeTripId = activeTripId,
                        activeDestStation = activeDestStation,
                        activeBoardingStation = activeBoardingStation,
                        onDirectionChange = { direction = it },
                        onNavigateToSettings = { screen = Screen.SETTINGS },
                        onBoard = { trip, customBoarding, customDest ->
                            activeTripId = trip.id
                            activeDestStation = customDest
                            activeBoardingStation = customBoarding
                            repository.setActiveTracking(trip.id, customDest, customBoarding)
                            tripToBoard = null
                            boardingStationForTrip = null
                        },
                        onFinishTrip = {
                            activeTripId = null
                            activeDestStation = null
                            activeBoardingStation = null
                            repository.setActiveTracking(null, null, null)
                        },
                        padding = padding
                    )
                    Screen.TIMETABLE -> TimetableScreen(
                        repository = repository,
                        lang = lang,
                        direction = direction,
                        onDirectionChange = { direction = it },
                        activeTripId = activeTripId,
                        onBoard = { trip -> tripToBoard = trip },
                        padding = padding
                    )
                    Screen.FAVORITES -> FavoritesScreen(
                        repository = repository,
                        lang = lang,
                        activeTripId = activeTripId,
                        onBoard = { trip -> tripToBoard = trip },
                        padding = padding
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        repository = repository,
                        currentLang = lang,
                        currentFontScale = fontScale,
                        currentColorTheme = currentColorTheme,
                        defaultStation = defaultStation,
                        onLanguageChange = { newLang ->
                            lang = newLang
                            repository.saveLanguage(newLang)
                        },
                        onFontScaleChange = { newScale ->
                            fontScale = newScale
                            repository.saveFontScale(newScale)
                        },
                        onColorThemeChange = onColorThemeChange,
                        onDefaultStationChange = { newStation ->
                            defaultStation = newStation
                            repository.saveDefaultStation(newStation)
                        },
                        onOpenOnboarding = { showOnboarding = true },
                        onBack = { screen = Screen.HOME },
                        padding = padding
                    )
                }
            }
        }
        tripToBoard?.let { trip ->
            val bStation = boardingStationForTrip ?: getBoardingStation(trip, direction, defaultStation).nameFa
            BoardingDialog(
                trip = trip,
                boardingStationFa = bStation,
                currentDefaultStation = defaultStation,
                lang = lang,
                onConfirm = { chosenDest ->
                    activeTripId = trip.id
                    activeDestStation = chosenDest
                    activeBoardingStation = bStation
                    repository.setActiveTracking(trip.id, chosenDest, bStation)
                    tripToBoard = null
                    boardingStationForTrip = null
                    screen = Screen.HOME
                },
                onDismiss = {
                    tripToBoard = null
                    boardingStationForTrip = null
                }
            )
        }
    }
}

