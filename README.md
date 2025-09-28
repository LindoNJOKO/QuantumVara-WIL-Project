
# NURTURE_NEST

NURTURE_NEST is an Android application designed to manage and support educational interactions for **Parents, Teachers, and Admins**. 
It provides a centralized platform with role-specific dashboards, real-time updates, and seamless navigation.

---

## Features

- **Splash Screen**: Welcome screen before entering the app.
- **User Registration & Login**: Supports multiple user types (Parent, Teacher, Admin).
- **Role-based Dashboards**: Each user type sees a personalized dashboard with relevant actions.
- **Persistent Bottom Navigation**: Includes Dashboard, Settings, Calendar, and Profile.
- **Fragments Architecture**: Modular design using Fragments for scalable UI management.
- **Edge-to-edge support**: Full-screen UI with proper padding for system bars.
- **Secure data handling**: Implements best practices for managing user preferences and session data.

---

## Project Structure

```

com.example.nurture\_nest
│
├── activities
│    ├── MainActivity.kt
│    ├── SplashScreen.kt
│    ├── Login.kt
│    └── Register.kt
│
├── Fragments
│    ├── ParentDashboardFragment.kt
│    ├── TeacherDashboardFragment.kt
│    ├── AdminDashboardFragment.kt
│    ├── SettingsFragment.kt
│    ├── CalendarFragment.kt
│    └── ProfileFragment.kt
│
├── res
│    ├── drawable
│    ├── layout
│    ├── mipmap
│    └── menu
│
├── Gradle & Build files
└── README.md

````

---

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/LindoNJOKO/QuantumVara-WIL-Project.git
````

2. Open the project in **Android Studio**.
3. Ensure the **Gradle wrapper version 8.13** is installed (should auto-download).
4. Build and run the project on an emulator or physical device.

---

## Technologies Used

* **Kotlin**: Primary language for Android development.
* **Android Jetpack Components**: Fragments, ViewBinding, and Material Design.
* **Gradle**: Build system with Kotlin DSL (`build.gradle.kts`).

---

## Contributing

Feel free to open issues or submit pull requests for improvements. Please follow the repository structure and naming conventions for fragments and activities.

---

## License

This project is for educational purposes. All rights reserved by the author.

````

---

You can save this as `README.md` in the **root of your NURTURE_NEST project**, then commit and push:

```bash
git add README.md
git commit -m "Add README for NURTURE_NEST project"
git push
