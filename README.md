# AI Agent Mod for Mindustry

## Overview

The **AI Agent Mod** is a powerful REST API extension for Mindustry that enables any AI agent -- from simple scripts to sophisticated reinforcement learning models -- to observe and control the game in real-time. By exposing game state through HTTP endpoints and accepting actions via POST requests, this mod bridges the gap between Mindustry's gameplay and external AI systems.

Whether you're building an automated base manager, training a neural network to play Mindustry, or creating a cooperative AI companion, this mod provides the interface you need.

## Features

### Game State Observation
- **Player State** -- position, health, inventory, items held, unit type
- **Unit Scanning** -- all units on the map with team, health, position, and type
- **Building Scanning** -- every building with configuration, health, and power status
- **Resource Tracking** -- real-time counts of all resources (copper, lead, silicon, etc.)
- **Map Information** -- map name, dimensions, current wave, game time

### Player Control
- **Movement** -- move the player to any coordinate on the map
- **Building** -- place any block at any position with configurable rotation
- **Mining** -- automatically mine ores at specified locations
- **Shooting** -- fire at targets or suppress an area

### Unit Commands
- **Select Units** -- select units by type, proximity, or custom filters
- **Move** -- command units to move to specific coordinates
- **Attack** -- order units to attack specific targets or positions
- **Patrol** -- set up patrol routes for defensive units

### Building Management
- **Configure** -- change settings on configurable blocks (sorters, routers, etc.)
- **Rotate** -- rotate conveyors, routers, and other directional blocks

### Technical Highlights
- **Thread-safe action queue** -- all actions are queued and executed on the main game thread
- **Non-blocking API** -- HTTP server runs independently of game loop
- **JSON responses** -- all endpoints return clean, parseable JSON
- **Zero dependencies** -- uses Mindustry's built-in networking

## Installation

### Prerequisites
- Mindustry installed (Steam, itch.io, or standalone)
- Java 17 or higher

### Step-by-Step

1. **Download the mod**
   Download `ai-agent-mod.jar` from the [Releases](https://github.com/yourusername/ai-agent-mod/releases) page.

2. **Place in mods folder**
   - **Linux/Mac**: `~/.local/share/Mindustry/mods/`
   - **Windows**: `%appdata%/Mindustry/mods/`
   - **Steam**: Right-click Mindustry > Browse Local Files > `mods/`

3. **Launch Mindustry**
   Start the game normally. The mod loads automatically.

4. **Verify the server**
   The API server starts automatically on port **8089**. You should see a message in the console:
   ```
   [AIAgentMod] API server started on port 8089
   ```

### Building from Source

```bash
git clone https://github.com/yourusername/ai-agent-mod.git
cd ai-agent-mod
./gradlew build
# Output: build/libs/ai-agent-mod.jar
cp build/libs/ai-agent-mod.jar ~/.local/share/Mindustry/mods/
```

## Quick Start

### Check if mod is loaded
```bash
curl http://localhost:8089/
```
Response:
```json
{
  "status": "ok",
  "message": "AI Agent Mod is running",
  "version": "1.0.0"
}
```

### Get game state
```bash
curl http://localhost:8089/api/state
```
Response:
```json
{
  "player": {
    "x": 256.5,
    "y": 312.0,
    "health": 100,
    "maxHealth": 100,
    "unit": "alpha"
  },
  "resources": {
    "copper": 450,
    "lead": 230,
    "graphite": 89
  },
  "map": {
    "name": "Frozen Farlands",
    "width": 500,
    "height": 500,
    "wave": 12
  }
}
```

### Move player
```bash
curl -X POST http://localhost:8089/api/action/move \
  -H "Content-Type: application/json" \
  -d '{"x": 500, "y": 400}'
```

### Build a block
```bash
curl -X POST http://localhost:8089/api/action/build \
  -H "Content-Type: application/json" \
  -d '{"x": 10, "y": 10, "block": "conveyor", "rotation": 0}'
```

## Configuration

The mod can be configured via the `config/ai-agent-mod.properties` file:

| Property | Default | Description |
|----------|---------|-------------|
| `api.port` | `8089` | HTTP server port |
| `api.host` | `0.0.0.0` | Bind address (0.0.0.0 for all interfaces) |
| `scan.maxUnits` | `500` | Maximum units to return per scan |
| `scan.maxBuildings` | `1000` | Maximum buildings to return per scan |
| `action.queueSize` | `256` | Maximum queued actions |

## Architecture

```
+------------------+     HTTP      +------------------+
|   AI Agent       | <-----------> |   API Server     |
|   (Python/JS/    |    JSON      |   (port 8089)    |
|    Rust/etc.)    |               +------------------+
+------------------+                      |
                                          | Thread-safe queue
                                          v
+------------------+     Poll      +------------------+
|   Mindustry      | <-----------  |   Action Queue   |
|   Game Loop      |   Execute     |   Processor      |
+------------------+               +------------------+
```

## Documentation

- **[API Reference](API_REFERENCE.md)** -- Complete endpoint documentation with examples
- **[Developer Guide](DEVELOPER_GUIDE.md)** -- Architecture, extending the mod, contributing
- **[Usage Examples](USAGE_EXAMPLES.md)** -- Practical Python examples for AI agents
- **[Changelog](CHANGELOG.md)** -- Version history and release notes

## System Requirements

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| RAM | 2 GB | 4 GB |
| CPU | Dual-core | Quad-core |
| Mindustry | Build 146+ | Latest stable |
| Java | 17 | 21 |

## Troubleshooting

### Server not starting
- Check if port 8089 is already in use: `lsof -i :8089`
- Change port in config: `api.port=8090`
- Check Mindustry console for error messages

### Connection refused
- Verify Mindustry is running and mod is loaded
- Check firewall settings for the configured port
- Ensure you're using the correct IP (use `127.0.0.1` for local)

### Actions not executing
- The player must be in a game (not in menus)
- Some actions require sufficient resources
- Check response JSON for error messages

## Contributing

Contributions are welcome! Please see the [Developer Guide](DEVELOPER_GUIDE.md) for information on:
- Setting up a development environment
- Adding new API endpoints
- Adding new observation scanners
- Code style guidelines

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Acknowledgments

- [Mindustry](https://github.com/Anuken/Mindustry) -- Anuken and the Mindustry community
- Inspired by the need for AI-accessible game APIs in the strategy genre
