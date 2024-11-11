# LOCO - Keep It Forever 📝

A modern, feature-rich note-taking Android application built with Jetpack Compose that allows users to create, manage, and sync notes across devices.

App Screenshots 📱
<div align="center">
<table>
  <tr>
    <td><img src="screenshots/login_screen.png" alt="Login Screen" width="240"/></td>
    <td><img src="screenshots/home_screen.png" alt="Home Screen" width="240"/></td>
    <td><img src="screenshots/note_detail.png" alt="Note Detail" width="240"/></td>
    <td><img src="screenshots/navigation_drawer.png" alt="Navigation Drawer" width="240"/></td>
  </tr>
  <tr>
    <td align="center"><b>Login Screen</b></td>
    <td align="center"><b>Home Screen</b></td>
    <td align="center"><b>Note Detail</b></td>
    <td align="center"><b>Navigation Drawer</b></td>
  </tr>
</table>
</div>

## Features 🌟

- **User Authentication**
  - Google Sign-in integration
  - Email/password authentication
  - User profile management

- **Note Management**
  - Create, edit, and delete notes
  - Rich text formatting
  - Image attachments
  - Voice-to-text input
  - Category organization
  - Search functionality
  - Share notes

- **Sync & Backup**
  - Real-time synchronization with Firebase
  - Offline support with local storage
  - Automatic background sync
  - Data persistence

- **UI/UX**
  - Material Design 3 implementation
  - Dark/Light theme support
  - Customizable fonts
  - Grid layout for notes
  - Intuitive navigation drawer
  - Responsive design
  - Category-based filtering

- **Additional Features**
  - Note reminders with notifications
  - Speech-to-text functionality
  - Image handling and storage
  - Note categorization

## Tech Stack 🛠️

- **Frontend**
  - Jetpack Compose
  - Material Design 3
  - Coil for image loading
  - Navigation Component

- **Backend & Data**
  - Firebase Authentication
  - Cloud Firestore
  - Room Database
  - WorkManager for background tasks
  - ViewModel & StateFlow

- **Architecture**
  - MVVM (Model-View-ViewModel)
  - Repository Pattern
  - Clean Architecture principles
  - Dependency Injection

## Project Structure 📁

```
app/
├── model/
│   ├── firebase/    # Firebase related classes
│   ├── network/     # Network handling and repository
│   ├── notifications/ # Notification workers
│   └── room/        # Local database
├── ui/
│   ├── screens/     # Compose UI screens
│   └── theme/       # Theme and styling
└── viewModel/       # ViewModels for screens
```

## Getting Started 🚀

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK with minimum API level 21

### Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/loco.git
```

2. Open the project in Android Studio

3. Set up Firebase:
   - Create a new Firebase project
   - Add your Android app to Firebase project
   - Download `google-services.json` and place it in the app module
   - Enable Authentication and Firestore in Firebase Console

4. Build and run the project

### Configuration

Create a `local.properties` file in the project root and add:
```properties
sdk.dir=YOUR_ANDROID_SDK_PATH
FIREBASE_WEB_CLIENT_ID=YOUR_FIREBASE_WEB_CLIENT_ID
```

## Architecture Overview 🏗️

The app follows MVVM architecture with Clean Architecture principles:

- **UI Layer**: Compose UI components and screens
- **ViewModel Layer**: Manages UI state and business logic
- **Repository Layer**: Single source of truth for data
- **Data Layer**: Room database and Firebase services

## Contributing 🤝

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Rights and Usage ⚠️

This project is currently unlicensed, which means that by default, all rights are reserved. No permission is granted to use, modify, or share this code without explicit permission from the project owner.

## Acknowledgments 👏

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase](https://firebase.google.com/)
- [Material Design 3](https://m3.material.io/)
- [Android Jetpack](https://developer.android.com/jetpack)

## Contact 📧

Suraj Barik - [@sjb_s2003]([https://twitter.com/yourtwitter](https://x.com/sjb_s2003)) - surajbarik2003@gmail.com 

Project Link: [https://github.com/sjbs2003/loco](https://github.com/sjbs2003/loco)

---
Made with ❤️ by [Suraj Barik]
