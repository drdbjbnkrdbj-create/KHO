package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppLanguage
import com.example.localization.S
import com.example.ui.theme.*
import com.example.ui.viewmodel.KhawlaViewModel

@Composable
fun OnboardingScreen(
    viewModel: KhawlaViewModel,
    onFinished: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    var currentStep by remember { mutableStateOf(0) } // 0, 1, 2

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (currentStep == index) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (currentStep == index) DeepLavender else CardBorder
                                )
                        )
                    }
                }

                TextButton(onClick = {
                    viewModel.prefs.setOnboardingCompleted(true)
                    onFinished()
                }) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "تخطي" else "Passer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedText
                    )
                }
            }

            // Step Content
            AnimatedContent(targetState = currentStep, label = "onboarding_step") { step ->
                when (step) {
                    0 -> OnboardingStep1(language)
                    1 -> OnboardingStep2(language)
                    2 -> OnboardingStep3(viewModel, language, theme)
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (language == AppLanguage.ARABIC) "السابق" else "Précédent")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < 2) {
                            currentStep += 1
                        } else {
                            viewModel.prefs.setOnboardingCompleted(true)
                            onFinished()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepLavender),
                    modifier = Modifier.testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (currentStep == 2) {
                            if (language == AppLanguage.ARABIC) "لنبدأ معاً! 🌸" else "C'est parti ! 🌸"
                        } else {
                            if (language == AppLanguage.ARABIC) "التالي" else "Suivant"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingStep1(language: AppLanguage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_omar_full),
            contentDescription = "Omar",
            modifier = Modifier
                .height(260.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (language == AppLanguage.ARABIC) "مرحباً بكِ في خولة" else "Bienvenue sur Khawla",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (language == AppLanguage.ARABIC)
                "أنا عُمر، رفيقكِ الدراسي الهادئ. هنا ستجدين كل ما تحتاجينه لتنظيم دراستك في الأقسام التحضيرية MPSI دون قلق أو ضغوط."
            else
                "Je suis Omar, ton compagnon d'étude. Tout est pensé pour t'accompagner avec bienveillance dans ton parcours en prépa MPSI, à ton propre rythme.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingStep2(language: AppLanguage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_omar_sticker_guide),
            contentDescription = "Omar Guide",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (language == AppLanguage.ARABIC) "أدواتكِ في مكان واحد" else "Tout pour réussir",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        val features = if (language == AppLanguage.ARABIC) listOf(
            Icons.Default.School to "المواد والفصول: تتبعي استيعابك لكل مقرر",
            Icons.Default.Checklist to "المهام والواجبات: نظمي أعمالك دون تأجيل مرهق",
            Icons.Default.Psychology to "المراجعة الذكية: بطاقات التكرار المتباعد وسجل الأخطاء",
            Icons.Default.LocalLibrary to "المكتبة والدفتر: فئاتك، قوانينك وكتبك المستعارة"
        ) else listOf(
            Icons.Default.School to "Matières & Chapitres : suivi de compréhension",
            Icons.Default.Checklist to "Tâches & DM : organisation fluide sans culpabilité",
            Icons.Default.Psychology to "Flashcards & Erreurs : répétition espacée efficace",
            Icons.Default.LocalLibrary to "Bibliothèque : notes, formulaires et livres empruntés"
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            features.forEach { (icon, text) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PalePink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = DeepLavender, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingStep3(
    viewModel: KhawlaViewModel,
    language: AppLanguage,
    theme: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_omar_sticker_heart),
            contentDescription = "Omar Heart",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (language == AppLanguage.ARABIC) "تخصيص تجربتك" else "Personnalise ton espace",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Language Chooser
        Text(
            text = S.get("language_setting", language),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                AppLanguage.ARABIC to "العربية",
                AppLanguage.FRENCH to "Français"
            ).forEach { (lang, label) ->
                val isSel = language == lang
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) PalePink else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, DeepLavender) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.prefs.setLanguage(lang) }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) DeepLavender else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Theme Chooser
        Text(
            text = S.get("theme_setting", language),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "light" to S.get("theme_light", language).split(" ").first(),
                "dark" to S.get("theme_dark", language).split(" ").first()
            ).forEach { (tKey, label) ->
                val isSel = theme == tKey
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) PalePink else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, DeepLavender) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.prefs.setTheme(tKey) }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) DeepLavender else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
