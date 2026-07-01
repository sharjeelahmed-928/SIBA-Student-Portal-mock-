package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodels.AIStudyViewModel
import com.example.ui.viewmodels.AttendanceViewModel
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.GPAViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainContainerScreen(
    authViewModel: AuthViewModel,
    attendanceViewModel: AttendanceViewModel,
    gpaViewModel: GPAViewModel,
    aiStudyViewModel: AIStudyViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("dashboard") }

    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                // Dashboard Tab
                NavigationBarItem(
                    selected = selectedTab == "dashboard",
                    onClick = { selectedTab = "dashboard" },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == "dashboard") Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Home", style = MaterialTheme.typography.labelSmall) }
                )

                // Attendance Tab
                NavigationBarItem(
                    selected = selectedTab == "attendance",
                    onClick = { selectedTab = "attendance" },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == "attendance") Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Attendance"
                        )
                    },
                    label = { Text("Attendance", style = MaterialTheme.typography.labelSmall) }
                )

                // GPA Tab
                NavigationBarItem(
                    selected = selectedTab == "gpa",
                    onClick = { selectedTab = "gpa" },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == "gpa") Icons.Filled.Calculate else Icons.Outlined.Calculate,
                            contentDescription = "GPA"
                        )
                    },
                    label = { Text("GPA Planner", style = MaterialTheme.typography.labelSmall) }
                )

                // AI Study Assistant Tab
                NavigationBarItem(
                    selected = selectedTab == "ai_assistant",
                    onClick = { selectedTab = "ai_assistant" },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == "ai_assistant") Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Assistant"
                        )
                    },
                    label = { Text("AI Assistant", style = MaterialTheme.typography.labelSmall) }
                )

                // About Tab
                NavigationBarItem(
                    selected = selectedTab == "about",
                    onClick = { selectedTab = "about" },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == "about") Icons.Filled.Badge else Icons.Outlined.Badge,
                            contentDescription = "About"
                        )
                    },
                    label = { Text("Card/About", style = MaterialTheme.typography.labelSmall) }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBg)
                .padding(innerPadding)
        ) {
            // High fidelity transitions based on active screen state
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabContent"
            ) { targetTab ->
                when (targetTab) {
                    "dashboard" -> DashboardScreen(
                        authViewModel = authViewModel,
                        attendanceViewModel = attendanceViewModel,
                        gpaViewModel = gpaViewModel,
                        onNavigateToTab = { selectedTab = it }
                    )
                    "attendance" -> AttendanceScreen(
                        viewModel = attendanceViewModel
                    )
                    "gpa" -> GPACalculatorScreen(
                        viewModel = gpaViewModel
                    )
                    "ai_assistant" -> AIStudyAssistantScreen(
                        viewModel = aiStudyViewModel
                    )
                    "about" -> AboutScreen(
                        authViewModel = authViewModel,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
