# API Reference

Complete reference for all REST API endpoints provided by the AI Agent Mod.

**Base URL:** `http://localhost:8089`  
**Content-Type:** `application/json`  
**Encoding:** UTF-8

---

## Table of Contents

- [Root & Health](#root--health)
- [Game State](#game-state)
- [Observation Endpoints](#observation-endpoints)
  - [Player State](#player-state)
  - [Units](#units)
  - [Buildings](#buildings)
  - [Resources](#resources)
  - [Map Info](#map-info)
- [Action Endpoints](#action-endpoints)
  - [Move Player](#move-player)
  - [Build](#build)
  - [Mine](#mine)
  - [Shoot](#shoot)
  - [Command Units](#command-units)
  - [Configure Building](#configure-building)
  - [Rotate Building](#rotate-building)
- [Error Handling](#error-handling)
- [Data Types](#data-types)

---

## Root & Health

### GET /

Check if the mod is loaded and the API server is running.

**URL:** `/`  
**Method:** `GET`  
**Auth:** None

#### Response

| Field | Type | Description |
|-------|------|-------------|
| `status` | string | Always `"ok"` on success |
| `message` | string | Human-readable status message |
| `version` | string | Mod version string |

#### Example

**Request:**
```bash
curl http://localhost:8089/
```

**Response:**
```json
{
  "status": "ok",
  "message": "AI Agent Mod is running",
  "version": "1.0.0"
}
```

---

## Game State

### GET /api/state

Get a comprehensive snapshot of the current game state. This is a convenience endpoint that aggregates data from multiple scanners into a single response.

**URL:** `/api/state`  
**Method:** `GET`  
**Auth:** None

#### Response

| Field | Type | Description |
|-------|------|-------------|
| `player` | PlayerState | Current player state |
| `resources` | object | Resource name -> count mapping |
| `map` | MapInfo | Map metadata and game progress |
| `unitCount` | integer | Total units on the map |
| `buildingCount` | integer | Total buildings on the map |
| `timestamp` | integer | Game tick timestamp |

#### Example

**Request:**
```bash
curl http://localhost:8089/api/state
```

**Response:**
```json
{
  "player": {
    "x": 256.5,
    "y": 312.0,
    "health": 100,
    "maxHealth": 100,
    "shield": 0,
    "unit": "alpha",
    "team": "sharded",
    "dead": false,
    "flying": false,
    "mining": null,
    "velocityX": 0.5,
    "velocityY": -0.2,
    "rotation": 45.0,
    "item": null,
    "itemCapacity": 70
  },
  "resources": {
    "copper": 450,
    "lead": 230,
    "graphite": 89,
    "silicon": 156,
    "metaglass": 45,
    "titanium": 78,
    "thorium": 23,
    "surge-alloy": 12,
    "phase-fabric": 8,
    "plastanium": 34
  },
  "map": {
    "name": "Frozen Farlands",
    "width": 500,
    "height": 500,
    "wave": 12,
    "waveTime": 47.5,
    "wavesToSurvive": 30,
    "gameTime": 1847.3,
    "paused": false
  },
  "unitCount": 47,
  "buildingCount": 312,
  "timestamp": 110839
}
```

---

## Observation Endpoints

### Player State

#### GET /api/state/player

Get detailed information about the player-controlled unit.

**URL:** `/api/state/player`  
**Method:** `GET`

##### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `x` | float | X coordinate in world units |
| `y` | float | Y coordinate in world units |
| `health` | float | Current health |
| `maxHealth` | float | Maximum health |
| `shield` | float | Current shield amount |
| `unit` | string | Unit type name (e.g., `alpha`, `flare`) |
| `team` | string | Team name |
| `dead` | boolean | Whether the unit is dead |
| `flying` | boolean | Whether the unit can fly |
| `mining` | object\|null | Current mining target `{x, y, ore}` or null |
| `velocityX` | float | X velocity |
| `velocityY` | float | Y velocity |
| `rotation` | float | Facing direction in degrees |
| `item` | string\|null | Currently held item name |
| `itemCapacity` | integer | Maximum items that can be held |
| `tileX` | integer | X tile coordinate |
| `tileY` | integer | Y tile coordinate |

##### Example

```bash
curl http://localhost:8089/api/state/player
```

```json
{
  "x": 256.5,
  "y": 312.0,
  "health": 100,
  "maxHealth": 100,
  "shield": 0,
  "unit": "alpha",
  "team": "sharded",
  "dead": false,
  "flying": false,
  "mining": {
    "x": 250,
    "y": 310,
    "ore": "copper"
  },
  "velocityX": 0.0,
  "velocityY": 0.0,
  "rotation": 90.0,
  "item": "copper",
  "itemCapacity": 70,
  "tileX": 25,
  "tileY": 31
}
```

---

### Units

#### GET /api/state/units

Scan and return all visible units on the map.

**URL:** `/api/state/units`  
**Method:** `GET`  
**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `team` | string | (all) | Filter by team name (`sharded`, `crux`, etc.) |
| `type` | string | (all) | Filter by unit type (`dagger`, `flare`, etc.) |
| `x` | float | (none) | Center X for radius filter |
| `y` | float | (none) | Center Y for radius filter |
| `radius` | float | (none) | Radius for proximity filter (world units) |
| `limit` | integer | `500` | Maximum number of units to return |

##### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `units` | Unit[] | Array of unit objects |
| `count` | integer | Total number of units returned |
| `filtered` | boolean | Whether results were filtered |

**Unit object fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | integer | Unique unit ID |
| `x` | float | X coordinate |
| `y` | float | Y coordinate |
| `health` | float | Current health |
| `maxHealth` | float | Maximum health |
| `type` | string | Unit type name |
| `team` | string | Team name |
| `flying` | boolean | Whether the unit can fly |
| `dead` | boolean | Whether the unit is dead |
| `rotation` | float | Facing direction in degrees |
| `velocityX` | float | X velocity |
| `velocityY` | float | Y velocity |
| `payload` | string\|null | Carried payload description |
| `mineTile` | object\|null | `{x, y, ore}` if mining |
| `spawnedByCore` | boolean | Whether spawned from a core |

##### Examples

**All units:**
```bash
curl http://localhost:8089/api/state/units
```

**Enemy units only:**
```bash
curl "http://localhost:8089/api/state/units?team=crux"
```

**Nearby units in radius:**
```bash
curl "http://localhost:8089/api/state/units?x=256&y=312&radius=100"
```

**Flare units only:**
```bash
curl "http://localhost:8089/api/state/units?type=flare&limit=50"
```

---

### Buildings

#### GET /api/state/buildings

Scan and return all buildings on the map.

**URL:** `/api/state/buildings`  
**Method:** `GET`  
**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `team` | string | (all) | Filter by team name |
| `type` | string | (all) | Filter by block type (`core`, `conveyor`, `turret`, etc.) |
| `x` | float | (none) | Center X for radius filter |
| `y` | float | (none) | Center Y for radius filter |
| `radius` | float | (none) | Radius for proximity filter |
| `limit` | integer | `1000` | Maximum buildings to return |

##### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `buildings` | Building[] | Array of building objects |
| `count` | integer | Total buildings returned |
| `filtered` | boolean | Whether results were filtered |

**Building object fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | integer | Unique building ID |
| `x` | integer | X tile coordinate |
| `y` | integer | Y tile coordinate |
| `worldX` | float | X world coordinate |
| `worldY` | float | Y world coordinate |
| `health` | float | Current health |
| `maxHealth` | float | Maximum health |
| `type` | string | Block type name |
| `team` | string | Team name |
| `rotation` | integer | Block rotation (0-3) |
| `power` | object\|null | Power status `{status, netPower}` |
| `items` | object | Items stored `{itemName: count}` |
| `liquids` | object | Liquids stored `{liquidName: amount}` |
| `enabled` | boolean | Whether the building is active |
| `configured` | any | Building-specific configuration |
| `efficiency` | float | Production efficiency (0.0 - 1.0) |
| `progress` | float | Build/craft progress (0.0 - 1.0) |

##### Examples

**All buildings:**
```bash
curl http://localhost:8089/api/state/buildings
```

**Core buildings only:**
```bash
curl "http://localhost:8089/api/state/buildings?type=core"
```

**Nearby buildings:**
```bash
curl "http://localhost:8089/api/state/buildings?x=256&y=312&radius=50"
```

**Team buildings only:**
```bash
curl "http://localhost:8089/api/state/buildings?team=sharded"
```

---

### Resources

#### GET /api/state/resources

Get current resource counts from the core storage.

**URL:** `/api/state/resources`  
**Method:** `GET`

##### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `resources` | object | Map of resource name to integer count |
| `capacity` | integer | Total storage capacity |
| `used` | integer | Total items stored |

##### Example

```bash
curl http://localhost:8089/api/state/resources
```

```json
{
  "resources": {
    "copper": 450,
    "lead": 230,
    "graphite": 89,
    "silicon": 156,
    "metaglass": 45,
    "titanium": 78,
    "thorium": 23,
    "surge-alloy": 12,
    "phase-fabric": 8,
    "plastanium": 34,
    "sand": 120,
    "coal": 200,
    "scrap": 67,
    "water": 500,
    "slag": 15,
    "oil": 30,
    "cryofluid": 10
  },
  "capacity": 9000,
  "used": 2029
}
```

---

### Map Info

#### GET /api/state/map

Get map metadata and game progress information.

**URL:** `/api/state/map`  
**Method:** `GET`

##### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Map name |
| `description` | string | Map description |
| `author` | string | Map author |
| `width` | integer | Map width in tiles |
| `height` | integer | Map height in tiles |
| `wave` | integer | Current wave number |
| `waveTime` | float | Seconds elapsed in current wave |
| `waveSpacing` | float | Seconds between waves |
| `wavesToSurvive` | integer | Total waves to win (0 = endless) |
| `gameTime` | float | Total game time in seconds |
| `paused` | boolean | Whether the game is paused |
| `rules` | object | Game rule overrides |
| `teams` | object[] | Active teams with statistics |

##### Example

```bash
curl http://localhost:8089/api/state/map
```

```json
{
  "name": "Frozen Farlands",
  "description": "A frozen wasteland rich in thorium.",
  "author": "Anuken",
  "width": 500,
  "height": 500,
  "wave": 12,
  "waveTime": 47.5,
  "waveSpacing": 120.0,
  "wavesToSurvive": 30,
  "gameTime": 1847.3,
  "paused": false,
  "rules": {
    "unitCap": 1000,
    "waveTimer": true,
    "enemyCoreBuildRadius": 400
  },
  "teams": [
    {
      "name": "sharded",
      "id": 1,
      "cores": 1,
      "items": 2029
    },
    {
      "name": "crux",
      "id": 2,
      "cores": 1,
      "items": 0
    }
  ]
}
```

---

## Action Endpoints

All action endpoints accept a JSON body and return a consistent response format.

**Action Response Format:**

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Whether the action was queued successfully |
| `actionId` | string | Unique ID for tracking the action |
| `message` | string | Human-readable result message |
| `queuedAt` | integer | Tick timestamp when queued |

### Move Player

#### POST /api/action/move

Move the player unit to the specified coordinates.

**URL:** `/api/action/move`  
**Method:** `POST`

##### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `x` | float | Yes | Target X coordinate (world units) |
| `y` | float | Yes | Target Y coordinate (world units) |

##### Example

```bash
curl -X POST http://localhost:8089/api/action/move \
  -H "Content-Type: application/json" \
  -d '{"x": 500, "y": 400}'
```

**Response:**
```json
{
  "success": true,
  "actionId": "move-1704391-1234",
  "message": "Player move queued to (500.0, 400.0)",
  "queuedAt": 110856
}
```

---

### Build

#### POST /api/action/build

Place a block at the specified tile position.

**URL:** `/api/action/build`  
**Method:** `POST`

##### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `x` | integer | Yes | Tile X coordinate |
| `y` | integer | Yes | Tile Y coordinate |
| `block` | string | Yes | Block name (e.g., `conveyor`, `router`, `duo`) |
| `rotation` | integer | No | Rotation (0=right, 1=up, 2=left, 3=down), default 0 |
| `config` | any | No | Block-specific configuration |

##### Common Block Names

| Block | Category | Description |
|-------|----------|-------------|
| `conveyor` | Distribution | Basic item transport |
| `titanium-conveyor` | Distribution | Faster conveyor |
| `plastanium-conveyor` | Distribution | Armored conveyor |
| `router` | Distribution | Distributes items to 3 outputs |
| `junction` | Distribution | Crosses conveyors without mixing |
| `bridge-conveyor` | Distribution | Transports over gaps |
| `phase-conveyor` | Distribution | Teleports items |
| `duo` | Turret | Basic dual turret |
| `scatter` | Turret | Flak anti-air turret |
| `hail` | Turret | Artillery turret |
| `arc` | Turret | Lightning turret |
| `lancer` | Turret | Laser turret |
| `salvo` | Turret | Burst fire turret |
| `fuse` | Turret | Shotgun turret |
| `ripple` | Turret | Mortar turret |
| `cyclone` | Turret | Dual flak turret |
| `foreshadow` | Turret | Railgun turret |
| `spectre` | Turret | Minigun turret |
| `meltdown` | Turret | Beam turret |
| `drill` | Production | Basic ground drill |
| `pneumatic-drill` | Production | Faster ground drill |
| `laser-drill` | Production | Advanced drill |
| `airblast-drill` | Production | Endgame drill |
| `mechanical-pump` | Liquid | Basic water pump |
| `rotary-pump` | Liquid | Faster pump |
| `thermal-pump` | Liquid | Endgame pump |
| `graphite-press` | Crafting | Makes graphite from coal |
| `silicon-smelter` | Crafting | Makes silicon from sand+coal |
| `kiln` | Crafting | Makes metaglass |
| `plastanium-compressor` | Crafting | Makes plastanium |
| `phase-weaver` | Crafting | Makes phase fabric |
| `alloy-smelter` | Crafting | Makes surge alloy |
| `sorter` | Distribution | Sorts items by type |
| `inverted-sorter` | Distribution | Inverted sorting |
| `overflow-gate` | Distribution | Overflow routing |
| `underflow-gate` | Distribution | Underflow routing |
| `container` | Storage | Small storage |
| `vault` | Storage | Large storage |
| `core-shard` | Core | Default core |
| `core-foundation` | Core | Upgraded core |
| `core-nucleus` | Core | Maximum core |
| `battery` | Power | Energy storage |
| `battery-large` | Power | Large energy storage |
| `combustion-generator` | Power | Basic generator |
| `thermal-generator` | Power | Heat-based generator |
| `steam-generator` | Power | Steam generator |
| `differential-generator` | Power | Advanced generator |
| `rtg-generator` | Power | Thorium generator |
| `solar-panel` | Power | Basic solar |
| `solar-panel-large` | Power | Large solar |
| `thorium-reactor` | Power | High-output reactor |
| `impact-reactor` | Power | Endgame reactor |
| `power-node` | Power | Small power node |
| `power-node-large` | Power | Large power node |
| `surge-tower` | Power | Long-range power node |
| `diode` | Power | Battery diode |

##### Example

```bash
curl -X POST http://localhost:8089/api/action/build \
  -H "Content-Type: application/json" \
  -d '{"x": 10, "y": 10, "block": "conveyor", "rotation": 0}'
```

**Response:**
```json
{
  "success": true,
  "actionId": "build-1704391-1235",
  "message": "Build conveyor at (10, 10) with rotation 0 queued",
  "queuedAt": 110860
}
```

##### Build with Configuration

```bash
curl -X POST http://localhost:8089/api/action/build \
  -H "Content-Type: application/json" \
  -d '{
    "x": 15,
    "y": 20,
    "block": "sorter",
    "rotation": 0,
    "config": "copper"
  }'
```

---

### Mine

#### POST /api/action/mine

Start mining at the specified ore tile.

**URL:** `/api/action/mine`  
**Method:** `POST`

##### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `x` | float | Yes | Target X coordinate (ore location) |
| `y` | float | Yes | Target Y coordinate (ore location) |

##### Example

```bash
curl -X POST http://localhost:8089/api/action/mine \
  -H "Content-Type: application/json" \
  -d '{"x": 100, "y": 200}'
```

**Response:**
```json
{
  "success": true,
  "actionId": "mine-1704391-1236",
  "message": "Mine action queued at (100.0, 200.0)",
  "queuedAt": 110865
}
```

---

### Shoot

#### POST /api/action/shoot

Fire at a target position or suppress an area.

**URL:** `/api/action/shoot`  
**Method:** `POST`

##### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `x` | float | Yes | Target X coordinate |
| `y` | float | Yes | Target Y coordinate |
| `shoot` | boolean | No | Whether to start (`true`) or stop (`false`) shooting, default `true` |

##### Example

**Fire at position:**
```bash
curl -X POST http://localhost:8089/api/action/shoot \
  -H "Content-Type: application/json" \
  -d '{"x": 300, "y": 400}'
```

**Stop shooting:**
```bash
curl -X POST http://localhost:8089/api/action/shoot \
  -H "Content-Type: application/json" \
  -d '{"x": 0, "y": 0, "shoot": false}'
```

**Response:**
```json
{
  "success": true,
  "actionId": "shoot-1704391-1237",
  "message": "Shoot at (300.0, 400.0) queued",
  "queuedAt": 110870
}
```

---

### Command Units

#### POST /api/action/command

Issue commands to selected units.

**URL:** `/api/action/command`  
**Method:** `POST`

##### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `command` | string | Yes | Command type: `move`, `attack`, `patrol`, `idle`, `mine` |
| `x` | float | No* | Target X coordinate (*required for move, attack, patrol) |
| `y` | float | No* | Target Y coordinate (*required for move, attack, patrol) |
| `targetId` | integer | No | Target unit/building ID for attack command |
| `unitFilter` | object | No | Filter for selecting which units to command |

**Unit Filter Object:**

| Field | Type | Description |
|-------|------|-------------|
| `types` | string[] | Array of unit type names to include |
| `maxUnits` | integer | Maximum units to command |
| `radius` | float | Command units within this radius of (x, y) |
| `teamOnly` | boolean | Only command own-team units |

##### Example -- Move Command

```bash
curl -X POST http://localhost:8089/api/action/command \
  -H "Content-Type: application/json" \
  -d '{
    "command": "move",
    "x": 500,
    "y": 400,
    "unitFilter": {
      "types": ["dagger", "mace"],
      "maxUnits": 10,
      "radius": 200
    }
  }'
```

##### Example -- Attack Command

```bash
curl -X POST http://localhost:8089/api/action/command \
  -H "Content-Type: application/json" \
  -d '{
    "command": "attack",
    "x": 600,
    "y": 350,
    "unitFilter": {
      "maxUnits": 20,
      "radius": 300
    }
  }'
```

##### Example -- Patrol Command

```bash
curl -X POST http://localhost:8089/api/action/command \
  -H "Content-Type: application/json" \
  -d '{
    "command": "patrol",
    "x": 300,
    "y": 300,
    "unitFilter": {
      "types": ["flare"],
      "maxUnits": 5
    }
  }'
```

**Response:**
```json
{
  "success": true,
  "actionId": "command-1704391-1238",
  "message": "Command 'move' queued for up to 10 units",
  "queuedAt": 110875
}
```

---

### Configure Building

#### POST /api/action/configure

Configure a building's settings (e.g., sorter item, factory recipe).

**URL:** `/api/action/configure`  
**Method:** `POST`

##### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `x` | integer | Yes | Building tile X coordinate |
| `y` | integer | Yes | Building tile Y coordinate |
| `config` | any | Yes | Configuration value (string for sorters, object for factories) |

##### Example -- Configure Sorter

```bash
curl -X POST http://localhost:8089/api/action/configure \
  -H "Content-Type: application/json" \
  -d '{
    "x": 15,
    "y": 20,
    "config": "copper"
  }'
```

**Response:**
```json
{
  "success": true,
  "actionId": "configure-1704391-1239",
  "message": "Configure building at (15, 20) queued",
  "queuedAt": 110880
}
```

---

### Rotate Building

#### POST /api/action/rotate

Rotate a building to a new orientation.

**URL:** `/api/action/rotate`  
**Method:** `POST`

##### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `x` | integer | Yes | Building tile X coordinate |
| `y` | integer | Yes | Building tile Y coordinate |
| `rotation` | integer | Yes | New rotation (0=right, 1=up, 2=left, 3=down) |

##### Rotation Reference

```
Rotation 0 (Right):  -->
Rotation 1 (Up):     /\ 
Rotation 2 (Left):   <--
Rotation 3 (Down):   \/
```

##### Example

```bash
curl -X POST http://localhost:8089/api/action/rotate \
  -H "Content-Type: application/json" \
  -d '{"x": 10, "y": 10, "rotation": 1}'
```

**Response:**
```json
{
  "success": true,
  "actionId": "rotate-1704391-1240",
  "message": "Rotate building at (10, 10) to 1 queued",
  "queuedAt": 110885
}
```

---

## Error Handling

All errors follow a consistent JSON format:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error description",
    "details": "Additional context if available"
  }
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `NOT_IN_GAME` | 400 | Player is not currently in a game |
| `INVALID_PARAMETERS` | 400 | Missing or invalid request parameters |
| `INVALID_COORDINATES` | 400 | Coordinates are out of map bounds |
| `INVALID_BLOCK` | 400 | Unknown or unavailable block name |
| `INVALID_ROTATION` | 400 | Rotation value must be 0-3 |
| `INSUFFICIENT_RESOURCES` | 400 | Not enough resources for the action |
| `INVALID_COMMAND` | 400 | Unknown command type |
| `NO_TARGET` | 404 | Target unit or building not found |
| `BUILDING_NOT_FOUND` | 404 | No building at specified coordinates |
| `CANNOT_BUILD` | 400 | Cannot build at the specified location |
| `QUEUE_FULL` | 503 | Action queue is full, try again later |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

### HTTP Status Codes

| Status | Meaning |
|--------|---------|
| 200 OK | Successful GET request |
| 201 Created | Action successfully queued |
| 400 Bad Request | Invalid request parameters |
| 404 Not Found | Resource not found |
| 405 Method Not Allowed | Wrong HTTP method for endpoint |
| 503 Service Unavailable | Game not loaded or queue full |
| 500 Internal Server Error | Server-side error |

---

## Data Types

### Coordinate System

Mindustry uses two coordinate systems:

- **Tile coordinates**: Integer values representing tile grid positions. Range is `[0, mapWidth)` and `[0, mapHeight)`.
- **World coordinates**: Float values where `worldX = tileX * tileSize + offset`. Default tile size is 8 world units.

The API accepts world coordinates for movement/actions and tile coordinates for building placement.

### Rotation Values

```
0 = East (right)   = 0 degrees
1 = North (up)     = 90 degrees
2 = West (left)    = 180 degrees
3 = South (down)   = 270 degrees
```

### Team Names

| Name | ID | Description |
|------|-----|-------------|
| `sharded` | 1 | Default player team |
| `crux` | 2 | Default enemy team |
| `malis` | 3 | Additional team |
| `green` | 4 | Additional team |
| `blue` | 5 | Additional team |

### Item Names

**Raw resources:** `copper`, `lead`, `graphite`, `silicon`, `metaglass`, `titanium`, `thorium`, `surge-alloy`, `phase-fabric`, `plastanium`

**Natural:** `sand`, `coal`, `scrap`, `beryllium`, `tungsten`, `oxide`, `carbide`

**Liquids:** `water`, `slag`, `oil`, `cryofluid`, `arkycite`, `gallium`, `neoplasm`

### Unit Names

**Ground (T1-T5):** `dagger`/`mace`/`fortress`/`scepter`/`reign`, `nova`/`pulsar`/`quasar`/`vela`/`corvus`, `crawler`/`atrax`/`spiroct`/`arkyid`/`toxopid`

**Flying (T1-T5):** `flare`/`horizon`/`zenith`/`antumbra`/`eclipse`, `mono`/`poly`/`mega`/`quad`/`oct`, `risso`/`minke`/`bryde`/`sei`/`omura`

**Core units:** `alpha`, `beta`, `gamma`

**Special:** `evoke`, `incite`, `emanate`, `manifold`, `assembly-drone`

---

*For practical code examples, see [USAGE_EXAMPLES.md](USAGE_EXAMPLES.md). For development and extension information, see [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md).*
