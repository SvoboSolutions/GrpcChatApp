# GrpcApp

Eine Android-Chat-Anwendung mit gRPC für Echtzeit-Kommunikation.

## Features

- Echtzeit-Chat zwischen mehreren Android-Geräten
- gRPC-basierte Client-Server-Kommunikation
- Jetpack Compose UI
- Multi-User-Support

## Setup

### Voraussetzungen
- Android Studio Narwhal (2025.1.1) oder neuer
- JDK 17+
- Gradle 8.0+

### Installation
1. Projekt in Android Studio öffnen
2. Gradle-Dateien synchronisieren
3. Projekt bauen: `./gradlew build`
4. App installieren: `./gradlew installDebug`

## Verwendung

### ChatClient API
connect(host: String, port: Int)              // Verbindung herstellen
sendMessage(username: String, message: String) // Nachricht senden
joinChat(username: String): Flow<ChatMessage>  // Chat beitreten
disconnect()                                   // Verbindung schließen

#### Netzwerk-Konfiguration

Android Emulator: 10.0.2.2:9090
Physisches Gerät: 192.168.1.XXX:9090

### Technologie

- Kotlin Coroutines für asynchrone Operationen
- gRPC für Client-Server-Kommunikation
- Protocol Buffers für Nachrichtenserialisierung
- MVVM-Architektur mit StateFlow