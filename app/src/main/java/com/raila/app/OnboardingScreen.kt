package com.raila.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingStep(
    val titleFa: String,
    val titleEn: String,
    val subtitleFa: String,
    val subtitleEn: String,
    val badgeFa: String,
    val badgeEn: String,
    val icon: ImageVector,
    val highlightsFa: List<String>,
    val highlightsEn: List<String>
)

@Composable
fun OnboardingScreen(
    currentLang: AppLanguage,
    defaultStation: String,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectDefaultStation: (String) -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    val isRtl = currentLang == AppLanguage.FA
    val layoutDir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val steps = remember {
        listOf(
            OnboardingStep(
                titleFa = "انتخاب زبان برنامه",
                titleEn = "Choose App Language",
                subtitleFa = "ابتدا زبان مورد نظر خود را انتخاب کنید تا برنامه به همان زبان اجرا شود.",
                subtitleEn = "First select your preferred language for timetables and user interface.",
                badgeFa = "گام اول",
                badgeEn = "Step 1",
                icon = Icons.Filled.Language,
                highlightsFa = listOf("پشتیبانی کامل از زبان فارسی و تقویم شمسی", "امکان تغییر در هر زمان از منوی تنظیمات"),
                highlightsEn = listOf("Full English interface & Gregorian times", "Can be changed anytime from Settings")
            ),
            OnboardingStep(
                titleFa = "استقلال پروژه و سلب مسئولیت",
                titleEn = "Project Independence & Disclaimer",
                subtitleFa = "اطلاعیه مهم و عدم وابستگی به شرکت‌های ریلی",
                subtitleEn = "Important notice and non-affiliation disclaimer with railway organizations",
                badgeFa = "اطلاعیه مهم",
                badgeEn = "Important",
                icon = Icons.Filled.Shield,
                highlightsFa = listOf(
                    "پروژه کاملاً مستقل، شخصی و متن‌باز",
                    "بدون هیچ‌گونه وابستگی یا ارتباط رسمی با راه‌آهن یا رجا",
                    "سلب مسئولیت بابت تغییر ناگهانی ساعت، لغو یا خسارات"
                ),
                highlightsEn = listOf(
                    "100% independent, personal & open-source project",
                    "No official affiliation or partnership with IRIR or Raja",
                    "No liability for sudden schedule changes, cancellations, or damages"
                )
            ),
            OnboardingStep(
                titleFa = "جدول زمانی دقیق و ۱۰۰٪ آفلاین",
                titleEn = "Accurate & 100% Offline Timetable",
                subtitleFa = "ساعت حرکت تمامی قطارهای حومه‌ای مسیر تهران - پرند و بالعکس، بدون نیاز به اتصال اینترنت در جیب شماست.",
                subtitleEn = "Complete schedule of all Tehran - Parand suburban trains, accessible fully offline at any time.",
                badgeFa = "کاملاً آفلاین",
                badgeEn = "100% Offline",
                icon = Icons.Filled.Schedule,
                highlightsFa = listOf("بدون نیاز به اینترنت", "متن‌باز و رایگان در گیت‌هاب", "به‌روزرسانی خودکار جدول زمانی از گیت‌هاب"),
                highlightsEn = listOf("No internet required", "Free & Open Source on GitHub", "Auto syncs timetable from GitHub")
            ),
            OnboardingStep(
                titleFa = "۳ قطار بعدی با شمارش معکوس زنده",
                titleEn = "Next 3 Trains with Live Countdown",
                subtitleFa = "با تعیین ایستگاه پیش‌فرض، بلافاصله در صفحه اصلی نزدیک‌ترین قطارها و زمان دقیق باقیمانده را ببینید.",
                subtitleEn = "Choose your home station to instantly view the next 3 departures with real-time countdown minutes.",
                badgeFa = "شمارش معکوس",
                badgeEn = "Live Countdown",
                icon = Icons.Filled.Timer,
                highlightsFa = listOf("شمارش زنده دقایق تا حرکت", "تفکیک روزهای اداری و تعطیل", "امکان نشان کردن قطارها"),
                highlightsEn = listOf("Live remaining minutes countdown", "Workdays & holidays breakdown", "Bookmark favorite train trips")
            ),
            OnboardingStep(
                titleFa = "حالت همراه قطار",
                titleEn = "Live On-board Travel Companion",
                subtitleFa = "ایستگاه‌های بعدی و زمان تقریبی رسیدن به مقصد را در طول مسیر سفر به سادگی دنبال کنید.",
                subtitleEn = "Track upcoming stops and arrival estimate live during your train journey.",
                badgeFa = "همراه هوشمند",
                badgeEn = "Smart Companion",
                icon = Icons.Filled.Navigation,
                highlightsFa = listOf("نمایش ایستگاه پیش‌رو در مسیر", "محاسبه زمان تقریبی رسیدن به مقصد", "بدون مصرف باتری یا GPS"),
                highlightsEn = listOf("Next upcoming stop announcement", "Calculated arrival time to destination", "Battery friendly without GPS")
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isDisclaimerAccepted by rememberSaveable { mutableStateOf(false) }
    var chosenStation by remember { mutableStateOf(defaultStation) }

    val popularStations = listOf(
        "تهران" to "Tehran",
        "اسلامشهر" to "Eslamshahr",
        "نسیم‌شهر" to "Nasimshahr",
        "گلستان" to "Golestan",
        "نصیرشهر" to "Nasirshahr",
        "رباط‌کریم" to "Robat Karim",
        "پرند" to "Parand"
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.DirectionsTransit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (currentLang == AppLanguage.FA) "ریلا | Raila" else "Raila",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (currentLang == AppLanguage.FA) "راهنمای سریع شروع" else "Quick Start Tour",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            steps.forEachIndexed { index, _ ->
                                val isCurrent = index == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(6.dp)
                                        .width(if (isCurrent) 28.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outlineVariant
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    AnimatedContent(
                        targetState = currentStepIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> if (isRtl) -width else width } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> if (isRtl) width else -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> if (isRtl) width else -width } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> if (isRtl) -width else width } + fadeOut())
                            }
                        },
                        label = "OnboardingStepContent"
                    ) { stepIdx ->
                        val step = steps[stepIdx]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = step.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = if (currentLang == AppLanguage.FA) step.badgeFa else step.badgeEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.FA) step.titleFa else step.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.FA) step.subtitleFa else step.subtitleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                if (stepIdx == 0) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val isFa = currentLang == AppLanguage.FA
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelectLanguage(AppLanguage.FA) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isFa)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                                contentColor = if (isFa)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = BorderStroke(
                                                width = if (isFa) 2.dp else 1.dp,
                                                color = if (isFa)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outlineVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (isFa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                        modifier = Modifier.size(34.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "فا",
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isFa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                style = MaterialTheme.typography.labelMedium
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = "فارسی",
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "تقویم شمسی و اسامی فارسی ایستگاه‌ها",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (isFa) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }

                                        val isEn = currentLang == AppLanguage.EN
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelectLanguage(AppLanguage.EN) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isEn)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                                contentColor = if (isEn)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = BorderStroke(
                                                width = if (isEn) 2.dp else 1.dp,
                                                color = if (isEn)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outlineVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (isEn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                        modifier = Modifier.size(34.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "EN",
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isEn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                style = MaterialTheme.typography.labelMedium
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = "English",
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "Gregorian dates & English station names",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (isEn) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else if (stepIdx == 1) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = if (currentLang == AppLanguage.FA)
                                                        "این پروژه کاملاً مستقل، شخصی و متن‌باز است و هیچ‌گونه وابستگی، ارتباط رسمی یا همکاری با شرکت راه‌آهن جمهوری اسلامی ایران یا شرکت حمل و نقل ریلی رجا ندارد. ساعت‌ها و برنامه‌های نمایش داده شده صرفاً بر اساس اطلاعات عمومی منتشر شده است و توسعه‌دهنده هیچ‌گونه مسئولیتی در قبال تغییرات ناگهانی ساعت حرکت، لغو قطارها، تأخیرها یا خسارات احتمالی ناشی از استفاده از این برنامه بر عهده نمی‌گیرد."
                                                    else
                                                        "This project is completely independent, personal, and open-source, and has no official affiliation, relationship, or partnership with the Railways of the Islamic Republic of Iran or Raja Rail Transportation Co. The displayed schedules and timetables are based solely on publicly available information, and the developer assumes no responsibility for sudden schedule changes, train cancellations, delays, or potential damages resulting from the use of this application.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 21.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isDisclaimerAccepted)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else
                                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                            border = BorderStroke(
                                                width = if (isDisclaimerAccepted) 1.5.dp else 1.dp,
                                                color = if (isDisclaimerAccepted)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isDisclaimerAccepted = !isDisclaimerAccepted }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isDisclaimerAccepted,
                                                    onCheckedChange = { isDisclaimerAccepted = it },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = MaterialTheme.colorScheme.primary,
                                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (currentLang == AppLanguage.FA)
                                                        "متن فوق را مطالعه کرده‌ام و سلب مسئولیت را می‌پذیرم"
                                                    else
                                                        "I have read and accept the terms and disclaimer",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val highlights = if (currentLang == AppLanguage.FA) step.highlightsFa else step.highlightsEn
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        highlights.forEach { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    if (currentStepIndex == steps.size - 1) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (currentLang == AppLanguage.FA) "ایستگاه پرتردد یا محل سکونت شما:" else "Your Primary Station:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    items(popularStations) { (stFa, stEn) ->
                                        val name = if (currentLang == AppLanguage.FA) stFa else stEn
                                        val isSelected = chosenStation == stFa
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                chosenStation = stFa
                                                onSelectDefaultStation(stFa)
                                            },
                                            label = {
                                                Text(
                                                    text = name,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            leadingIcon = if (isSelected) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            } else null,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (currentStepIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentStepIndex-- },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = if (currentLang == AppLanguage.FA) "قبلی" else "Back",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val canProceed = if (currentStepIndex == 1) isDisclaimerAccepted else true

                            Button(
                                onClick = {
                                    if (!canProceed) return@Button
                                    if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                    } else {
                                        onSelectDefaultStation(chosenStation)
                                        onCompleteOnboarding()
                                    }
                                },
                                enabled = canProceed,
                                modifier = Modifier
                                    .weight(if (currentStepIndex > 0) 2f else 1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = if (!canProceed) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canProceed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (canProceed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = when (currentStepIndex) {
                                            0 -> if (currentLang == AppLanguage.FA) "تایید زبان و ادامه" else "Confirm & Continue"
                                            1 -> if (currentLang == AppLanguage.FA) "پذیرش شرایط و ادامه" else "Accept & Continue"
                                            steps.size - 1 -> if (currentLang == AppLanguage.FA) "شروع سفر با ریلا" else "Start Journey with Raila"
                                            else -> if (currentLang == AppLanguage.FA) "گام بعدی" else "Next"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (currentStepIndex == steps.size - 1)
                                            Icons.Filled.DirectionsTransit
                                        else
                                            Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (currentLang == AppLanguage.FA)
                                "پروژه کاملاً رایگان و متن‌باز در گیت‌هاب • نسخه ۱.۰"
                            else
                                "100% Free & Open Source on GitHub • v1.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}