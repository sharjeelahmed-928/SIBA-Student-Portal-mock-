package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Course
import com.example.data.database.Semester
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.DangerRose
import com.example.ui.viewmodels.GPAViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GPACalculatorScreen(
    viewModel: GPAViewModel
) {
    val semesters by viewModel.semesters.collectAsState()
    val semesterCourses by viewModel.semesterCourses.collectAsState()

    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var activeSemesterIdForCourseAdd by remember { mutableStateOf<Int?>(null) }

    val overallCgpa = viewModel.calculateOverallCGPA(semesterCourses)

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("GPA & CGPA Planner", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = { showAddSemesterDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Semester", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CGPA Gauge Header
                item {
                    CGPAOverviewCard(overallCgpa = overallCgpa, totalSemesters = semesters.size)
                }

                if (semesters.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Empty GPA",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Semesters Tracked",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Click 'Add Semester' above to start tracking courses, credit hours, and calculating your GPA/CGPA.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(semesters) { semester ->
                        val courses = semesterCourses[semester.id] ?: emptyList()
                        SemesterCoursesCard(
                            semester = semester,
                            courses = courses,
                            onAddCourseClick = { activeSemesterIdForCourseAdd = semester.id },
                            onDeleteCourse = { viewModel.deleteCourse(it) },
                            onDeleteSemester = { viewModel.deleteSemester(semester.id) },
                            semesterGpa = viewModel.calculateSemesterGPA(courses)
                        )
                    }
                }
            }

            // Dialog for Add Semester
            if (showAddSemesterDialog) {
                AddSemesterDialog(
                    existingSemestersCount = semesters.size,
                    onDismiss = { showAddSemesterDialog = false },
                    onConfirm = { number, name ->
                        viewModel.addSemester(number, name)
                        showAddSemesterDialog = false
                    }
                )
            }

            // Dialog for Add Course
            if (activeSemesterIdForCourseAdd != null) {
                AddCourseDialog(
                    onDismiss = { activeSemesterIdForCourseAdd = null },
                    onConfirm = { name, credits, grade, points ->
                        viewModel.addCourse(activeSemesterIdForCourseAdd!!, name, credits, grade, points)
                        activeSemesterIdForCourseAdd = null
                    }
                )
            }
        }
    }
}

@Composable
fun CGPAOverviewCard(overallCgpa: Double, totalSemesters: Int) {
    val progressColor = when {
        overallCgpa >= 3.5 -> SuccessEmerald
        overallCgpa >= 2.5 -> WarningAmber
        else -> DangerRose
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Circle Progress Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(96.dp)
            ) {
                val strokeWidth = 10.dp
                val baseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = baseColor,
                        style = Stroke(width = strokeWidth.toPx())
                    )
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = (overallCgpa / 4.0 * 360f).toFloat(),
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.2f", overallCgpa),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = "CGPA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Cumulative Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Status: " + when {
                        overallCgpa >= 3.5 -> "Excellent (First Class Honor)"
                        overallCgpa >= 2.5 -> "Good standing"
                        overallCgpa > 0.0 -> "Needs Improvement"
                        else -> "No course records added"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "Total Semesters: $totalSemesters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SemesterCoursesCard(
    semester: Semester,
    courses: List<Course>,
    onAddCourseClick: () -> Unit,
    onDeleteCourse: (Course) -> Unit,
    onDeleteSemester: () -> Unit,
    semesterGpa: Double
) {
    var expanded by remember { mutableStateOf(true) }

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
            // Semester Title Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .weight(1f)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = semester.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Semester GPA: " + String.format("%.2f", semesterGpa),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onAddCourseClick) {
                        Icon(Icons.Default.AddBox, contentDescription = "Add Course", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteSemester) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Semester", tint = DangerRose)
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (courses.isEmpty()) {
                        Text(
                            text = "No course records added. Click '+' above to insert classes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        courses.forEach { course ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = course.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Credits: ${course.creditHours} • Grade: ${course.grade} (${course.gradePoints} pts)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                IconButton(onClick = { onDeleteCourse(course) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Course",
                                        tint = DangerRose.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSemesterDialog(
    existingSemestersCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    val nextNumber = existingSemestersCount + 1
    var name by remember { mutableStateOf("Semester $nextNumber") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Semester Plan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Creating plan: Semester $nextNumber")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Semester Label") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nextNumber, name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }
    
    val gradesList = listOf(
        Pair("A", 4.0), Pair("A-", 3.7),
        Pair("B+", 3.3), Pair("B", 3.0), Pair("B-", 2.7),
        Pair("C+", 2.3), Pair("C", 2.0), Pair("C-", 1.7),
        Pair("D", 1.0), Pair("F", 0.0)
    )
    var selectedGradeIndex by remember { mutableIntStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Course Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name") },
                    placeholder = { Text("e.g. Distributed Computing") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = credits,
                    onValueChange = { credits = it },
                    label = { Text("Credit Hours") },
                    placeholder = { Text("e.g. 3") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Simple Grade Picker
                Column {
                    Text("Select Grade:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Grade: ${gradesList[selectedGradeIndex].first} (${gradesList[selectedGradeIndex].second} Pts)")
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            gradesList.forEachIndexed { idx, pair ->
                                DropdownMenuItem(
                                    text = { Text("${pair.first} (${pair.second} Pts)") },
                                    onClick = {
                                        selectedGradeIndex = idx
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val crInt = credits.toIntOrNull() ?: 3
                    val gradePair = gradesList[selectedGradeIndex]
                    if (name.isNotBlank()) {
                        onConfirm(name, crInt, gradePair.first, gradePair.second)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
