# AutoShutdown Mod for Minecraft 1.21.1 NeoForge

A complete Minecraft mod that automatically shuts down the server when no players are online.

## 🚀 Features

- **✅ Custom Command**: Use `/autoshutdown` to start detection
- **✅ Multiplayer Detection**: Works with both single-player hosts and dedicated servers
- **✅ Player List Monitoring**: Continuously monitors player count changes
- **✅ Configurable Timer**: Default 5-minute countdown (fully configurable)
- **✅ Warning Messages**: Countdown warnings every minute with final 1-minute warning
- **✅ Recovery Mechanism**: Auto-cancels when players join, restarts when they leave
- **✅ Cross-Platform Shutdown**: Optional computer shutdown (Windows/Mac/Linux)
- **✅ Configuration System**: Full TOML config file support
- **✅ Permission System**: Admin commands require OP permissions

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/autoshutdown` | Start auto shutdown detection | Anyone |
| `/autoshutdown status` | Check current shutdown status | Anyone |
| `/autoshutdown cancel` | Cancel active countdown | OP Level 2 |
| `/autoshutdown set <minutes>` | Set custom wait time | OP Level 2 |

## ⚙️ Configuration

All settings are in `config/autoshutdown-common.toml`:

```toml
[General Settings]
# The command name to trigger auto shutdown detection
commandName = "autoshutdown"

# Time to wait in minutes before shutting down when no players are online
# Range: 1-60 minutes
waitTimeMinutes = 5

# Whether to shutdown the computer after closing the server
# WARNING: This will attempt to shutdown the entire computer!
shutdownComputer = false

# Message prefix for all auto shutdown messages
messagePrefix = "[AutoShutdown] "
```

## 🔧 Installation & Setup

### Prerequisites
- Minecraft 1.21.1
- NeoForge 21.1.80 or higher
- Java 21 or higher

### Build Instructions

1. **Set up NeoForge development environment:**
   ```bash
   # Download NeoForge MDK for 1.21.1
   # Extract and replace the build.gradle with the one from this mod
   
   # Or use the NeoForge userdev plugin:
   plugins {
       id 'net.neoforged.gradle.userdev' version '7.0.80'
   }
   ```

2. **Build the mod:**
   ```bash
   ./gradlew build
   ```

3. **Install:**
   - Copy the generated JAR from `build/libs/` to your server's `mods/` folder
   - Start the server

### Alternative: Manual Setup

If you have issues with the build system, you can manually integrate this mod:

1. Copy all source files to your NeoForge mod project
2. Ensure your `build.gradle` includes NeoForge dependencies
3. Update the `mods.toml` file with your mod details
4. Build using your existing NeoForge setup

## 🎯 How It Works

### Detection Logic

1. **Single Player Mode**: 
   - Detects if only the host remains (player count ≤ 1)
   - Shows error message - not supported in single player

2. **Dedicated Server Mode**:
   - Detects when no players are online (player count = 0)
   - Starts countdown timer

### Countdown Process

```
Player leaves → Check player count → Start 5-minute timer
    ↓
Send warning messages every minute
    ↓
Final warning at 1 minute
    ↓
Save world → Shutdown server → Optional computer shutdown
```

### Recovery Mechanism

- **Player joins during countdown**: Timer cancels, "Auto shutdown cancelled" message
- **Player leaves again**: Timer restarts with full duration
- **Admin can cancel**: `/autoshutdown cancel` command

## 🔒 Safety Features

- **World Saving**: Automatically saves all chunks before shutdown
- **Graceful Shutdown**: 2-second delay ensures messages are sent
- **Permission Control**: Admin commands require OP level 2
- **Config Safety**: Computer shutdown disabled by default
- **Cross-Platform**: Works on Windows, macOS, and Linux

## 🖥️ Computer Shutdown Commands

The mod uses OS-specific shutdown commands:

- **Windows**: `shutdown /s /t 30` (30-second delay)
- **macOS**: `sudo shutdown -h +1` (1-minute delay)  
- **Linux**: `sudo shutdown -h +1` (1-minute delay)

⚠️ **Warning**: Enable computer shutdown only if you understand the risks!

## 🐛 Troubleshooting

### Common Issues

1. **Mod doesn't load**:
   - Check NeoForge version compatibility
   - Ensure Java 21 is installed
   - Verify mod is in correct `mods/` folder

2. **Command not found**:
   - Check `commandName` in config file
   - Restart server after config changes
   - Verify OP permissions for admin commands

3. **Auto shutdown not working**:
   - Check server type detection in logs
   - Verify player count logic
   - Check if countdown is active with `/autoshutdown status`

4. **Computer shutdown not working**:
   - Verify `shutdownComputer = true` in config
   - Check system permissions for shutdown commands
   - Ensure running with appropriate privileges

### Debug Logging

Enable debug logging by adding to `config/logging.properties`:
```
com.autoshutdown.level=DEBUG
```

## 📁 Project Structure

```
src/main/
├── java/com/autoshutdown/
│   ├── AutoShutdown.java              # Main mod class
│   ├── command/
│   │   └── AutoShutdownCommand.java    # Command handlers
│   ├── config/
│   │   ├── AutoShutdownConfig.java     # Configuration system
│   │   └── ModMenuIntegration.java     # Optional ModMenu support
│   └── events/
│       └── PlayerEventHandler.java     # Player join/leave events
└── resources/
    ├── META-INF/
    │   └── neoforge.mods.toml          # Mod metadata
    ├── assets/autoshutdown/
    │   └── icon.png                    # Mod icon (128x128)
    └── autoshutdown-common.toml        # Default config
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- NeoForge team for the excellent modding platform
- Minecraft community for inspiration and feedback
- Contributors who help improve this mod

---

**Note**: This mod is designed for server administrators who want to automatically manage server resources. Always test in a development environment before using on production servers.