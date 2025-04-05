# Lost and Found Mobile App

## Table of Contents
- [Overview](#overview)
- [Features checklist](#features-checklist)
- [Project Structure](#project-structure)
- [Technologies and Tools](#technologies-and-tools)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [License](#license)

## Overview
An Android-based Lost and Found app design to help user find their lost precious items by uploading the lost items on the platform that other user can help find your lost item.

### Features checklist
- [x] User can create an account
- [x] User can login with account and password
- [x] User can browse through list of lost items
- [x] User can post new lost item
- [x] User can call or message the person that post the item with the provided information
- [x] User can see the details of the lost item provided by other users
- [x] User can see the list of their post history and detail of the item posted by them

## Project Structure
```
lost-and-found-mobile-app/
├── app/
│   ├── build.gradle.kts                       # App-level build configuration
│   ├── google-services.json                   # Firebase configuration file
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml            # App manifest with permissions and components
│           ├── java/com/example/lostandfound/ # Main package
│           │   ├── MainActivity.kt            # Entry point of the application
│           │   ├── firebase/
│           │   │   ├── FirebaseManager.kt     # Firebase operations handler (693 lines)
│           │   │   └── firestore.rules        # Firestore security rules
│           │   ├── model/                     # Data models
│           │   │   ├── LostItem.kt            # Lost item data class
│           │   │   ├── User.kt                # User data class
│           │   │   ├── Chat.kt                # Chat data class
│           │   │   └── Message.kt             # Message data class
│           │   ├── ui/
│           │   │   ├── components/            # Reusable UI components
│           │   │   │   └── LostItemCard.kt   # Navigation graph and routes (347 lines)
│           │   │   ├── navigation/
│           │   │   │   └── AppNavigation.kt   # Navigation graph and routes (347 lines)
│           │   │   ├── screens/               # App screens
│           │   │   │   ├── BrowseScreen.kt    # Browse lost items screen (248 lines)
│           │   │   │   ├── ChatListScreen.kt  # List of chats screen (185 lines)
│           │   │   │   ├── ChatScreen.kt      # Individual chat screen (251 lines)
│           │   │   │   ├── DetailScreen.kt    # Item details screen (502 lines)
│           │   │   │   ├── HistoryScreen.kt   # User's history screen (116 lines)
│           │   │   │   ├── LoginScreen.kt     # Login screen (144 lines)
│           │   │   │   ├── PostScreen.kt      # Post new item screen (236 lines)
│           │   │   │   ├── ProfileScreen.kt   # User profile screen (373 lines)
│           │   │   │   ├── SettingsScreen.kt  # App settings screen (280 lines)
│           │   │   │   └── SignupScreen.kt    # Signup screen (197 lines)
│           │   │   └── theme/                 # Theme and styling
│           │   │   │   ├── Color.kt   # User profile screen (373 lines)
│           │   │   │   ├── Theme.kt  # App settings screen (280 lines)
│           │   │   │   └── Type.kt    # Signup screen (197 lines)
│           │   ├── utils/
│           │   │   └── FirebaseStorageUtils.kt # Firebase storage utilities (16 lines)
│           │   └── viewmodel/
│           │       └── LostAndFoundViewModel.kt # Main ViewModel (540 lines)
│           └── res/                           # Resources (layouts, drawables, etc.)
├── build.gradle.kts                           # Project-level build configuration
├── gradle/
│   └── libs.versions.toml                     # Dependency versions management
└── settings.gradle.kts                        # Project settings
```

## Technologies and Tools

<div align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kotlin/kotlin-original.svg" height="30" alt="kotlin logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/jetpackcompose/jetpackcompose-original.svg" height="30" alt="jetpackcompose logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/firebase/firebase-original.svg" height="30" alt="firebase logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/figma/figma-original.svg" height="30" alt="figma logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" height="30" alt="git logo"  />
  <img width="12" />
  <img src="https://skillicons.dev/icons?i=github" height="30" alt="github logo"  />
  <img width="12" />
</div>

## Prerequisites

To set up and run this project, ensure you have:

- **Android Studio**: Latest stable version
- **Gradle**: 8.9 (managed via Gradle wrapper)
- **Android SDK**:
    - SDK Platform 34 (or 35 if available)
    - Build Tools 34.0.0 (or 35.0.0)
    - Minimum SDK: 24
    - Target SDK: 34 (or 35)
- **Java**: JDK 11

## Installation
You can "git clone" this repository via https or ssh
* HTTPS
  ```sh
  git clone https://github.com/Sithi-sak/lost-and-found-mobile-app.git
  ```
* SSH
  ```sh
  git clone git@github.com:Sithi-sak/lost-and-found-mobile-app.git
  ```

> [!WARNING]
> For SSH, you would need to set up a SSH key and add it to Github

## License
This project is licensed under the **Apache 2.0 License**. See the [LICENSE](LICENSE) file for details.
