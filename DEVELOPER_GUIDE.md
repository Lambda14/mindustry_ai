# Developer Guide

This guide covers the architecture of the AI Agent Mod, how to extend it with new actions and observation scanners, configuration options, and thread safety considerations.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
  - [High-Level Design](#high-level-design)
  - [Component Breakdown](#component-breakdown)
  - [Data Flow](#data-flow)
- [Project Structure](#project-structure)
- [Adding New Actions](#adding-new-actions)
  - [Action Handler Pattern](#action-handler-pattern)
  - [Implementing a New Action](#implementing-a-new-action)
  - [Registering the Action](#registering-the-action)
- [Adding New Observation Scanners](#adding-new-observation-scanners)
  - [Scanner Pattern](#scanner-pattern)
  - [Implementing a New Scanner](#implementing-a-new-scanner)
- [API Server Configuration](#api-server-configuration)
- [Thread Safety Considerations](#thread-safety-considerations)
- [Building and Testing](#building-and-testing)
- [Contributing Guidelines](#contributing-guidelines)

---

## Architecture Overview

### High-Level Design

The AI Agent Mod is built on a layered architecture that separates the HTTP API from the game engine, ensuring thread safety and game stability:

```
+-------------------------------------------------------------+
|                        AI Agent                             |
|              (Python / JavaScript / Rust ...)               |
+-------------------------------------------------------------+
                            |
                            | HTTP/JSON
                            v
+-------------------------------------------------------------+
|                    API Server Layer                         |
|  +-------------------+  +-------------------+  +---------+ |
|  |  RootEndpoint     |  |  StateEndpoints   |  | Action  | |
|  |  GET /            |  |  GET /api/state/* |  | Endpts  | |
|  +-------------------+  +-------------------+  +---------+ |
+-------------------------------------------------------------+
                            |
                            | Internal Call
                            v
+-------------------------------------------------------------+
|                  Dispatcher Layer                           |
|  +-------------------+  +-------------------+              |
|  |  StateDispatcher  |  |  ActionDispatcher |              |
|  |  (Scanner Registry)|  |  (Handler Registry)|             |
|  +-------------------+  +-------------------+              |
+-------------------------------------------------------------+
         |                            |
         | Scan                       | Queue
         v                            v
+------------------+         +------------------------+
|  Scanner Layer   |         |   Action Queue         |
|  +-------------+ |         |   (Thread-Safe)        |
|  | PlayerScan  | |         |   +----------------+   |
|  | UnitScan    | |         |   | ActionQueue    |   |
|  | BuildingScan| |         |   | - enqueue()    |   |
|  | ResourceScan| |         |   | - dequeue()    |   |
|  | MapScan     | |         |   | - isFull()     |   |
|  +-------------+ |         |   +----------------+   |
+------------------+         +------------------------+
         |                            |
         | Read                       | Poll
         v                            v
+-------------------------------------------------------------+
|                    Mindustry Game Engine                    |
|  +-------------------+  +-------------------+              |
|  |  Vars.player      |  |  Vars.world       |              |
|  |  Vars.state       |  |  Logic & Render   |              |
|  +-------------------+  +-------------------+              |
+-------------------------------------------------------------+
```

### Component Breakdown

#### 1. API Server Layer (`server`)
An embedded HTTP server (using Mindustry's built-in `Net` HTTP capabilities) that listens on a configurable port. It routes incoming requests to the appropriate dispatcher based on URL path and HTTP method.

**Key classes:**
- `ApiServer` -- Main server class, handles startup/shutdown
- `RouteRegistry` -- Maps URL patterns to endpoint handlers
- `RequestParser` -- Parses HTTP request bodies into action/scan parameters
- `ResponseWriter` -- Formats scan results and action responses as JSON

#### 2. Dispatcher Layer (`dispatcher`)
The central hub that routes requests to the appropriate scanner or action handler. Decouples the HTTP layer from the game interaction layer.

**Key classes:**
- `StateDispatcher` -- Routes state observation requests to scanners
- `ActionDispatcher` -- Routes action requests to handlers and manages the action queue
- `DispatcherRegistry` -- Maintains the registry of all scanners and handlers

#### 3. Scanner Layer (`scan`)
Read-only modules that extract game state from the Mindustry engine. Each scanner is responsible for one domain of the game state.

**Key classes:**
- `PlayerScanner` -- Extracts player unit state
- `UnitScanner` -- Scans and filters units on the map
- `BuildingScanner` -- Scans and filters buildings/structures
- `ResourceScanner` -- Reads core inventory and capacity
- `MapScanner` -- Reads map metadata and game progress
- `ScanResult` -- Base class for all scan result objects

#### 4. Action Queue (`action`)
A thread-safe queue that buffers actions submitted via HTTP and feeds them to the main game thread for execution. This is the critical component that ensures thread safety.

**Key classes:**
- `ActionQueue` -- Thread-safe circular buffer for pending actions
- `Action` -- Base interface for all executable actions
- `ActionProcessor` -- Polls the queue and executes actions on the game thread
- `ActionResult` -- Tracks execution status and results

#### 5. Action Handlers (`action/handlers`)
Concrete implementations of game actions. Each handler encapsulates the logic for one type of action.

**Key classes:**
- `MoveHandler` -- Handles player movement
- `BuildHandler` -- Handles block placement
- `MineHandler` -- Handles ore mining
- `ShootHandler` -- Handles shooting
- `CommandHandler` -- Handles unit commands
- `ConfigureHandler` -- Handles building configuration
- `RotateHandler` -- Handles building rotation

### Data Flow

#### Observation Flow (Reading Game State)

```
1. AI Agent sends GET /api/state/units
2. API Server receives the HTTP request
3. RequestParser extracts query parameters (filters)
4. StateDispatcher looks up UnitScanner
5. UnitScanner reads from Vars.world.units (main thread only)
6. ScanResult is serialized to JSON by ResponseWriter
7. JSON response sent back to AI Agent
```

> **Important:** All game state reads happen on the main game thread during the render/update cycle. The scanner layer never accesses game state from HTTP worker threads.

#### Action Flow (Writing to Game State)

```
1. AI Agent sends POST /api/action/move
2. API Server receives the HTTP request
3. RequestParser extracts action parameters
4. ActionDispatcher creates a MoveAction
5. MoveAction is enqueued in the ActionQueue (thread-safe)
6. HTTP response returns immediately with actionId
7. ActionProcessor (on main thread) polls the queue each tick
8. MoveAction.execute() runs on the main thread
9. ActionResult is updated (can be queried via actionId)
```

> **Important:** Actions are never executed directly on HTTP threads. They are always queued and executed on the main game thread.

---

## Project Structure

```
ai-agent-mod/
|-- src/
|   |-- aiagent/
|   |   |-- mod/
|   |   |   |-- AIAgentMod.java              # Main mod entry point
|   |   |   |-- server/
|   |   |   |   |-- ApiServer.java           # HTTP server
|   |   |   |   |-- RouteRegistry.java       # URL routing
|   |   |   |   |-- endpoints/
|   |   |   |   |   |-- RootEndpoint.java
|   |   |   |   |   |-- StateEndpoints.java
|   |   |   |   |   |-- ActionEndpoints.java
|   |   |   |-- dispatcher/
|   |   |   |   |-- StateDispatcher.java
|   |   |   |   |-- ActionDispatcher.java
|   |   |   |   |-- DispatcherRegistry.java
|   |   |   |-- scan/
|   |   |   |   |-- Scanner.java             # Base scanner interface
|   |   |   |   |-- PlayerScanner.java
|   |   |   |   |-- UnitScanner.java
|   |   |   |   |-- BuildingScanner.java
|   |   |   |   |-- ResourceScanner.java
|   |   |   |   |-- MapScanner.java
|   |   |   |   |-- ScanResult.java          # Result base class
|   |   |   |-- action/
|   |   |   |   |-- Action.java              # Base action interface
|   |   |   |   |-- ActionQueue.java         # Thread-safe queue
|   |   |   |   |-- ActionProcessor.java     # Main thread executor
|   |   |   |   |-- ActionResult.java        # Execution tracking
|   |   |   |   |-- handlers/
|   |   |   |   |   |-- MoveHandler.java
|   |   |   |   |   |-- BuildHandler.java
|   |   |   |   |   |-- MineHandler.java
|   |   |   |   |   |-- ShootHandler.java
|   |   |   |   |   |-- CommandHandler.java
|   |   |   |   |   |-- ConfigureHandler.java
|   |   |   |   |   |-- RotateHandler.java
|   |   |   |-- config/
|   |   |   |   |-- ModConfig.java           # Configuration loader
|   |   |   |   |-- ConfigDefaults.java      # Default values
|   |   |   |-- util/
|   |   |       |-- JsonUtils.java           # JSON serialization
|   |   |       |-- ThreadSafe.java          # Thread safety utilities
|   |   |       |-- Validation.java          # Input validation
|-- mod.json                                  # Mindustry mod descriptor
|-- build.gradle                              # Build configuration
|-- README.md
|-- API_REFERENCE.md
|-- DEVELOPER_GUIDE.md
|-- CHANGELOG.md
|-- LICENSE
```

---

## Adding New Actions

### Action Handler Pattern

All actions follow the **Command Pattern** with a consistent interface:

```java
public interface Action {
    /** Unique type identifier for this action */
    String getType();

    /** Parameters required for execution */
    Map<String, Object> getParameters();

    /** Validate parameters before queuing */
    ValidationResult validate();

    /** Execute the action (called on main thread only) */
    ActionResult execute();
}
```

### Implementing a New Action

Let's walk through adding a `rebuild` action that automatically deconstructs and rebuilds a building (useful for fixing damaged structures).

#### Step 1: Create the Action Class

```java
package aiagent.mod.action;

import aiagent.mod.action.handlers.ActionHandler;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;

public class RebuildAction implements Action {
    private final int tileX;
    private final int tileY;
    private final String blockName;
    private final int rotation;

    public RebuildAction(int tileX, int tileY, String blockName, int rotation) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.blockName = blockName;
        this.rotation = rotation;
    }

    @Override
    public String getType() {
        return "rebuild";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("tileX", tileX);
        params.put("tileY", tileY);
        params.put("block", blockName);
        params.put("rotation", rotation);
        return params;
    }

    @Override
    public ValidationResult validate() {
        if (tileX < 0 || tileX >= Vars.world.width() ||
            tileY < 0 || tileY >= Vars.world.height()) {
            return ValidationResult.fail("INVALID_COORDINATES",
                "Coordinates out of bounds");
        }

        if (Vars.content.block(blockName) == null) {
            return ValidationResult.fail("INVALID_BLOCK",
                "Unknown block: " + blockName);
        }

        return ValidationResult.ok();
    }

    @Override
    public ActionResult execute() {
        Tile tile = Vars.world.tile(tileX, tileY);
        if (tile == null) {
            return ActionResult.fail("Tile not found");
        }

        // Step 1: Deconstruct existing building if present
        if (tile.build != null && !tile.build.dead) {
            // Queue deconstruction
            tile.build.kill();
        }

        // Step 2: Rebuild the block
        // This will be picked up by the build handler
        // The actual rebuild logic depends on your mod's build system

        return ActionResult.success("Rebuilt " + blockName + " at (" + tileX + ", " + tileY + ")");
    }
}
```

#### Step 2: Create the Handler

```java
package aiagent.mod.action.handlers;

import aiagent.mod.action.Action;
import aiagent.mod.action.ActionResult;
import aiagent.mod.util.JsonUtils;

public class RebuildHandler implements ActionHandler {
    @Override
    public String getActionType() {
        return "rebuild";
    }

    @Override
    public Action parseRequest(Request request) {
        int x = JsonUtils.getInt(request.body, "x");
        int y = JsonUtils.getInt(request.body, "y");
        String block = JsonUtils.getString(request.body, "block");
        int rotation = JsonUtils.getInt(request.body, "rotation", 0);

        return new RebuildAction(x, y, block, rotation);
    }

    @Override
    public ActionResult validate(Action action) {
        return action.validate();
    }
}
```

#### Step 3: Register the Handler

```java
// In DispatcherRegistry.java or your mod's init() method:
public void registerHandlers() {
    // ... existing handlers ...
    ActionDispatcher.registerHandler(new RebuildHandler());
}
```

#### Step 4: Add the Endpoint

The action endpoint is already generic (`POST /api/action/{type}`), so no new endpoint is needed. The action type `"rebuild"` is automatically routed to your handler.

---

## Adding New Observation Scanners

### Scanner Pattern

All scanners implement a common interface:

```java
public interface Scanner<T extends ScanResult> {
    /** Unique identifier for this scanner */
    String getName();

    /** Perform the scan (called on main thread only) */
    T scan(ScanParameters params);

    /** JSON schema for the scan result */
    JsonSchema getResultSchema();
}
```

### Implementing a New Scanner

Let's add a scanner that reports on power grid status across the map.

#### Step 1: Define the Result Class

```java
package aiagent.mod.scan;

import java.util.List;

public class PowerScanResult extends ScanResult {
    public int totalNodes;
    public int totalGenerators;
    public int totalConsumers;
    public float totalProduction;
    public float totalConsumption;
    public float netPower;
    public List<PowerGridInfo> grids;

    public static class PowerGridInfo {
        public int nodeCount;
        public float production;
        public float consumption;
        public float batteryStorage;
        public float batteryCapacity;
    }
}
```

#### Step 2: Implement the Scanner

```java
package aiagent.mod.scan;

import aiagent.mod.util.JsonUtils;
import mindustry.Vars;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.power.ImpactReactor;
import mindustry.world.blocks.power.ThermalGenerator;

public class PowerScanner implements Scanner<PowerScanResult> {

    @Override
    public String getName() {
        return "power";
    }

    @Override
    public PowerScanResult scan(ScanParameters params) {
        PowerScanResult result = new PowerScanResult();

        int totalNodes = 0;
        int totalGenerators = 0;
        int totalConsumers = 0;
        float totalProduction = 0f;
        float totalConsumption = 0f;

        // Iterate through all buildings to find power-related ones
        Vars.world.tiles.eachTile(tile -> {
            if (tile.build != null && tile.build.power != null) {
                totalNodes++;
                PowerGraph graph = tile.build.power.graph;
                if (graph != null) {
                    // These are cumulative -- only count each graph once
                }
            }
            // Check if generator
            if (tile.build != null && tile.block() instanceof ThermalGenerator) {
                totalGenerators++;
            }
        });

        result.totalNodes = totalNodes;
        result.totalGenerators = totalGenerators;
        result.totalConsumers = totalConsumers;
        result.totalProduction = totalProduction;
        result.totalConsumption = totalConsumption;
        result.netPower = totalProduction - totalConsumption;

        return result;
    }

    @Override
    public JsonSchema getResultSchema() {
        return JsonSchema.builder()
            .addField("totalNodes", JsonType.INTEGER)
            .addField("totalGenerators", JsonType.INTEGER)
            .addField("totalConsumers", JsonType.INTEGER)
            .addField("totalProduction", JsonType.FLOAT)
            .addField("totalConsumption", JsonType.FLOAT)
            .addField("netPower", JsonType.FLOAT)
            .build();
    }
}
```

#### Step 3: Register the Scanner

```java
// In StateDispatcher.java:
public void registerScanners() {
    // ... existing scanners ...
    registerScanner(new PowerScanner());
}
```

#### Step 4: Add the Endpoint

The state endpoint is already generic (`GET /api/state/{scanner}`), so your new scanner is automatically available at:

```
GET /api/state/power
```

---

## API Server Configuration

### Configuration File

The mod reads configuration from `config/ai-agent-mod.properties` in the Mindustry user data directory:

```properties
# Server settings
api.port=8089
api.host=0.0.0.0

# Scan limits (prevent overwhelming responses)
scan.maxUnits=500
scan.maxBuildings=1000

# Action queue settings
action.queueSize=256
action.maxBatchSize=32

# Logging
logging.level=INFO
logging.scanResults=false
logging.actionResults=true

# Advanced
server.threadPoolSize=4
server.requestTimeoutMs=5000
scan.cacheEnabled=false
scan.cacheTtlMs=100
```

### Configuration Class

```java
package aiagent.mod.config;

import arc.util.Log;
import java.io.*;
import java.util.Properties;

public class ModConfig {
    private static final String CONFIG_FILE = "config/ai-agent-mod.properties";
    private static Properties properties = new Properties();

    // Default values
    public static int API_PORT = 8089;
    public static String API_HOST = "0.0.0.0";
    public static int SCAN_MAX_UNITS = 500;
    public static int SCAN_MAX_BUILDINGS = 1000;
    public static int ACTION_QUEUE_SIZE = 256;
    public static int ACTION_MAX_BATCH = 32;

    public static void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
                apply();
                Log.info("[AIAgentMod] Configuration loaded from " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.err("[AIAgentMod] Failed to load config, using defaults", e);
            }
        } else {
            Log.info("[AIAgentMod] No config file found, using defaults");
            saveDefaults();
        }
    }

    private static void apply() {
        API_PORT = getInt("api.port", API_PORT);
        API_HOST = getString("api.host", API_HOST);
        SCAN_MAX_UNITS = getInt("scan.maxUnits", SCAN_MAX_UNITS);
        SCAN_MAX_BUILDINGS = getInt("scan.maxBuildings", SCAN_MAX_BUILDINGS);
        ACTION_QUEUE_SIZE = getInt("action.queueSize", ACTION_QUEUE_SIZE);
        ACTION_MAX_BATCH = getInt("action.maxBatchSize", ACTION_MAX_BATCH);
    }

    private static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private static void saveDefaults() {
        // Create default config file
    }
}
```

### Hot Reloading

Configuration can be reloaded without restarting Mindustry by sending a special request:

```bash
curl -X POST http://localhost:8089/api/admin/reload-config
```

> **Note:** This is an admin endpoint and may require authentication in future versions.

---

## Thread Safety Considerations

### The Golden Rule

**Never access Mindustry's game state from an HTTP worker thread.** All game objects (`Vars.player`, `Vars.world`, `Vars.state`) must only be accessed from the main game thread.

### Thread Model

```
Thread 1: Main Game Thread (Mindustry core)
  - Runs game logic (60 ticks/second)
  - Runs render loop
  - Executes queued actions
  - Calls ActionProcessor.poll() each tick

Thread 2-N: HTTP Worker Threads (Net HTTP server)
  - Handle incoming HTTP requests
  - Parse request bodies
  - Create Action objects
  - Enqueue actions in ActionQueue
  - Return HTTP responses immediately
  - NEVER touch game state directly
```

### The Action Queue

The `ActionQueue` is the critical thread-safe component:

```java
package aiagent.mod.action;

import java.util.concurrent.*;

public class ActionQueue {
    private final BlockingQueue<Action> queue;
    private final int capacity;

    public ActionQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /** Called from HTTP worker threads -- thread safe */
    public boolean enqueue(Action action) throws QueueFullException {
        boolean success = queue.offer(action);
        if (!success) {
            throw new QueueFullException("Action queue is full (" + capacity + ")");
        }
        return true;
    }

    /** Called from main game thread only */
    public Action dequeue() {
        return queue.poll();
    }

    public boolean isFull() {
        return queue.remainingCapacity() == 0;
    }

    public int size() {
        return queue.size();
    }
}
```

### Action Execution on Main Thread

The `ActionProcessor` is called from the main game thread each tick:

```java
package aiagent.mod.action;

import arc.Events;
import mindustry.game.EventType;

public class ActionProcessor {
    private final ActionQueue queue;
    private final int maxPerTick;

    public ActionProcessor(ActionQueue queue, int maxPerTick) {
        this.queue = queue;
        this.maxPerTick = maxPerTick;

        // Register to receive tick events on the main thread
        Events.run(EventType.Trigger.update, this::process);
    }

    private void process() {
        int processed = 0;
        Action action;

        // Process up to maxPerTick actions per frame
        while (processed < maxPerTick && (action = queue.dequeue()) != null) {
            try {
                ActionResult result = action.execute();
                ActionTracker.recordResult(action.getActionId(), result);
            } catch (Exception e) {
                Log.err("[AIAgentMod] Action execution failed: " + action.getType(), e);
                ActionTracker.recordResult(action.getActionId(),
                    ActionResult.fail("Execution error: " + e.getMessage()));
            }
            processed++;
        }
    }
}
```

### Immutable Scan Results

All scan results are immutable data transfer objects. The scanner reads game state and copies it into a result object on the main thread, then the HTTP thread serializes this immutable object to JSON:

```java
// Scanner runs on main thread
public PlayerScanResult scan(ScanParameters params) {
    PlayerScanResult result = new PlayerScanResult();
    // ... read from Vars.player on main thread ...
    result.x = player.x;  // Safe: main thread
    result.y = player.y;
    return result;  // Return immutable copy
}
```

### Common Thread Safety Pitfalls

#### Pitfall 1: Direct Game Object Access
```java
// WRONG: HTTP thread accessing game state directly
public Response handleMove(Request request) {
    float x = request.body.x;
    float y = request.body.y;
    // DANGER: This runs on HTTP thread!
    Vars.player.x = x;  // CRASH / undefined behavior
    Vars.player.y = y;
    return Response.ok();
}
```

#### Pitfall 2: Mutable Scan Results
```java
// WRONG: Returning references to mutable game objects
public List<Unit> scanUnits() {
    // DANGER: Returns live game objects that can change!
    return Vars.units.toArray();  // NOT thread-safe
}
```

#### Pitfall 3: Synchronization on Wrong Object
```java
// WRONG: Synchronizing on a game object
synchronized (Vars.player) {
    // Mindustry doesn't use synchronized on game objects
    // This can cause deadlocks with the game's own threading
}
```

### Best Practices

1. **Always queue actions** -- Never execute actions directly from HTTP threads
2. **Copy data eagerly** -- When scanning, copy all values into result objects immediately
3. **Use immutable results** -- Scan result objects should have no setters after construction
4. **Prefer BlockingQueue** -- Use `ArrayBlockingQueue` or `LinkedBlockingQueue` for the action queue
5. **Keep queues bounded** -- Always set a maximum capacity to prevent memory issues
6. **Handle exceptions** -- Wrap action execution in try-catch to prevent crashing the game
7. **Validate early** -- Validate request parameters on the HTTP thread before queuing
8. **Minimize main-thread work** -- Action execution should be quick; avoid heavy computation

---

## Building and Testing

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Gradle 8.0 or higher (wrapper included)
- Mindustry source or compiled JAR for dependencies

### Development Setup

```bash
# Clone the repository
git clone https://github.com/yourusername/ai-agent-mod.git
cd ai-agent-mod

# Build the mod
./gradlew build

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport
```

### Running in Development Mode

```bash
# Build and copy to Mindustry mods folder
./gradlew build && \
  cp build/libs/ai-agent-mod.jar ~/.local/share/Mindustry/mods/

# Launch Mindustry with the mod
mindustry
```

### Testing with cURL

```bash
# Start a local test server
./gradlew testServer

# In another terminal, test endpoints
curl http://localhost:8089/
curl http://localhost:8089/api/state
curl -X POST http://localhost:8089/api/action/move -H "Content-Type: application/json" -d '{"x":100,"y":100}'
```

---

## Contributing Guidelines

### Code Style
- Follow the existing code formatting (4 spaces indentation)
- Use meaningful variable names
- Add JavaDoc comments to all public methods and classes
- Keep methods focused and under 50 lines where possible

### Pull Request Process
1. Fork the repository and create a feature branch
2. Make your changes with clear, atomic commits
3. Add tests for new functionality
4. Update documentation (README, API_REFERENCE, DEVELOPER_GUIDE)
5. Ensure all tests pass: `./gradlew test`
6. Submit a pull request with a clear description

### Reporting Issues
- Use GitHub Issues with the appropriate template
- Include Mindustry version, mod version, and Java version
- Provide steps to reproduce
- Include relevant log output

---

*For API usage examples, see [USAGE_EXAMPLES.md](USAGE_EXAMPLES.md). For endpoint documentation, see [API_REFERENCE.md](API_REFERENCE.md).*
