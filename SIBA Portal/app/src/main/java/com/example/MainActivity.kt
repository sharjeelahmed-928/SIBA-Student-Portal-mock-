package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.StudentRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 1. Initialize local database
        val database = AppDatabase.getDatabase(this)
        
        // 2. Initialize student repository
        val repository = StudentRepository(
            userProfileDao = database.userProfileDao(),
            subjectDao = database.subjectDao(),
            attendanceLogDao = database.attendanceLogDao(),
            semesterDao = database.semesterDao(),
            courseDao = database.courseDao()
        )
        
        // 3. Setup factory
        val viewModelFactory = ViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                // Initialize ViewModels with the custom repository factory
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                val attendanceViewModel: AttendanceViewModel = viewModel(factory = viewModelFactory)
                val gpaViewModel: GPAViewModel = viewModel(factory = viewModelFactory)
                val aiStudyViewModel: AIStudyViewModel = viewModel(factory = viewModelFactory)

                var currentScreen by remember { mutableStateOf("splash") }
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                // Automatic session check on start
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn && currentScreen == "login") {
                        currentScreen = "main"
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "AppScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "splash" -> SplashScreen {
                                currentScreen = if (isLoggedIn) "main" else "login"
                            }
                            "login" -> LoginScreen(
                                authViewModel = authViewModel,
                                onNavigateToRegister = { currentScreen = "register" },
                                onLoginSuccess = { currentScreen = "main" }
                            )
                            "register" -> RegisterScreen(
                                authViewModel = authViewModel,
                                onNavigateToLogin = { currentScreen = "login" },
                                onRegisterSuccess = { currentScreen = "main" }
                            )
                            "main" -> MainContainerScreen(
                                authViewModel = authViewModel,
                                attendanceViewModel = attendanceViewModel,
                                gpaViewModel = gpaViewModel,
                                aiStudyViewModel = aiStudyViewModel,
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds timer
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )

            Text(
                text = "Sukkur IBA University",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Student Portal & AI Academic Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Subtly and elegantly display developer credits at the footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Designed & Developed by",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
            Text(
                text = "Sharjeel Ahmed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }
}
