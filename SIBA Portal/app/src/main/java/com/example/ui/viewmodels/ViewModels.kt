package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.Content
import com.example.ai.GeminiClient
import com.example.ai.Part
import com.example.data.database.*
import com.example.data.repository.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// ------------------ VIEWMODEL FACTORY ------------------

class ViewModelFactory(
    private val repository: StudentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository) as T
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> AttendanceViewModel(repository) as T
            modelClass.isAssignableFrom(GPAViewModel::class.java) -> GPAViewModel(repository) as T
            modelClass.isAssignableFrom(AIStudyViewModel::class.java) -> AIStudyViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

// ------------------ AUTH VIEWMODEL ------------------

class AuthViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail = _currentUserEmail.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun login(email: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentUserEmail.value = email
            _isLoggedIn.value = true
            
            // Collect user profile
            repository.getUserProfile(email).collect { profile ->
                if (profile == null) {
                    // Create default profile customized for Sukkur IBA University
                    val defaultProfile = UserProfile(
                        email = email,
                        name = name,
                        university = "Sukkur IBA University",
                        department = "Computer Science",
                        semester = "Semester 3",
                        batch = "Batch 2025",
                        studentId = "023-25-0184",
                        degree = "BS",
                        role = "Student",
                        creationDate = "2026-07-01",
                        lastLogin = "2026-07-01"
                    )
                    repository.saveUserProfile(defaultProfile)
                    _userProfile.value = defaultProfile
                } else {
                    // Update last login
                    val updated = profile.copy(lastLogin = "2026-07-01")
                    repository.saveUserProfile(updated)
                    _userProfile.value = updated
                }
            }
        }
    }

    fun register(
        email: String,
        name: String,
        studentId: String,
        department: String,
        semester: String,
        batch: String,
        university: String,
        phone: String,
        bio: String,
        degree: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = UserProfile(
                email = email,
                name = name,
                studentId = studentId,
                department = department,
                semester = semester,
                batch = batch,
                university = university,
                phone = phone,
                bio = bio,
                degree = degree,
                role = "Student",
                creationDate = "2026-07-01",
                lastLogin = "2026-07-01"
            )
            repository.saveUserProfile(profile)
            _currentUserEmail.value = email
            _userProfile.value = profile
            _isLoggedIn.value = true
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile)
            _userProfile.value = profile
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserEmail.value = null
        _userProfile.value = null
    }
}

// ------------------ ATTENDANCE VIEWMODEL ------------------

class AttendanceViewModel(private val repository: StudentRepository) : ViewModel() {
    val subjects = repository.allSubjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val logs = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addSubject(name: String, targetPercentage: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSubject(
                Subject(
                    name = name,
                    targetPercentage = targetPercentage,
                    totalClasses = 0,
                    attendedClasses = 0
                )
            )
        }
    }

    fun markAttendance(subject: Subject, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Log record
            repository.logAttendance(
                AttendanceLog(
                    subjectId = subject.id,
                    status = status
                )
            )

            // Update subject tallies
            val newTotal = subject.totalClasses + 1
            val newAttended = if (status == "Present" || status == "Late") {
                subject.attendedClasses + 1
            } else {
                subject.attendedClasses
            }

            repository.updateSubject(
                subject.copy(
                    totalClasses = newTotal,
                    attendedClasses = newAttended
                )
            )
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubject(subject)
        }
    }

    fun resetAttendance(subject: Subject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSubject(
                subject.copy(
                    totalClasses = 0,
                    attendedClasses = 0
                )
            )
        }
    }
}

// ------------------ GPA VIEWMODEL ------------------

class GPAViewModel(private val repository: StudentRepository) : ViewModel() {
    val semesters = repository.allSemesters.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Courses mapped by semesterId
    private val _semesterCourses = MutableStateFlow<Map<Int, List<Course>>>(emptyMap())
    val semesterCourses = _semesterCourses.asStateFlow()

