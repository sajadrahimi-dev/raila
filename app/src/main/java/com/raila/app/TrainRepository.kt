package com.raila.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class TrainRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("raila_prefs", Context.MODE_PRIVATE)

    private var tripsByDirection: Map<Direction, List<Trip>> = emptyMap()
    private val scheduleUpdateListeners = java.util.concurrent.CopyOnWriteArrayList<() -> Unit>()

    init {
        loadDatabase()
    }

    fun addScheduleUpdateListener(listener: () -> Unit) {
        scheduleUpdateListeners.add(listener)
    }

    fun removeScheduleUpdateListener(listener: () -> Unit) {
        scheduleUpdateListeners.remove(listener)
    }

    private fun loadDatabase() {
        val json = loadCurrentDatabaseJson()
        try {
            val root = JSONObject(json)
            tripsByDirection = Direction.entries.associateWith { direction ->
                parseTrips(root.getJSONArray(direction.jsonKey))
            }
        } catch (e: Exception) {

            val assetJson = appContext.assets.open("database.json").bufferedReader().use { it.readText() }
            val root = JSONObject(assetJson)
            tripsByDirection = Direction.entries.associateWith { direction ->
                parseTrips(root.getJSONArray(direction.jsonKey))
            }
        }
    }

    private fun loadCurrentDatabaseJson(): String {
        val cachedFile = java.io.File(appContext.filesDir, "cached_database.json")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            try {
                val content = cachedFile.readText()
                if (content.contains("tehran_to_parand") && content.contains("parand_to_tehran")) {
                    return content
                }
            } catch (_: Exception) {}
        }
        return appContext.assets.open("database.json").bufferedReader().use { it.readText() }
    }

    fun checkForDatabaseUpdate(onComplete: ((Boolean, String) -> Unit)? = null) {
        Thread {
            try {
                val url = java.net.URL(GITHUB_RAW_DATABASE_URL)
                val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 7000
                    readTimeout = 7000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Raila-Android-App")
                    setRequestProperty("Accept", "application/json, text/plain")
                }
                val responseCode = conn.responseCode
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val fetchedJson = conn.inputStream.bufferedReader().use { it.readText() }
                    if (fetchedJson.isNotBlank() &&
                        fetchedJson.contains("tehran_to_parand") &&
                        fetchedJson.contains("parand_to_tehran")
                    ) {
                        val root = JSONObject(fetchedJson)
                        val newTrips = Direction.entries.associateWith { direction ->
                            parseTrips(root.getJSONArray(direction.jsonKey))
                        }

                        val currentJson = loadCurrentDatabaseJson()
                        val isChanged = fetchedJson.trim() != currentJson.trim()

                        val now = System.currentTimeMillis()
                        prefs.edit().putLong(KEY_LAST_UPDATE_TIME, now).apply()

                        if (isChanged) {
                            val cachedFile = java.io.File(appContext.filesDir, "cached_database.json")
                            cachedFile.writeText(fetchedJson)
                            tripsByDirection = newTrips

                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                scheduleUpdateListeners.forEach { it.invoke() }
                                onComplete?.invoke(true, "updated")
                            }
                        } else {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onComplete?.invoke(true, "up_to_date")
                            }
                        }
                        return@Thread
                    }
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onComplete?.invoke(false, "http_$responseCode")
                }
            } catch (e: Exception) {

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onComplete?.invoke(false, e.message ?: "offline")
                }
            }
        }.start()
    }

    fun lastUpdateTime(): Long = prefs.getLong(KEY_LAST_UPDATE_TIME, 0L)

    private fun parseTrips(array: JSONArray): List<Trip> {
        val list = mutableListOf<Trip>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.getJSONObject("name")
            val days = obj.getJSONObject("days")
            val stationsArray = obj.getJSONArray("stations")
            val stations = (0 until stationsArray.length()).map { j ->
                val st = stationsArray.getJSONObject(j)
                val stName = st.getJSONObject("name")
                Station(
                    nameFa = stName.getString("fa"),
                    nameEn = stName.getString("en"),
                    time = st.getString("time")
                )
            }
            list.add(
                Trip(
                    id = obj.getString("id"),
                    nameFa = name.getString("fa"),
                    nameEn = name.getString("en"),
                    daysFa = days.getString("fa"),
                    daysEn = days.getString("en"),
                    stations = stations
                )
            )
        }
        return list.sortedBy { toMinutes(it.stations.first().time) }
    }

    fun trips(direction: Direction): List<Trip> = tripsByDirection.getValue(direction)

    fun tripById(id: String): Trip? = tripsByDirection.values.flatten().firstOrNull { it.id == id }

    fun findTripById(id: String): Trip? = tripById(id)

    fun nextTrips(direction: Direction, count: Int, fromStationFa: String?): List<Trip> =
        nextTrips(direction, count, Calendar.getInstance(), fromStationFa)

    fun stationNames(direction: Direction, lang: AppLanguage): List<String> {
        val first = trips(direction).firstOrNull() ?: return emptyList()
        return first.stations.map { if (lang == AppLanguage.FA) it.nameFa else it.nameEn }
    }

    fun allStationNames(lang: AppLanguage): List<String> {
        return stationNames(Direction.TEHRAN_TO_PARAND, lang)
    }

    fun searchTrips(
        originName: String,
        destinationName: String,
        lang: AppLanguage
    ): Pair<Direction?, List<Trip>> {
        val standardStations = allStationNames(lang)
        val orgIdx = standardStations.indexOf(originName)
        val dstIdx = standardStations.indexOf(destinationName)

        if (orgIdx == -1 || dstIdx == -1 || orgIdx == dstIdx) {
            return Pair(null, emptyList())
        }

        val dir = if (orgIdx < dstIdx) Direction.TEHRAN_TO_PARAND else Direction.PARAND_TO_TEHRAN
        val results = tripsBetween(dir, originName, destinationName, lang)
        return Pair(dir, results)
    }

    fun runsOn(trip: Trip, calendar: Calendar): Boolean {
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val allowedDays = when {
            trip.daysEn.contains("Everyday", ignoreCase = true) -> ALL_DAYS
            trip.daysEn.contains("Workdays", ignoreCase = true) -> WORKDAYS
            trip.daysEn.contains("Saturday to Thursday", ignoreCase = true) -> SAT_TO_THU
            else -> ALL_DAYS
        }
        return day in allowedDays
    }

    fun nextTrip(direction: Direction, calendar: Calendar = Calendar.getInstance()): Trip? {
        return nextTrips(direction, count = 1, calendar = calendar).firstOrNull()
    }

    fun nextTrips(
        direction: Direction,
        count: Int = 3,
        calendar: Calendar = Calendar.getInstance(),
        fromStationFa: String? = null
    ): List<Trip> {
        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        return trips(direction)
            .filter { runsOn(it, calendar) }
            .filter { trip ->
                val boardingStation = if (!fromStationFa.isNullOrBlank()) {
                    getBoardingStation(trip, direction, fromStationFa)
                } else {
                    trip.stations.first()
                }
                toMinutes(boardingStation.time) >= nowMinutes
            }
            .take(count)
    }

    fun tripsBetween(
        direction: Direction,
        originName: String,
        destinationName: String,
        lang: AppLanguage
    ): List<Trip> {
        return trips(direction).filter { trip ->
            val names = trip.stations.map { if (lang == AppLanguage.FA) it.nameFa else it.nameEn }
            val originIndex = names.indexOf(originName)
            val destIndex = names.indexOf(destinationName)
            originIndex != -1 && destIndex != -1 && originIndex < destIndex
        }
    }

    private val favoriteListeners = java.util.concurrent.CopyOnWriteArrayList<() -> Unit>()

    fun addFavoritesListener(listener: () -> Unit) {
        favoriteListeners.add(listener)
    }

    fun removeFavoritesListener(listener: () -> Unit) {
        favoriteListeners.remove(listener)
    }

    fun favoriteIds(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun isFavorite(tripId: String): Boolean = tripId in favoriteIds()

    fun toggleFavorite(tripId: String) {
        val current = favoriteIds().toMutableSet()
        if (!current.add(tripId)) current.remove(tripId)
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        favoriteListeners.forEach { it.invoke() }
    }

    fun savedLanguage(): AppLanguage {
        val name = prefs.getString(KEY_LANGUAGE, AppLanguage.FA.name)
        return try {
            AppLanguage.valueOf(name ?: AppLanguage.FA.name)
        } catch (_: Exception) {
            AppLanguage.FA
        }
    }

    fun saveLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, lang.name).apply()
    }

    fun savedFontScale(): FontScale {
        val name = prefs.getString(KEY_FONT_SCALE, FontScale.NORMAL.name)
        return try {
            FontScale.valueOf(name ?: FontScale.NORMAL.name)
        } catch (_: Exception) {
            FontScale.NORMAL
        }
    }

    fun saveFontScale(scale: FontScale) {
        prefs.edit().putString(KEY_FONT_SCALE, scale.name).apply()
    }

    fun savedDefaultStation(): String {
        return prefs.getString(KEY_DEFAULT_STATION, "تهران") ?: "تهران"
    }

    fun saveDefaultStation(stationFa: String) {
        prefs.edit().putString(KEY_DEFAULT_STATION, stationFa).apply()
    }

    fun savedColorTheme(): AppColorTheme {
        val name = prefs.getString(KEY_COLOR_THEME, AppColorTheme.ROYAL_PURPLE.name)
        return try {
            AppColorTheme.valueOf(name ?: AppColorTheme.ROYAL_PURPLE.name)
        } catch (_: Exception) {
            AppColorTheme.ROYAL_PURPLE
        }
    }

    fun saveColorTheme(theme: AppColorTheme) {
        prefs.edit().putString(KEY_COLOR_THEME, theme.name).apply()
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun activeTripId(): String? = prefs.getString(KEY_ACTIVE_TRIP_ID, null)

    fun activeDestStation(): String? = prefs.getString(KEY_ACTIVE_DEST_STATION, null)

    fun activeBoardingStation(): String? = prefs.getString(KEY_ACTIVE_BOARDING_STATION, null)

    fun setActiveTracking(tripId: String?, destStationFa: String?, boardingStationFa: String? = null) {
        val editor = prefs.edit()
        if (tripId == null || destStationFa == null) {
            editor.remove(KEY_ACTIVE_TRIP_ID)
            editor.remove(KEY_ACTIVE_DEST_STATION)
            editor.remove(KEY_ACTIVE_BOARDING_STATION)
        } else {
            editor.putString(KEY_ACTIVE_TRIP_ID, tripId)
            editor.putString(KEY_ACTIVE_DEST_STATION, destStationFa)
            if (boardingStationFa != null) {
                editor.putString(KEY_ACTIVE_BOARDING_STATION, boardingStationFa)
            } else {
                editor.remove(KEY_ACTIVE_BOARDING_STATION)
            }
        }
        editor.apply()
    }

    fun toMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun canBoardTrip(trip: Trip, calendar: Calendar = Calendar.getInstance()): Boolean {
        if (!runsOn(trip, calendar)) return false
        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val lastStationTime = trip.stations.lastOrNull()?.time ?: return false
        return nowMinutes < toMinutes(lastStationTime)
    }

    fun isDisclaimerAccepted(): Boolean = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)

    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    companion object {
        private const val KEY_FAVORITES = "favorite_trip_ids"
        private const val KEY_LANGUAGE = "user_app_language"
        private const val KEY_FONT_SCALE = "user_font_scale"
        private const val KEY_DEFAULT_STATION = "user_default_station"
        private const val KEY_COLOR_THEME = "user_color_theme"
        private const val KEY_ONBOARDING_COMPLETED = "user_onboarding_completed"
        private const val KEY_DISCLAIMER_ACCEPTED = "user_disclaimer_accepted"
        private const val KEY_ACTIVE_TRIP_ID = "active_tracking_trip_id"
        private const val KEY_ACTIVE_DEST_STATION = "active_tracking_dest_station"
        private const val KEY_ACTIVE_BOARDING_STATION = "active_tracking_boarding_station"
        private const val KEY_LAST_UPDATE_TIME = "timetable_last_update_time"

        const val GITHUB_REPO_URL = "https:/" + "/github.com/sajadrahimi-dev/raila"
        const val GITHUB_RAW_DATABASE_URL = "https:/" + "/raw.githubusercontent.com/sajadrahimi-dev/raila/main/database.json"
        const val GITHUB_BLOB_DATABASE_URL = "https:/" + "/github.com/sajadrahimi-dev/raila/blob/main/database.json"

        private val ALL_DAYS = setOf(
            Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY,
            Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
        )

        private val SAT_TO_THU = setOf(
            Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY,
            Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY
        )

        private val WORKDAYS = setOf(
            Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY,
            Calendar.TUESDAY, Calendar.WEDNESDAY
        )
    }
}
