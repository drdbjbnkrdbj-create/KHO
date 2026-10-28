package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppLanguage
import com.example.localization.S
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.KhawlaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FocusTimerScreen(
    viewModel: KhawlaViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val isBreakMode by viewModel.isBreakMode.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val soundVibrateEnabled by viewModel.soundVibrateEnabled.collectAsState()
    val focusDurationMinutes by viewModel.focusDurationMinutes.collectAsState()

    val totalDurationSeconds = if (isBreakMode) 5 * 60 else focusDurationMinutes * 60
    val progress = 1f - (timerSecondsRemaining.toFloat() / totalDurationSeconds.toFloat())

    val minutes = timerSecondsRemaining / 60
    val seconds = timerSecondsRemaining % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Mode Title
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isBreakMode) GentleSuccess.copy(alpha = 0.15f) else PalePink,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isBreakMode) Icons.Default.Spa else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isBreakMode) GentleSuccess else DeepLavender,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isBreakMode) S.get("short_break", language) else S.get("focus_session", language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isBreakMode) GentleSuccess else DeepLavender
                    )
                }
            }
        }

        // Circular Timer Display
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .padding(10.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = if (isBreakMode) GentleSuccess else DeepLavender,
                    trackColor = CardBorder
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isBreakMode) "Pause / راحة" else "Focus / تركيز",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Controls (Start / Pause / Reset)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.resetTimer() },
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = S.get("timer_reset", language))
                }

                Button(
                    onClick = { viewModel.toggleTimer() },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) GentleWarning else DeepLavender
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("timer_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTimerRunning) S.get("timer_pause", language) else S.get("timer_start", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        viewModel.prefs.setSoundVibrateEnabled(!soundVibrateEnabled)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = if (soundVibrateEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = S.get("sound_vibrate", language),
                        tint = if (soundVibrateEnabled) DeepLavender else MutedText
                    )
                }
            }
        }

        // Duration selector chips (15, 25, 45 min)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    FilterChip(
                        selected = focusDurationMinutes == mins,
                        onClick = {
                            viewModel.prefs.setFocusDurationMinutes(mins)
                            if (!isTimerRunning && !isBreakMode) {
                                viewModel.resetTimer()
                            }
                        },
                        label = { Text("$mins min") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PalePink,
                            selectedLabelColor = DeepLavender
                        )
                    )
                }
            }
        }

        // Omar Encouragement Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_omar_smile_transparent),
                        contentDescription = "Omar cheering",
                        modifier = Modifier
                            .size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "أنتِ قادرة على هذا يا بطلة! ✨" else "Tu es formidable, continue comme ça ! ✨",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "ركزي على مهمة واحدة، لا تفكري في كل شيء دفعة واحدة."
                            else "Une seule équation, un seul paragraphe à la fois. Le calme est la clé du succès.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Focus Sessions Log
        item {
            SectionHeader(title = "${S.get("focus_sessions", language)} (${focusSessions.size})")
        }

        if (focusSessions.isEmpty()) {
            item {
                Text(
                    text = if (language == AppLanguage.ARABIC) "لم تسجلي جلسات اليوم بعد. ابدئي جلستك الأولى!"
                    else "Aucune session enregistrée aujourd'hui. Lance ta première session !",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        } else {
            val dateLocale = if (language == AppLanguage.ARABIC) Locale("ar") else Locale.FRENCH
            val timeFmt = SimpleDateFormat("HH:mm", dateLocale)

            items(focusSessions.take(5), key = { it.id }) { s ->
                val timeStr = timeFmt.format(Date(s.timestamp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GentleSuccess, modifier = Modifier.size(18.dp))
                            Text(
                                text = "${s.durationMinutes} min ${if (s.sessionType == "focus") "Focus" else "Pause"}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = MutedText)
                    }
                }
            }
        }
    }
}
