package com.raila.app
data class Station(
    val nameFa: String,
    val nameEn: String,
    val time: String
) {
    fun name(lang: AppLanguage) = if (lang == AppLanguage.FA) nameFa else nameEn
}
data class Trip(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val daysFa: String,
    val daysEn: String,
    val stations: List<Station>
) {
    val departureTime: String get() = stations.first().time
    val arrivalTime: String get() = stations.last().time

    fun name(lang: AppLanguage) = if (lang == AppLanguage.FA) nameFa else nameEn
    fun days(lang: AppLanguage) = if (lang == AppLanguage.FA) daysFa else daysEn
}

enum class Direction(val jsonKey: String, val labelFa: String, val labelEn: String) {
    TEHRAN_TO_PARAND("tehran_to_parand", "تهران ← پرند", "Tehran → Parand"),
    PARAND_TO_TEHRAN("parand_to_tehran", "پرند ← تهران", "Parand → Tehran");

    fun label(lang: AppLanguage) = if (lang == AppLanguage.FA) labelFa else labelEn
    fun opposite() = if (this == TEHRAN_TO_PARAND) PARAND_TO_TEHRAN else TEHRAN_TO_PARAND
}

enum class AppLanguage { FA, EN }

enum class AppColorTheme(
    val id: String,
    val labelFa: String,
    val labelEn: String,
    val primaryHex: Long
) {
    ROYAL_PURPLE("royal_purple", "بنفش سلطنتی", "Royal Purple", 0xFF6750A4),
    OCEAN_BLUE("ocean_blue", "آبی اقیانوسی", "Ocean Blue", 0xFF00639B),
    WARM_ORANGE("warm_orange", "نارنجی گرم", "Warm Orange", 0xFF9C4100),
    BURGUNDY("burgundy", "زرشکی", "Burgundy", 0xFF8B1A40),
    FOREST_GREEN("forest_green", "سبز جنگلی", "Forest Green", 0xFF006C4C);

    fun label(lang: AppLanguage) = if (lang == AppLanguage.FA) labelFa else labelEn
}

fun stationNameByFa(stationFa: String, lang: AppLanguage): String {
    if (lang == AppLanguage.FA) return stationFa
    return when (stationFa.trim()) {
        "تهران" -> "Tehran"
        "تپه سفید" -> "Tappe Sefid"
        "اسلامشهر", "اسلام شهر" -> "Eslamshahr"
        "نسیم شهر", "نسیم‌شهر" -> "Nasimshahr"
        "گلستان" -> "Golestan"
        "نصیر شهر", "نصیرشهر" -> "Nasirshahr"
        "رباط کریم", "رباط‌کریم" -> "Robat Karim"
        "پرند" -> "Parand"
        else -> stationFa
    }
}

enum class FontScale(val scale: Float, val labelFa: String, val labelEn: String) {
    SMALL(0.85f, "کوچک", "Small"),
    NORMAL(1.0f, "استاندارد", "Normal"),
    LARGE(1.18f, "بزرگ", "Large"),
    EXTRA_LARGE(1.32f, "خیلی بزرگ", "X-Large");

    fun label(lang: AppLanguage) = if (lang == AppLanguage.FA) labelFa else labelEn
}

fun getBoardingStation(trip: Trip, direction: Direction, defaultStationFa: String): Station {
    val originStation = trip.stations.first()
    val terminusStation = trip.stations.last()

    if (defaultStationFa == terminusStation.nameFa) {
        return originStation
    }
    if (defaultStationFa == originStation.nameFa) {
        return originStation
    }

    val intermediateStop = trip.stations.firstOrNull { it.nameFa == defaultStationFa }
    if (intermediateStop != null && intermediateStop != terminusStation) {
        return if (direction == Direction.TEHRAN_TO_PARAND) originStation else intermediateStop
    }

    return originStation
}

fun getDestinationStation(trip: Trip, direction: Direction, defaultStationFa: String): Station {
    val originStation = trip.stations.first()
    val terminusStation = trip.stations.last()

    if (defaultStationFa == terminusStation.nameFa) {
        return terminusStation
    }
    if (defaultStationFa == originStation.nameFa) {
        return terminusStation
    }

    val intermediateStop = trip.stations.firstOrNull { it.nameFa == defaultStationFa }
    if (intermediateStop != null && intermediateStop != originStation) {
        return if (direction == Direction.TEHRAN_TO_PARAND) intermediateStop else terminusStation
    }

    return terminusStation
}

fun getBoardingStationName(direction: Direction, defaultStationFa: String): String {
    return when (direction) {
        Direction.TEHRAN_TO_PARAND -> {
            if (defaultStationFa == "پرند" || defaultStationFa == "تهران") "تهران" else "تهران"
        }
        Direction.PARAND_TO_TEHRAN -> {
            if (defaultStationFa == "تهران") "پرند" else defaultStationFa
        }
    }
}