    init {
        // Automatically fetch courses for semesters
        viewModelScope.launch(Dispatchers.IO) {
            semesters.collect { semesterList ->
                semesterList.forEach { sem ->
                    repository.getCoursesForSemester(sem.id).collect { courseList ->
                        _semesterCourses.update { current ->
                            current.toMutableMap().apply { put(sem.id, courseList) }
                        }
                    }
                }
            }
        }
    }

    fun addSemester(number: Int, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSemester(Semester(id = number, name = name))
        }
    }

    fun deleteSemester(semesterId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSemester(Semester(id = semesterId, name = "Semester $semesterId"))
        }
    }

    fun addCourse(semesterId: Int, name: String, creditHours: Int, grade: String, points: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCourse(
                Course(
                    semesterId = semesterId,
                    name = name,
                    creditHours = creditHours,
                    grade = grade,
                    gradePoints = points
                )
            )
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCourse(course)
        }
    }

    // Mathematical calculations
    fun calculateSemesterGPA(courses: List<Course>): Double {
        if (courses.isEmpty()) return 0.0
        var totalPoints = 0.0
        var totalCredits = 0
        courses.forEach { course ->
            totalPoints += course.gradePoints * course.creditHours
            totalCredits += course.creditHours
        }
        return if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }

    fun calculateOverallCGPA(allCourses: Map<Int, List<Course>>): Double {
        val flatCourses = allCourses.values.flatten()
        if (flatCourses.isEmpty()) return 0.0
        var totalPoints = 0.0
        var totalCredits = 0
        flatCourses.forEach { course ->
            totalPoints += course.gradePoints * course.creditHours
            totalCredits += course.creditHours
        }
        return if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }
}

