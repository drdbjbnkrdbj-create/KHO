package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.localization.AppLanguage
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.KhawlaTheme
import com.example.ui.viewmodel.KhawlaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: KhawlaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val language by viewModel.language.collectAsState()
            val themeMode by viewModel.theme.collectAsState()
            val isOmarVisible by viewModel.isOmarVisible.collectAsState()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
            val currentNavDestination by viewModel.currentNavDestination.collectAsState()
            val isOmarHelpOpen by viewModel.isOmarHelpOpen.collectAsState()
            val isQuickAddOpen by viewModel.isQuickAddOpen.collectAsState()

            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                KhawlaTheme(darkTheme = isDark) {
                    var showOnboardingManual by remember { mutableStateOf(false) }

                    if (!isOnboardingCompleted || showOnboardingManual) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinished = { showOnboardingManual = false }
                        )
                    } else {
                        // Handle back button
                        BackHandler(enabled = currentNavDestination != 0) {
                            viewModel.navigateTo(0)
                        }

                        Scaffold(
                            topBar = {
                                KhawlaTopBar(
                                    language = language,
                                    onOpenTimer = { viewModel.navigateTo(5) },
                                    onOpenSettings = { viewModel.navigateTo(6) },
                                    onOpenRescue = { viewModel.setRescueDialogOpen(true) }
                                )
                            },
                            bottomBar = {
                                if (currentNavDestination in 0..4) {
                                    KhawlaBottomNav(
                                        selectedIndex = currentNavDestination,
                                        language = language,
                                        onSelectIndex = { viewModel.navigateTo(it) }
                                    )
                                }
                            },
                            contentWindowInsets = WindowInsets(0, 0, 0, 0)
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AnimatedContent(
                                    targetState = currentNavDestination,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "screen_transition"
                                ) { destination ->
                                    when (destination) {
                                        0 -> HomeScreen(viewModel = viewModel)
                                        1 -> SubjectsScreen(viewModel = viewModel)
                                        2 -> TasksScreen(viewModel = viewModel)
                                        3 -> ReviewScreen(viewModel = viewModel)
                                        4 -> LibraryScreen(viewModel = viewModel)
                                        5 -> FocusTimerScreen(viewModel = viewModel)
                                        6 -> SettingsScreen(
                                            viewModel = viewModel,
                                            onRestartOnboarding = { showOnboardingManual = true }
                                        )
                                        else -> HomeScreen(viewModel = viewModel)
                                    }
                                }

                                // Omar persistent floating assistant
                                if (currentNavDestination in 0..4) {
                                    OmarFloatingAssistant(
                                        language = language,
                                        visible = isOmarVisible,
                                        onClick = { viewModel.setOmarHelpOpen(true) }
                                    )
                                }
                            }
                        }

                        // Omar Contextual Help Dialog
                        if (isOmarHelpOpen) {
                            OmarHelpDialog(
                                language = language,
                                currentScreenIndex = currentNavDestination,
                                onDismiss = { viewModel.setOmarHelpOpen(false) },
                                onHideOmar = { viewModel.prefs.setOmarVisible(false) }
                            )
                        }

                        // Quick Add Modal
                        if (isQuickAddOpen) {
                            QuickAddBottomSheet(
                                language = language,
                                onDismiss = { viewModel.setQuickAddOpen(false) },
                                onSelectOption = { option ->
                                    when (option) {
                                        "task" -> viewModel.navigateTo(2)
                                        "subject" -> viewModel.navigateTo(1)
                                        "note" -> viewModel.navigateTo(4)
                                        "flashcard", "formula" -> viewModel.navigateTo(3)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
