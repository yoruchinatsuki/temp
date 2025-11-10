# AutoShutdown Mod for Minecraft 1.21.1 NeoForge

A Minecraft mod that automatically shuts down the server when no players are online.

## Features

- **Custom Command**: Use `/autoshutdown` to start detection
- **Multiplayer Detection**: Works with both single-player hosts and dedicated servers
- **Player List Monitoring**: Continuously monitors player count
- **Configurable Timer**: Default 5-minute countdown (configurable)
- **Warning Messages**: Countdown warnings every minute
- **Recovery Mechanism**: Auto-cancels when players join
- **Cross-Platform Shutdown**: Optional computer shutdown (Windows/Mac/Linux)
- **Configuration System**: Full config file support

## Commands

- `/autoshutdown` - Start auto shutdown detection
- `/autoshutdown status` - Check current status
- `/autoshutdown cancel` - Cancel active countdown (requires OP)
- `/autoshutdown set <minutes>` - Set custom wait time (requires OP)

## Configuration

All settings can be configured in `config/autoshutdown-common.toml`:

- `commandName` - Command name (default: "autoshutdown")
- `waitTimeMinutes` - Wait time before shutdown (default: 5)
- `shutdownComputer` - Whether to shutdown computer (default: false)
- `messagePrefix` - Message prefix (default: "[AutoShutdown] ")

## Installation

1. Install NeoForge 21.1.80 for Minecraft 1.21.1
2. Place the mod JAR file in the `mods` folder
3. Start the server/client

## Usage

1. Start your server as usual
2. Use `/autoshutdown` command to enable auto shutdown
3. The mod will monitor player count and automatically shut down when no players are online
4. Configure settings in the config file as needed

## Permissions

- Basic usage: No special permissions required
- Admin commands (`cancel`, `set`): Require OP level 2

## Compatibility

- Minecraft: 1.21.1
- NeoForge: 21.1.80+
- Cross-platform: Windows, macOS, Linux

## Safety Features

- World saving before shutdown
- Configurable computer shutdown (disabled by default)
- Automatic cancellation when players join
- Clear warning messages

## License

MIT License