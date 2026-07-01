package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Subject
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.DangerRose
import com.example.ui.viewmodels.AttendanceViewModel
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel
) {
    val subjects by viewModel.subjects.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Attendance Manager", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (subjects.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Empty Attendance",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Subjects Added Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add your university courses here to start logging attendance and receive smart target predictions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(subjects) { subject ->
                        SubjectAttendanceCard(
                            subject = subject,
                            onPresent = { viewModel.markAttendance(subject, "Present") },
                            onAbsent = { viewModel.markAttendance(subject, "Absent") },
                            onLate = { viewModel.markAttendance(subject, "Late") },
                            onDelete = { viewModel.deleteSubject(subject) },
                            onReset = { viewModel.resetAttendance(subject) }
                        )
                    }
                }
            }

            // Dialog for adding course/subject
            if (showAddDialog) {
                AddSubjectDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, target ->
                        viewModel.addSubject(name, target)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SubjectAttendanceCard(
    subject: Subject,
    onPresent: () -> Unit,
    onAbsent: () -> Unit,
    onLate: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit
) {
    val percent = if (subject.totalClasses > 0) {
        (subject.attendedClasses.toDouble() / subject.totalClasses) * 100
    } else {
        100.0 // Assume 100% on start
    }

    val isBelowTarget = percent < subject.targetPercentage

    // Calculate prediction metrics
    val predictionText = remember(subject.attendedClasses, subject.totalClasses, subject.targetPercentage) {
        if (subject.totalClasses == 0) {
            "No classes logged yet. Log your first class to start predictions."
        } else {
            val pDecimal = subject.targetPercentage / 100.0
            if (isBelowTarget) {
                // Formula to calculate consecutive classes to attend to reach target
                // (Attended + X) / (Total + X) = Target => X = (Target * Total - Attended) / (1 - Target)
                val totalNeeded = ceil((pDecimal * subject.totalClasses - subject.attendedClasses) / (1.0 - pDecimal)).toInt()
                if (totalNeeded > 0) {
                    "Attend the next $totalNeeded classes consecutively to reach your ${subject.targetPercentage.toInt()}% goal."
                } else {
                    "Attend the next class to boost your percentage."
                }
            } else {
                // Formula to calculate consecutive classes you can safely skip
                // Attended / (Total + Y) = Target => Y = (Attended / Target) - Total
                val totalCanSkip = ((subject.attendedClasses.toDouble() / pDecimal) - subject.totalClasses).toInt()
                if (totalCanSkip > 0) {
                    "You are above target! You can safely miss the next $totalCanSkip classes without falling below ${subject.targetPercentage.toInt()}%."
                } else {
                    "You are exactly on track. Do not miss any upcoming classes."
                }
            }
        }
    }

    val progressColor = when {
        percent >= subject.targetPercentage -> SuccessEmerald
        percent >= subject.targetPercentage - 10.0 -> WarningAmber
        else -> DangerRose
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target Goal: ${subject.targetPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Row {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Stats", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Subject", tint = DangerRose)
                    }
                }
            }

            // Stats View
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${percent.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = "Classes: ${subject.attendedClasses} / ${subject.totalClasses}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Substantial Buttons for marking logs
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onPresent,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAbsent,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRose),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Absent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onLate,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Late", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { (percent / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            )

            // Smart prediction indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isBelowTarget) DangerRose.copy(alpha = 0.08f) else SuccessEmerald.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isBelowTarget) Icons.Default.Info else Icons.Default.CheckCircle,
                        contentDescription = "Prediction",
                        tint = if (isBelowTarget) DangerRose else SuccessEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = predictionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf(75.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subject / Course", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    placeholder = { Text("e.g. Advanced Machine Learning") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Text(
                        text = "Target Attendance Goal: ${target.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = target.toFloat(),
                        onValueChange = { target = it.toDouble() },
                        valueRange = 50f..100f,
                        steps = 9
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, target) },
                enabled = name.isNotBlank()
            ) {
                Text("Add Course")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
