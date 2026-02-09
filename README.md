# SketchSync - Real-time Collaborative Whiteboard

An Android application supporting multi-user real-time collaborative drawing, focusing on the closed loop of "Collaborative Drawing + Voice Chat + Replay + Permissions".

## Features

- **Real-time Collaboration**: Paths, cursors, and clear events synced in real-time
- **Drawing Tools**: Brush, Eraser, Line, Rectangle, Circle, Text
- **Undo/Redo**: Local operations support undo and redo
- **Pan/Zoom**: Support canvas dragging and pinch-to-zoom
- **Voice Chat**: Talk while drawing
- **Replay**: Playback stroke by stroke, support seeking, pause/resume
- **Permission System**: Owner/Editor/Viewer roles, member management, and kicking
- **Theme**: Switch between Day/Night modes
- **Gallery**: Export and save artworks to the cloud
- **Join Notifications**: Notify when someone joins the room (toggle in settings)

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + AndroidView
- **Architecture**: MVVM
- **Backend**: Firebase (Auth, Firestore, Realtime Database, Storage)
- **Voice**: Agora SDK
- **DI**: Hilt

## Getting Started

### Prerequisites

1. Android Studio Hedgehog or higher
2. JDK 17+
3. Firebase project configuration
4. Agora account and App ID

### Setup Steps

1. Clone the project
2. Create a project in Firebase Console and enable Auth / Firestore / Realtime Database / Storage
3. Download `google-services.json` and place it in the `app/` directory
4. Add Agora App ID in `local.properties`:
   ```properties
   AGORA_APP_ID=your_agora_app_id
   ```
5. Sync Gradle and run

## Project Structure

```
app/src/main/java/com/sketchsync/
├── data/           # Data Layer
│   ├── model/      # Data Models
│   └── repository/ # Repositories
├── di/             # Dependency Injection
├── ui/             # UI Layer
│   ├── auth/       # Login/Register
│   ├── room/       # Room List/Creation
│   ├── canvas/     # Canvas & Tools
│   ├── profile/    # Profile & Settings
│   ├── gallery/    # Gallery
│   └── theme/      # Theme Configuration
└── util/           # Utilities
```

## License

MIT License
