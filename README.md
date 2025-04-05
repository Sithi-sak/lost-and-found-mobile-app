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
│   ├── build.gradle.kts                           # App-level build configuration
│   ├── google-services.json                       # Firebase configuration file
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml                # App manifest with permissions and components
│           ├── java/com/example/lostandfound/   
│           │   ├── MainActivity.kt              
│           │   ├── firebase/
│           │   │   ├── FirebaseManager.kt         # Firebase operations handler
│           │   │   └── firestore.rules            # Firestore security rules
│           │   ├── model/                         # Data models
│           │   │   ├── LostItem.kt              
│           │   │   ├── User.kt                  
│           │   │   ├── Chat.kt                  
│           │   │   └── Message.kt               
│           │   ├── ui/
│           │   │   ├── components/                # Reusable UI components
│           │   │   │   └── LostItemCard.kt      
│           │   │   ├── navigation/
│           │   │   │   └── AppNavigation.kt       # Navigation
│           │   │   ├── screens/                   # App screens
│           │   │   │   ├── BrowseScreen.kt      
│           │   │   │   ├── ChatListScreen.kt
│           │   │   │   ├── ChatScreen.kt
│           │   │   │   ├── DetailScreen.kt
│           │   │   │   ├── HistoryScreen.kt
│           │   │   │   ├── LoginScreen.kt
│           │   │   │   ├── PostScreen.kt
│           │   │   │   ├── ProfileScreen.kt
│           │   │   │   ├── SettingsScreen.kt
│           │   │   │   └── SignupScreen.kt
│           │   │   └── theme/                     # Theme and styling
│           │   ├── utils/
│           │   │   └── FirebaseStorageUtils.kt    # Firebase storage utilities
│           │   └── viewmodel/
│           │       └── LostAndFoundViewModel.kt   # Main ViewModel
│           └── res/                             
├── build.gradle.kts                               # Project-level build configuration
├── gradle/
│   └── libs.versions.toml                         # Dependency versions management
└── settings.gradle.kts                          
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
