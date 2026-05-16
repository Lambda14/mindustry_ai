# Changelog

All notable changes to the AI Agent Mod for Mindustry are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2024-01-15

### Added
- **Core API Server** -- HTTP server running on port 8089 with full REST API
- **Game State Observation**
  - `GET /api/state` -- Comprehensive game state snapshot endpoint
  - `GET /api/state/player` -- Player position, health, inventory, unit details
  - `GET /api/state/units` -- Full unit scanning with team/type/proximity filters
  - `GET /api/state/buildings` -- Complete building scan with type/team/radius filters
  - `GET /api/state/resources` -- Core resource inventory with counts and capacity
  - `GET /api/state/map` -- Map metadata, wave info, game rules, team statistics
- **Player Actions**
  - `POST /api/action/move` -- Move player to world coordinates
  - `POST /api/action/build` -- Place blocks with configurable rotation and config
  - `POST /api/action/mine` -- Mine ores at specified locations
  - `POST /api/action/shoot` -- Shoot at targets or suppress fire
- **Unit Commands**
  - `POST /api/action/command` -- Issue move, attack, patrol, idle, mine commands
  - Unit filtering by type, proximity, and team
  - Support for commanding up to configured maximum units
- **Building Management**
  - `POST /api/action/configure` -- Configure sorters, factories, and other blocks
  - `POST /api/action/rotate` -- Rotate directional buildings (0-3)
- **Thread-Safe Action Queue**
  - Concurrent action queuing from HTTP threads
  - Main-thread execution ensuring game state consistency
  - Configurable queue size (default: 256 actions)
  - Action ID tracking for status monitoring
- **Configuration System**
  - `config/ai-agent-mod.properties` for server and scan settings
  - Configurable port, bind address, scan limits, and queue size
- **Error Handling**
  - Structured error responses with codes and messages
  - HTTP status code mapping for all error conditions
  - Input validation for coordinates, block names, and rotations
- **Health Check Endpoint**
  - `GET /` -- Root endpoint for mod status verification
  - Returns version, status, and human-readable message
- **Comprehensive Documentation**
  - README with installation, quick start, and troubleshooting
  - Full API reference with all endpoints, parameters, and examples
  - Developer guide for extending the mod
  - Usage examples with Python code
  - This changelog

### Technical Details
- Built for Mindustry build 146+
- Requires Java 17 or higher
- Zero external dependencies -- uses Mindustry's built-in HTTP server
- Supports all vanilla Mindustry blocks, units, items, and teams
- Compatible with both single-player and multiplayer (when hosting)

---

## Release Notes Template

### [X.Y.Z] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes to existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security improvements

---

## Planned Features

### [1.1.0] - Planned
- WebSocket endpoint for real-time state streaming
- Batch action endpoint for submitting multiple actions at once
- Action status and history tracking
- Enhanced unit AI (formation movement, retreat logic)
- Blueprint support for multi-block construction patterns
- Schematic import and build from `.msch` files

### [1.2.0] - Planned
- Research/tech tree API
- Enhanced multiplayer support
- Mod compatibility layer for modded blocks and units
- Replay/action logging for training data collection
- Performance metrics endpoint

### [2.0.0] - Planned
- GraphQL API alternative for flexible queries
- gRPC support for high-frequency AI training
- Plugin system for custom action handlers
- Built-in analytics and visualization dashboard
- Multi-instance orchestration for AI swarm training

---

[1.0.0]: https://github.com/yourusername/ai-agent-mod/releases/tag/v1.0.0