// ------------------ AI STUDY VIEWMODEL ------------------

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class Flashcard(
    val front: String,
    val back: String,
    var level: String = "Medium" // Easy, Medium, Hard
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

class AIStudyViewModel(private val repository: StudentRepository) : ViewModel() {
    // General Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I am your University Smart Study AI Assistant. Ask me anything about your university classes, formulas, concepts, or click one of the quick actions below to generate study material!", isUser = false)
    ))
    val chatMessages = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _highThinkingMode = MutableStateFlow(false)
    val highThinkingMode = _highThinkingMode.asStateFlow()

    // Flashcards State
    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards = _flashcards.asStateFlow()

    // Interactive Quiz State
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex = _currentQuizIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished = _quizFinished.asStateFlow()

    private val _showQuizExplanation = MutableStateFlow<Int?>(null) // index of selected answer
    val showQuizExplanation = _showQuizExplanation.asStateFlow()

    fun toggleHighThinking() {
        _highThinkingMode.update { !it }
    }

    // --- Core Chat API Calls ---
    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        
        _chatMessages.update { it + ChatMessage(userPrompt, isUser = true) }
        _isLoading.value = true

        viewModelScope.launch {
            // Build conversation history format for REST client
            val history = _chatMessages.value.filter { !it.isSystem }.takeLast(8).map { msg ->
                Content(parts = listOf(Part(text = msg.message)))
            }

            val systemInstruction = "You are Sukkur IBA University Student Study AI Assistant (SIBAU). Be precise, encouraging, use clear markdown formatting with emojis where appropriate. Provide academic insights and study plans tailored for SIBAU courses."

            val aiResponse = GeminiClient.callGemini(
                prompt = userPrompt,
                systemInstruction = systemInstruction,
                useHighThinking = _highThinkingMode.value,
                conversationHistory = history
            )

            _chatMessages.update { it + ChatMessage(aiResponse, isUser = false) }
            _isLoading.value = false
        }
    }

    // --- Generate Study Flashcards ---
    fun generateFlashcards(topic: String) {
        if (topic.isBlank()) return
        _isLoading.value = true
        _chatMessages.update { it + ChatMessage("Generating interactive flashcards on: $topic...", isUser = false, isSystem = true) }

        viewModelScope.launch {
            val prompt = """
                Generate exactly 5 comprehensive study flashcards about the topic "$topic".
                Respond ONLY with a raw JSON array matching this format:
                [
                  {"front": "Question/Term on Front", "back": "Detailed definition/explanation on Back"}
                ]
                Do not include markdown tags like ```json or ```, respond only with plain JSON string.
            """.trimIndent()

            val responseText = GeminiClient.callGemini(
                prompt = prompt,
                useHighThinking = false
            )

            try {
                // Clean markdown wrappers if returned
                val cleaned = responseText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val jsonArray = JSONArray(cleaned)
                val newList = mutableListOf<Flashcard>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    newList.add(
                        Flashcard(
                            front = obj.optString("front", "Topic"),
                            back = obj.optString("back", "Explanation")
                        )
                    )
                }
                _flashcards.value = newList
                _chatMessages.update { it + ChatMessage("Successfully generated ${newList.size} flashcards! Go to the Flashcards Tab below to study them.", isUser = false) }
            } catch (e: Exception) {
                _chatMessages.update { it + ChatMessage("Flashcard Generation succeeded with text. Here is the compiled information:\n\n$responseText", isUser = false) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Generate Quiz MCQs ---
    fun generateQuiz(topic: String) {
        if (topic.isBlank()) return
        _isLoading.value = true
        _quizQuestions.value = emptyList()
        _currentQuizIndex.value = 0
        _quizScore.value = 0
        _quizFinished.value = false
        _showQuizExplanation.value = null

        _chatMessages.update { it + ChatMessage("Generating a customized study quiz on: $topic...", isUser = false, isSystem = true) }

        viewModelScope.launch {
            val prompt = """
                Generate exactly 5 multiple choice quiz questions about "$topic".
                Each question must have exactly 4 plausible options, a correctAnswerIndex (0 to 3), and a short, informative academic explanation.
                Respond ONLY with a raw JSON array matching this format:
                [
                  {
                    "question": "What is ...?",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswerIndex": 1,
                    "explanation": "Why B is correct..."
                  }
                ]
                Do not include markdown tags like ```json, just return raw JSON text.
            """.trimIndent()

            val responseText = GeminiClient.callGemini(
                prompt = prompt,
                useHighThinking = _highThinkingMode.value
            )

            try {
                val cleaned = responseText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val jsonArray = JSONArray(cleaned)
                val newList = mutableListOf<QuizQuestion>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val optArray = obj.getJSONArray("options")
                    val options = mutableListOf<String>()
                    for (j in 0 until optArray.length()) {
                        options.add(optArray.getString(j))
                    }
                    newList.add(
                        QuizQuestion(
                            question = obj.optString("question", "Question"),
                            options = options,
                            correctAnswerIndex = obj.optInt("correctAnswerIndex", 0),
                            explanation = obj.optString("explanation", "")
                        )
                    )
                }
                _quizQuestions.value = newList
                _chatMessages.update { it + ChatMessage("Interactive quiz successfully generated! You can now start the quiz below.", isUser = false) }
            } catch (e: Exception) {
                _chatMessages.update { it + ChatMessage("I could not compile structured questions, but here are some review questions on $topic:\n\n$responseText", isUser = false) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun answerQuizQuestion(selectedOptionIndex: Int) {
        val currentQuestion = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return
        _showQuizExplanation.value = selectedOptionIndex
        if (selectedOptionIndex == currentQuestion.correctAnswerIndex) {
            _quizScore.update { it + 1 }
        }
    }

    fun nextQuizQuestion() {
        _showQuizExplanation.value = null
        val nextIndex = _currentQuizIndex.value + 1
        if (nextIndex < _quizQuestions.value.size) {
            _currentQuizIndex.value = nextIndex
        } else {
            _quizFinished.value = true
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("Chat cleared! Ask me anything about your university classes, formulas, concepts, or click one of the quick actions below to generate study material!", isUser = false)
        )
    }
}
