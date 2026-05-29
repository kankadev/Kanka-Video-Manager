# Kanka Video Manager

![screenshot_prototype](https://user-images.githubusercontent.com/84390410/208550722-85bb1514-7222-443a-a7a8-d4a3e7df9d27.png)

Kanka Video Manager allows you to easily sort or sort out media files. We use this program to quickly view, move and delete surveillance videos - even in Windows.

## Why This Program Exists

When reviewing large collections of surveillance videos, using a standard media player like VLC is inefficient:

**The Problem:**
- You have to manually close the player, find the file in Explorer, delete it, and reopen the player for each video
- Windows prevents deleting files that are in use by the media player
- You lose track of which videos you've already reviewed
- The process is slow and error-prone

**The Solution:**
Kanka Video Manager solves these problems by:
- **Mark for Deletion**: Click "Delete" to mark a video for deletion - the next video starts playing immediately
- **Batch Processing**: Videos are only actually deleted when you click "Process all files in playlist now!"
- **No File Lock Issues**: Since videos are stopped before deletion, Windows doesn't block the operation
- **Progress Tracking**: You can see exactly which videos you've reviewed and marked
- **Efficient Workflow**: Review dozens of videos in the time it used to take to review a few

This makes it ideal for quickly sifting through surveillance footage to decide what to keep and what to delete.

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

- Java 17 or higher (not required for MSI installer)
- VLC Media Player (required for VLCJ - must be installed separately)
- Maven 3.6 or higher (only for building from source)

### Installation

**Option 1: Using the MSI Installer (Recommended)**

1. Download the latest MSI installer from the [Releases](https://github.com/kankadev/Kanka-Video-Manager/releases) page
2. Install VLC Media Player (required for video playback)
3. Run the MSI installer
4. Launch the application from the Start menu

**Troubleshooting:**

If the application doesn't start or the console window closes too quickly:

1. Open Command Prompt as Administrator
2. Navigate to the installation directory (usually `C:\Program Files\KankaVideoManager`)
3. Run the application with: `KankaVideoManager.exe`
4. Copy any error messages and report them

**Common Issues:**

- **Application doesn't start**: Make sure VLC Media Player is installed and in your PATH
- **VLC not found error**: Install VLC Media Player from https://www.videolan.org/vlc/
- **Java errors**: The MSI includes Java, but if you see Java errors, try the fat JAR instead

**Option 2: Building from Source**

1. Clone this repository:
```bash
git clone https://github.com/kankadev/Kanka-Video-Manager.git
```

2. Install VLC Media Player (required for VLCJ)

3. Import the project into your IDE (IntelliJ IDEA, Eclipse, etc.)

4. Run the application:
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
