# 🎓 University Smart Student

<p align="center">
  <img src="assets/logo.png" alt="University Smart Student Logo" width="180"/>
</p>

<p align="center">
An intelligent AI-powered Android application designed to help university students manage their academic life efficiently.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-purple"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue"/>
  <img src="https://img.shields.io/badge/AI-Gemini-orange"/>
  <img src="https://img.shields.io/badge/License-MIT-red"/>
</p>

---

# 📖 About

**University Smart Student** is an AI-powered personal academic assistant built specifically for university students.

The application combines modern Android development with artificial intelligence to simplify daily academic tasks such as studying, note organization, scheduling, productivity, and intelligent assistance.

Whether you're preparing for exams, organizing assignments, or asking AI for help, University Smart Student keeps everything in one place.

---

# ✨ Features

- 🤖 AI Academic Assistant
- 📚 Smart Notes
- 📝 Assignment Manager
- 📅 Timetable & Schedule
- ⏰ Reminders
- 📖 Study Planner
- 🎯 Goal Tracking
- 📂 Subject Management
- 📊 Productivity Dashboard
- 🌙 Dark & Light Mode
- 🔒 Secure Local Storage
- ☁ Cloud Synchronization *(optional)*
- 🔍 Intelligent Search
- ⚡ Fast & Modern UI
- 📱 Material Design 3

---

# 🛠 Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin | Android Development |
| Jetpack Compose | UI Framework |
| Material 3 | UI Components |
| Android Jetpack | Architecture Components |
| Gemini API | AI Integration |
| MVVM | App Architecture |
| Kotlin Coroutines | Asynchronous Programming |
| Room Database | Local Storage |
| Firebase *(Optional)* | Authentication & Cloud Sync |

---

# 📂 Project Structure

```
app/
 ├── ui/
 ├── screens/
 ├── navigation/
 ├── viewmodel/
 ├── data/
 ├── repository/
 ├── ai/
 ├── utils/
 └── MainActivity.kt
```

---

# 🚀 Getting Started

## Prerequisites

Before running the project, make sure you have:

- Android Studio (Latest Stable Version)
- Android SDK
- JDK 17+
- Git
- Gemini API Key

---

# ⚙ Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/university-smart-student.git
```

```bash
cd university-smart-student
```

---

### 2. Open in Android Studio

- Launch **Android Studio**
- Click **Open**
- Select the project folder
- Wait for Gradle Sync to complete

---

### 3. Configure Environment Variables

Create a file named:

```
.env
```

inside the project root.

Add your Gemini API key:

```env
GEMINI_API_KEY=YOUR_API_KEY_HERE
```

You can use `.env.example` as a reference.

---

### 4. Update Gradle Configuration

Open:

```
app/build.gradle.kts
```

Remove the following line if present:

```kotlin
signingConfig = signingConfigs.getByName("debugConfig")
```

---

### 5. Build the Project

Allow Gradle to download all dependencies.

---

### 6. Run the Application

Connect an Android device or start an emulator, then click:

```
Run ▶
```

or press

```
Shift + F10
```

---

# 🔑 Environment Variables

| Variable | Description |
|----------|-------------|
| GEMINI_API_KEY | Your Gemini API Key |

---

# 📱 Minimum Requirements

- Android 8.0 (API 26)
- 4 GB RAM Recommended
- Internet connection for AI features

---

# 🧩 Architecture

The project follows **MVVM (Model–View–ViewModel)** architecture to ensure clean, scalable, and maintainable code.

```
UI
 │
 ▼
ViewModel
 │
 ▼
Repository
 │
 ├── Local Database
 └── AI Services
```

---

# 📸 Screenshots

> Add your application screenshots here.

```
screenshots/
    home.png
    dashboard.png
    ai_chat.png
    notes.png
```

---

# 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch

```bash
git checkout -b feature/new-feature
```

3. Commit your changes

```bash
git commit -m "Add new feature"
```

4. Push to GitHub

```bash
git push origin feature/new-feature
```

5. Open a Pull Request

---

# 🐞 Reporting Issues

Found a bug or have a feature request?

Please open an issue describing:

- Expected behavior
- Actual behavior
- Device information
- Screenshots (if applicable)

---

# 📜 License

This project is licensed under the MIT License.

See the `LICENSE` file for details.

---

# 👨‍💻 Developer

**Sharjeel Ahmed**

Computer Science (AI) Student

---

# ⭐ Support

If you found this project helpful, consider giving it a ⭐ on GitHub.

It helps support future development.

---

<p align="center">
Made with ❤️ for students, by a student.
</p>
