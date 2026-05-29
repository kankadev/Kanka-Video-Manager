# Kanka Video Manager

![screenshot_prototype](https://user-images.githubusercontent.com/84390410/208550722-85bb1514-7222-443a-a7a8-d4a3e7df9d27.png)

Kanka Video Manager allows you to easily sort or sort out media files. We use this program to quickly view, move and delete surveillance videos - even in Windows.

As a base Kanka Video Manager uses vlc / [vlcj](https://github.com/caprica/vlcj) and is developed using Java/JavaFX.

## Features

- **Video Playback**: Play media files with VLCJ
- **Playlist Management**: Drag and drop files to create playlists
- **Video Metadata**: Display duration and file size for all videos
- **File Operations**: Move or delete videos directly from the playlist
- **Keyboard Shortcuts**: Global shortcuts for quick control
- **Video Border**: Dynamic border color based on playback status (green=playing, red=paused/stopped)
- **Speed Control**: Adjust playback speed
- **Skip Functions**: Skip forward/backward in videos

## Keyboard Shortcuts

- **D** - Delete selected video
- **M** - Move selected video
- **N** - Next video
- **Space** - Play / Pause
- **P** - Previous video
- **S** - Stop playback

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- VLC Media Player (required for VLCJ)

### Installation

1. Clone this repository:
```bash
git clone https://github.com/kankadev/Kanka-Video-Manager.git
```

2. Import the project into your IDE (IntelliJ IDEA, Eclipse, etc.)

3. Run the application:
```bash
mvn javafx:run
```

### Building a JAR

To create an executable JAR file:
```bash
mvn package
```

Run the JAR:
```bash
java -jar target/kanka-video-manager-1.0-SNAPSHOT.jar
```

## Development

The project uses Maven for dependency management and JavaFX for the UI. The main controller is `MainController.java` and the FXML layout is in `main_window.fxml`.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

This license was chosen because the project depends on VLCJ, which is licensed under GPL v3. The GPL v3 license ensures that:
- The software remains free for all users
- Any modifications must also be shared under the same license
- Users have the freedom to study, modify, and distribute the software
