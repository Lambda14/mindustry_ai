# Usage Examples for AI Agents

This document provides practical, runnable Python examples for common AI agent tasks using the Mindustry AI Agent Mod REST API.

**Requirements:** `pip install requests`

**Base URL:** `http://localhost:8089`

---

## Table of Contents

- [Helper Class](#helper-class)
- [Example 1: Scout the Map](#example-1-scout-the-map)
- [Example 2: Build a Conveyor Line](#example-2-build-a-conveyor-line)
- [Example 3: Defend the Base](#example-3-defend-the-base)
- [Example 4: Mining Automation](#example-4-mining-automation)
- [Example 5: Full AI Loop](#example-5-full-ai-loop)
- [Advanced Patterns](#advanced-patterns)

---

## Helper Class

All examples below use this shared helper class for API communication:

```python
import requests
import json
import time
from typing import Optional, Dict, Any, List

class MindustryAPI:
    """Client for the Mindustry AI Agent Mod REST API."""

    def __init__(self, base_url: str = "http://localhost:8089"):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    # -- Health Check --

    def health(self) -> Dict[str, Any]:
        """Check if the mod is loaded and the API is accessible."""
        response = self.session.get(f"{self.base_url}/")
        response.raise_for_status()
        return response.json()

    # -- State Observation --

    def get_state(self) -> Dict[str, Any]:
        """Get the full game state snapshot."""
        response = self.session.get(f"{self.base_url}/api/state")
        response.raise_for_status()
        return response.json()

    def get_player(self) -> Dict[str, Any]:
        """Get player state."""
        response = self.session.get(f"{self.base_url}/api/state/player")
        response.raise_for_status()
        return response.json()

    def get_units(self, **filters) -> Dict[str, Any]:
        """
        Scan units with optional filters.
        Filters: team, type, x, y, radius, limit
        """
        response = self.session.get(
            f"{self.base_url}/api/state/units",
            params=filters
        )
        response.raise_for_status()
        return response.json()

    def get_buildings(self, **filters) -> Dict[str, Any]:
        """
        Scan buildings with optional filters.
        Filters: team, type, x, y, radius, limit
        """
        response = self.session.get(
            f"{self.base_url}/api/state/buildings",
            params=filters
        )
        response.raise_for_status()
        return response.json()

    def get_resources(self) -> Dict[str, Any]:
        """Get current resource counts from core."""
        response = self.session.get(f"{self.base_url}/api/state/resources")
        response.raise_for_status()
        return response.json()

    def get_map_info(self) -> Dict[str, Any]:
        """Get map metadata and game progress."""
        response = self.session.get(f"{self.base_url}/api/state/map")
        response.raise_for_status()
        return response.json()

    # -- Player Actions --

    def move(self, x: float, y: float) -> Dict[str, Any]:
        """Move the player to the specified world coordinates."""
        response = self.session.post(
            f"{self.base_url}/api/action/move",
            json={"x": x, "y": y}
        )
        response.raise_for_status()
        return response.json()

    def build(self, x: int, y: int, block: str,
              rotation: int = 0, config: Any = None) -> Dict[str, Any]:
        """
        Place a block at tile coordinates.

        Args:
            x: Tile X coordinate
            y: Tile Y coordinate
            block: Block name (e.g., 'conveyor', 'duo')
            rotation: Rotation 0=right, 1=up, 2=left, 3=down
            config: Optional block configuration
        """
        payload = {
            "x": x,
            "y": y,
            "block": block,
            "rotation": rotation
        }
        if config is not None:
            payload["config"] = config

        response = self.session.post(
            f"{self.base_url}/api/action/build",
            json=payload
        )
        response.raise_for_status()
        return response.json()

    def mine(self, x: float, y: float) -> Dict[str, Any]:
        """Start mining at the specified ore location."""
        response = self.session.post(
            f"{self.base_url}/api/action/mine",
            json={"x": x, "y": y}
        )
        response.raise_for_status()
        return response.json()

    def shoot(self, x: float, y: float, shoot: bool = True) -> Dict[str, Any]:
        """Fire at a target position or stop shooting."""
        response = self.session.post(
            f"{self.base_url}/api/action/shoot",
            json={"x": x, "y": y, "shoot": shoot}
        )
        response.raise_for_status()
        return response.json()

    # -- Unit Commands --

    def command_units(self, command: str, x: Optional[float] = None,
                      y: Optional[float] = None,
                      target_id: Optional[int] = None,
                      unit_filter: Optional[Dict] = None) -> Dict[str, Any]:
        """
        Issue commands to units.

        Args:
            command: 'move', 'attack', 'patrol', 'idle', 'mine'
            x: Target X coordinate (for move/attack/patrol)
            y: Target Y coordinate (for move/attack/patrol)
            target_id: Target unit/building ID (for attack)
            unit_filter: Filter dict with keys:
                - types: List of unit type names
                - maxUnits: Maximum units to command
                - radius: Radius to search for units
                - teamOnly: Only command own-team units
        """
        payload = {"command": command}
        if x is not None:
            payload["x"] = x
        if y is not None:
            payload["y"] = y
        if target_id is not None:
            payload["targetId"] = target_id
        if unit_filter is not None:
            payload["unitFilter"] = unit_filter

        response = self.session.post(
            f"{self.base_url}/api/action/command",
            json=payload
        )
        response.raise_for_status()
        return response.json()

    # -- Building Management --

    def configure(self, x: int, y: int, config: Any) -> Dict[str, Any]:
        """Configure a building at the specified tile."""
        response = self.session.post(
            f"{self.base_url}/api/action/configure",
            json={"x": x, "y": y, "config": config}
        )
        response.raise_for_status()
        return response.json()

    def rotate(self, x: int, y: int, rotation: int) -> Dict[str, Any]:
        """Rotate a building at the specified tile."""
        response = self.session.post(
            f"{self.base_url}/api/action/rotate",
            json={"x": x, "y": y, "rotation": rotation}
        )
        response.raise_for_status()
        return response.json()
```

---

## Example 1: Scout the Map

This example demonstrates how to observe the game state, scan nearby units and buildings, and build a spatial awareness model for the AI agent.

```python
#!/usr/bin/env python3
"""
Example 1: Scout the Map

This script demonstrates how to:
- Check if the mod is loaded
- Get the full game state
- Scan for nearby units (friendly and enemy)
- Scan for nearby buildings
- Build a spatial awareness report
"""

from mindustry_api import MindustryAPI


def scout_map():
    api = MindustryAPI()

    # Step 1: Verify the mod is loaded
    health = api.health()
    print(f"[Scout] Mod status: {health['message']}")
    print(f"[Scout] Mod version: {health['version']}")

    # Step 2: Get the full game state
    state = api.get_state()
    player = state["player"]
    resources = state["resources"]
    map_info = state["map"]

    print(f"\n[Scout] === Player State ===")
    print(f"[Scout] Position: ({player['x']:.1f}, {player['y']:.1f})")
    print(f"[Scout] Health: {player['health']}/{player['maxHealth']}")
    print(f"[Scout] Unit: {player['unit']}")
    print(f"[Scout] Holding: {player.get('item', 'nothing')}")

    print(f"\n[Scout] === Resources ===")
    for resource, count in resources.items():
        if count > 0:
            print(f"[Scout]   {resource}: {count}")

    print(f"\n[Scout] === Map Info ===")
    print(f"[Scout] Map: {map_info['name']}")
    print(f"[Scout] Size: {map_info['width']}x{map_info['height']}")
    print(f"[Scout] Wave: {map_info['wave']}")

    # Step 3: Scan for all units
    px, py = player["x"], player["y"]

    all_units = api.get_units(limit=500)
    print(f"\n[Scout] === Unit Scan ===")
    print(f"[Scout] Total units on map: {all_units['count']}")

    # Step 4: Find nearby enemy units (potential threats)
    nearby_enemies = api.get_units(
        team="crux",         # Enemy team
        x=px, y=py,
        radius=200,          # Within 200 world units
        limit=50
    )

    print(f"\n[Scout] Enemy units within 200 units: {nearby_enemies['count']}")
    threat_level = 0
    for unit in nearby_enemies.get("units", []):
        dist = ((unit["x"] - px) ** 2 + (unit["y"] - py) ** 2) ** 0.5
        threat = unit.get("health", 1) / max(unit.get("maxHealth", 1), 1)
        threat_level += threat
        print(f"[Scout]   {unit['type']} at ({unit['x']:.0f}, {unit['y']:.0f}) "
              f"[dist={dist:.0f}, health={unit['health']:.0f}]")

    print(f"\n[Scout] Threat level: {threat_level:.1f}")

    # Step 5: Find nearby buildings
    nearby_buildings = api.get_buildings(
        team="sharded",      # Our buildings
        x=px, y=py,
        radius=100,
        limit=50
    )

    print(f"\n[Scout] === Building Scan (within 100 units) ===")
    print(f"[Scout] Nearby buildings: {nearby_buildings['count']}")

    building_types = {}
    for building in nearby_buildings.get("buildings", []):
        btype = building["type"]
        building_types[btype] = building_types.get(btype, 0) + 1

    for btype, count in sorted(building_types.items()):
        print(f"[Scout]   {btype}: {count}")

    # Step 6: Look for defensive structures (turrets)
    turrets = api.get_buildings(
        type="duo",           # Duo turret type
        x=px, y=py,
        radius=150,
        limit=20
    )
    print(f"\n[Scout] Duo turrets nearby: {turrets['count']}")

    # Return a summary for further decision-making
    return {
        "player_pos": (px, py),
        "threat_level": threat_level,
        "enemy_count": nearby_enemies["count"],
        "building_count": nearby_buildings["count"],
        "resources": resources,
        "wave": map_info["wave"]
    }


if __name__ == "__main__":
    report = scout_map()
    print("\n[Scout] === Scout Report Summary ===")
    for key, value in report.items():
        print(f"[Scout] {key}: {value}")
```

---

## Example 2: Build a Conveyor Line

This example demonstrates building a line of connected conveyors, including rotation calculations to ensure proper item flow.

```python
#!/usr/bin/env python3
"""
Example 2: Build a Conveyor Line

This script demonstrates how to:
- Plan a conveyor route between two points
- Calculate correct rotations for each conveyor
- Queue multiple build actions
- Verify the build was placed correctly
"""

import time
from mindustry_api import MindustryAPI


def calculate_rotation(from_x: int, from_y: int, to_x: int, to_y: int) -> int:
    """
    Calculate the Mindustry rotation value for a conveyor segment.

    Rotations:
        0 = East (right, +X)
        1 = North (up, +Y)
        2 = West (left, -X)
        3 = South (down, -Y)
    """
    dx = to_x - from_x
    dy = to_y - from_y

    if abs(dx) >= abs(dy):
        return 0 if dx > 0 else 2
    else:
        return 1 if dy > 0 else 3


def build_conveyor_line():
    api = MindustryAPI()

    # Step 1: Get player position (our starting point)
    player = api.get_player()
    start_x, start_y = int(player["x"] / 8), int(player["y"] / 8)

    # Step 2: Define the conveyor route
    # We'll build a simple L-shaped line: right 5 tiles, then up 3 tiles
    route = []

    # Go right 5 tiles
    for i in range(5):
        route.append((start_x + i, start_y))

    # Go up 3 tiles
    for i in range(1, 4):
        route.append((start_x + 4, start_y + i))

    print(f"[Build] Planning conveyor line from ({start_x}, {start_y})")
    print(f"[Build] Route has {len(route)} segments")

    # Step 3: Calculate rotations for each segment
    placements = []
    for i in range(len(route) - 1):
        x, y = route[i]
        next_x, next_y = route[i + 1]
        rotation = calculate_rotation(x, y, next_x, next_y)
        placements.append({
            "x": x,
            "y": y,
            "rotation": rotation
        })

    # Last segment points same as previous
    if len(route) > 1:
        last_rotation = placements[-1]["rotation"] if placements else 0
        placements.append({
            "x": route[-1][0],
            "y": route[-1][1],
            "rotation": last_rotation
        })

    # Step 4: Print the build plan
    print("\n[Build] === Build Plan ===")
    for i, p in enumerate(placements):
        rot_names = {0: "right", 1: "up", 2: "left", 3: "down"}
        print(f"[Build] Segment {i+1}: ({p['x']}, {p['y']}) "
              f"facing {rot_names.get(p['rotation'], '?')}")

    # Step 5: Execute the builds one at a time
    print("\n[Build] === Executing Builds ===")
    for i, placement in enumerate(placements):
        result = api.build(
            x=placement["x"],
            y=placement["y"],
            block="conveyor",
            rotation=placement["rotation"]
        )
        print(f"[Build] Segment {i+1}: {result['message']}")
        time.sleep(0.1)  # Small delay between builds

    # Step 6: Verify by scanning for our conveyors
    time.sleep(0.5)  # Wait for builds to complete
    buildings = api.get_buildings(
        type="conveyor",
        x=start_x * 8, y=start_y * 8,
        radius=100,
        limit=50
    )

    conveyor_count = buildings["count"]
    print(f"\n[Build] Verification: Found {conveyor_count} conveyors in area")
    print(f"[Build] Expected: {len(placements)} conveyors")

    if conveyor_count >= len(placements):
        print("[Build] SUCCESS: All conveyors placed!")
    else:
        print("[Build] WARNING: Some conveyors may be missing")

    return conveyor_count >= len(placements)


def build_mining_drill_line():
    """
    Extended example: Build a line from a drill to a core,
    including the drill and a container at the end.
    """
    api = MindustryAPI()

    # Get player position
    player = api.get_player()
    px, py = int(player["x"] / 8), int(player["y"] / 8)

    # Place a pneumatic drill
    drill_x, drill_y = px + 3, py
    print(f"[Build] Placing drill at ({drill_x}, {drill_y})")
    result = api.build(drill_x, drill_y, "pneumatic-drill")
    print(f"[Build] Drill: {result['message']}")

    # Build conveyor line from drill toward player
    # Simple straight line
    for i in range(1, 4):
        cx, cy = drill_x - i, drill_y
        result = api.build(cx, cy, "conveyor", rotation=2)  # Left
        print(f"[Build] Conveyor at ({cx}, {cy}): {result['message']}")
        time.sleep(0.1)

    print("[Build] Mining drill line complete!")


if __name__ == "__main__":
    build_conveyor_line()
    # Uncomment to run the extended example:
    # build_mining_drill_line()
```

---

## Example 3: Defend the Base

This example demonstrates defensive AI behaviors: detecting enemies, commanding units to attack, and manually shooting at threats.

```python
#!/usr/bin/env python3
"""
Example 3: Defend the Base

This script demonstrates how to:
- Continuously scan for enemy units
- Command friendly units to attack enemies
- Shoot at approaching threats manually
- Retreat if the threat is too great
"""

import time
from mindustry_api import MindustryAPI


def defend_base():
    api = MindustryAPI()

    # Configuration
    SCAN_RADIUS = 300          # Detection range
    ATTACK_RADIUS = 250        # Command attack within this range
    PANIC_THRESHOLD = 10       # Enemy count that triggers panic
    SHOOT_COOLDOWN = 0.2       # Minimum seconds between shots

    print("[Defend] Base defense system activated")

    last_shot_time = 0
    tick = 0

    try:
        while True:
            tick += 1
            print(f"\n[Defend] === Tick {tick} ===")

            # Step 1: Get current state
            player = api.get_player()
            px, py = player["x"], player["y"]

            # Step 2: Scan for enemies
            enemies = api.get_units(
                team="crux",
                x=px, y=py,
                radius=SCAN_RADIUS,
                limit=100
            )

            enemy_list = enemies.get("units", [])
            enemy_count = len(enemy_list)

            print(f"[Defend] Enemies detected: {enemy_count}")

            if enemy_count == 0:
                print("[Defend] No threats detected. Sector secure.")
                time.sleep(1)
                continue

            # Step 3: Identify the closest and most dangerous enemies
            threats = []
            for enemy in enemy_list:
                dist = ((enemy["x"] - px) ** 2 + (enemy["y"] - py) ** 2) ** 0.5
                health_ratio = enemy.get("health", 1) / max(enemy.get("maxHealth", 1), 1)
                threat_score = (1 / (dist + 1)) * health_ratio * 100
                threats.append({
                    **enemy,
                    "distance": dist,
                    "threat_score": threat_score
                })

            # Sort by threat score (highest first)
            threats.sort(key=lambda t: t["threat_score"], reverse=True)

            primary_target = threats[0]
            print(f"[Defend] Primary target: {primary_target['type']} "
                  f"at ({primary_target['x']:.0f}, {primary_target['y']:.0f}) "
                  f"[dist={primary_target['distance']:.0f}]")

            # Step 4: Command nearby units to attack
            if tick % 10 == 0:  # Issue commands every 10 ticks
                result = api.command_units(
                    command="attack",
                    x=primary_target["x"],
                    y=primary_target["y"],
                    unit_filter={
                        "maxUnits": 20,
                        "radius": ATTACK_RADIUS
                    }
                )
                print(f"[Defend] Unit command: {result['message']}")

            # Step 5: Manual shooting at the primary target
            current_time = time.time()
            if current_time - last_shot_time > SHOOT_COOLDOWN:
                result = api.shoot(
                    x=primary_target["x"],
                    y=primary_target["y"],
                    shoot=True
                )
                last_shot_time = current_time
                print(f"[Defend] Shooting at primary target")

            # Step 6: Panic check -- retreat if overwhelmed
            if enemy_count > PANIC_THRESHOLD:
                print(f"[Defend] WARNING: High threat level! ({enemy_count} enemies)")

                # Command all units to defensive positions near player
                result = api.command_units(
                    command="move",
                    x=px + 50,  # Slightly ahead of player
                    y=py,
                    unit_filter={
                        "maxUnits": 50,
                        "radius": 400
                    }
                )
                print(f"[Defend] Defensive reposition: {result['message']}")

            # Step 7: Report top threats
            print(f"\n[Defend] Top 3 threats:")
            for i, threat in enumerate(threats[:3]):
                print(f"[Defend]   {i+1}. {threat['type']} "
                      f"dist={threat['distance']:.0f} "
                      f"health={threat['health']:.0f}/{threat['maxHealth']:.0f}")

            time.sleep(0.5)

    except KeyboardInterrupt:
        print("\n[Defend] Defense system deactivated.")
        # Stop shooting
        api.shoot(0, 0, shoot=False)


def setup_defensive_perimeter():
    """Build turrets around the base for automated defense."""
    api = MindustryAPI()

    # Get player position (assume this is near the core)
    player = api.get_player()
    cx, cy = int(player["x"] / 8), int(player["y"] / 8)

    print(f"[Defend] Setting up defensive perimeter around ({cx}, {cy})")

    # Place turrets in a diamond pattern around the core
    turret_positions = [
        (cx + 5, cy),      # East
        (cx - 5, cy),      # West
        (cx, cy + 5),      # North
        (cx, cy - 5),      # South
        (cx + 4, cy + 4),  # NE
        (cx - 4, cy - 4),  # SW
        (cx + 4, cy - 4),  # SE
        (cx - 4, cy + 4),  # NW
    ]

    # Power nodes to connect turrets
    node_positions = [
        (cx + 2, cy + 2),
        (cx - 2, cy - 2),
    ]

    # Build power nodes first
    for x, y in node_positions:
        result = api.build(x, y, "power-node")
        print(f"[Defend] Power node at ({x}, {y}): {result['message']}")
        time.sleep(0.1)

    # Build turrets
    for i, (x, y) in enumerate(turret_positions):
        result = api.build(x, y, "duo")  # Basic turret
        print(f"[Defend] Turret {i+1} at ({x}, {y}): {result['message']}")
        time.sleep(0.1)

    print("[Defend] Defensive perimeter complete!")


if __name__ == "__main__":
    # Run the active defense system
    defend_base()

    # Uncomment to set up static defenses:
    # setup_defensive_perimeter()
```

---

## Example 4: Mining Automation

This example demonstrates automated ore detection, mining, and resource delivery using the player unit.

```python
#!/usr/bin/env python3
"""
Example 4: Mining Automation

This script demonstrates how to:
- Find nearby ore tiles
- Navigate to and mine ore
- Monitor inventory
- Return to base when full or when enough ore is collected
"""

import time
import math
from mindustry_api import MindustryAPI


def find_nearby_ore():
    """
    In a real implementation, this would scan the map for ore tiles.
    For this example, we return common ore locations relative to the player.

    In practice, you would:
    1. Scan the map terrain for ore deposits
    2. Use the building scanner to find drills (which indicate ore)
    3. Or maintain a known map of ore locations
    """
    api = MindustryAPI()
    player = api.get_player()
    px, py = player["x"], player["y"]

    # Known ore locations (these would come from terrain scanning)
    # For this example, we use positions relative to player
    known_ores = [
        {"x": px + 50, "y": py + 30, "type": "copper"},
        {"x": px - 40, "y": py + 60, "type": "lead"},
        {"x": px + 80, "y": py - 20, "type": "coal"},
        {"x": px + 120, "y": py + 40, "type": "titanium"},
    ]

    return known_ores


def mining_automation():
    api = MindustryAPI()

    # Configuration
    TARGET_ORE = "copper"
    DESIRED_AMOUNT = 200
    FULL_CAPACITY = 70    # Alpha unit item capacity

    print(f"[Mine] Starting mining automation for {TARGET_ORE}")
    print(f"[Mine] Target amount: {DESIRED_AMOUNT}")

    # Step 1: Find ore locations
    ores = find_nearby_ore()
    target_ores = [o for o in ores if o["type"] == TARGET_ORE]

    if not target_ores:
        print(f"[Mine] No {TARGET_ORE} deposits found!")
        return

    print(f"[Mine] Found {len(target_ores)} {TARGET_ORE} deposits")

    # Step 2: Get initial resource count
    resources = api.get_resources()
    initial_amount = resources.get("resources", {}).get(TARGET_ORE, 0)
    current_amount = initial_amount

    print(f"[Mine] Current {TARGET_ORE} in storage: {initial_amount}")

    ore_index = 0
    cycles = 0

    while current_amount - initial_amount < DESIRED_AMOUNT:
        cycles += 1
        print(f"\n[Mine] === Mining cycle {cycles} ===")

        # Step 3: Move to ore location
        ore = target_ores[ore_index % len(target_ores)]
        print(f"[Mine] Moving to {TARGET_ORE} deposit at "
              f"({ore['x']:.0f}, {ore['y']:.0f})")

        result = api.move(ore["x"], ore["y"])
        print(f"[Mine] {result['message']}")

        # Wait for arrival (in a real agent, you'd poll position)
        time.sleep(2)

        # Step 4: Start mining
        print(f"[Mine] Starting mining at ({ore['x']:.0f}, {ore['y']:.0f})")
        result = api.mine(ore["x"], ore["y"])
        print(f"[Mine] {result['message']}")

        # Step 5: Monitor inventory and wait until full
        mining_time = 0
        max_mining_time = 15  # Safety timeout

        while mining_time < max_mining_time:
            time.sleep(1)
            mining_time += 1

            player = api.get_player()
            held_item = player.get("item")

            if held_item and held_item == TARGET_ORE:
                print(f"[Mine] Mining... holding {TARGET_ORE} "
                      f"({mining_time}s elapsed)")
            else:
                print(f"[Mine] Waiting for mining to start...")

            # Check if we're at capacity (would need item count from API)
            if mining_time >= 10:  # Approximate fill time
                print(f"[Mine] Inventory likely full, returning to base")
                break

        # Step 6: Stop mining
        print(f"[Mine] Finished mining, collected for {mining_time}s")

        # Step 7: Return to base (core)
        player = api.get_player()
        buildings = api.get_buildings(type="core", limit=5)

        if buildings["count"] > 0:
            core = buildings["buildings"][0]
            core_x = core["worldX"]
            core_y = core["worldY"]

            print(f"[Mine] Returning to core at ({core_x:.0f}, {core_y:.0f})")
            result = api.move(core_x, core_y)
            print(f"[Mine] {result['message']}")

            # Wait for arrival
            time.sleep(2)
            print(f"[Mine] Arrived at core, resources deposited")
        else:
            print("[Mine] WARNING: No core found!")

        # Step 8: Check progress
        resources = api.get_resources()
        current_amount = resources.get("resources", {}).get(TARGET_ORE, 0)
        collected = current_amount - initial_amount

        print(f"[Mine] Progress: {collected}/{DESIRED_AMOUNT} {TARGET_ORE} "
              f"({collected / DESIRED_AMOUNT * 100:.1f}%)")

        ore_index += 1  # Move to next deposit for variety

    print(f"\n[Mine] Mining complete! Collected "
          f"{current_amount - initial_amount} {TARGET_ORE}")


def smart_mining_with_drills():
    """
    Advanced example: Instead of manual mining, build drills at ore locations
    and connect them with conveyors.
    """
    api = MindustryAPI()

    # Ore locations with tile coordinates
    ore_sites = [
        {"tile_x": 25, "tile_y": 30, "ore": "copper"},
        {"tile_x": 40, "tile_y": 35, "ore": "lead"},
        {"tile_x": 35, "tile_y": 50, "ore": "coal"},
    ]

    for site in ore_sites:
        tx, ty = site["tile_x"], site["tile_y"]
        ore_type = site["ore"]

        print(f"[Mine] Setting up {ore_type} drill at ({tx}, {ty})")

        # Place a pneumatic drill on the ore
        result = api.build(tx, ty, "pneumatic-drill")
        print(f"[Mine] Drill: {result['message']}")
        time.sleep(0.2)

        # Add a conveyor next to it to carry items away
        # (assuming the drill outputs to the right)
        result = api.build(tx + 1, ty, "conveyor", rotation=0)
        print(f"[Mine] Output conveyor: {result['message']}")
        time.sleep(0.2)

    print("[Mine] All drill sites set up!")


if __name__ == "__main__":
    mining_automation()
    # Uncomment for drill-based automation:
    # smart_mining_with_drills()
```

---

## Example 5: Full AI Loop

This example demonstrates a complete observe-decide-act cycle, forming the core of an AI agent that can play Mindustry autonomously.

```python
#!/usr/bin/env python3
"""
Example 5: Full AI Loop

This script demonstrates a complete autonomous AI agent with:
- Observation phase: Collect all relevant game state
- Decision phase: Analyze state and decide on actions
- Action phase: Execute decided actions
- Learning feedback: Track outcomes for future decisions
"""

import time
import random
from dataclasses import dataclass
from enum import Enum
from typing import List, Dict, Any, Optional
from mindustry_api import MindustryAPI


class Strategy(Enum):
    """High-level strategic modes."""
    EARLY_GAME = "early_game"      # Focus on resource gathering
    BUILDING = "building"           # Focus on base construction
    DEFENSIVE = "defensive"         # Focus on defense
    OFFENSIVE = "offensive"         # Focus on attacking
    EXPANSION = "expansion"         # Focus on territory control


@dataclass
class GameState:
    """Structured representation of the game state."""
    player_x: float
    player_y: float
    player_health: float
    player_max_health: float
    resources: Dict[str, int]
    wave: int
    game_time: float
    unit_count: int
    building_count: int
    enemy_count: int
    nearby_enemies: List[Dict]
    nearby_buildings: List[Dict]
    strategy: Strategy


@dataclass
class Decision:
    """An AI decision with reasoning."""
    action: str
    parameters: Dict[str, Any]
    reasoning: str
    priority: int  # 1 = highest, 10 = lowest


class MindustryAI:
    """Autonomous AI agent for Mindustry."""

    def __init__(self):
        self.api = MindustryAPI()
        self.state = None
        self.last_strategy_change = 0
        self.current_strategy = Strategy.EARLY_GAME
        self.decision_history = []
        self.enemies_killed_estimate = 0

    def observe(self) -> GameState:
        """
        Observation Phase: Collect all relevant game state.
        This is the agent's "eyes and ears" -- everything it knows
        about the world comes from this phase.
        """
        # Get comprehensive state
        full_state = self.api.get_state()
        player = full_state["player"]
        resources = full_state.get("resources", {})
        map_info = full_state.get("map", {})

        # Scan for enemies
        px, py = player["x"], player["y"]
        enemies_response = self.api.get_units(
            team="crux",
            x=px, y=py,
            radius=300,
            limit=100
        )
        enemies = enemies_response.get("units", [])

        # Scan nearby buildings
        buildings_response = self.api.get_buildings(
            team="sharded",
            x=px, y=py,
            radius=200,
            limit=50
        )
        buildings = buildings_response.get("buildings", [])

        # Determine strategy
        strategy = self.determine_strategy(
            wave=map_info.get("wave", 0),
            resources=resources,
            enemy_count=len(enemies),
            building_count=len(buildings)
        )

        self.state = GameState(
            player_x=px,
            player_y=py,
            player_health=player.get("health", 100),
            player_max_health=player.get("maxHealth", 100),
            resources=resources,
            wave=map_info.get("wave", 0),
            game_time=map_info.get("gameTime", 0),
            unit_count=full_state.get("unitCount", 0),
            building_count=full_state.get("building_count", len(buildings)),
            enemy_count=len(enemies),
            nearby_enemies=enemies,
            nearby_buildings=buildings,
            strategy=strategy
        )

        return self.state

    def determine_strategy(self, wave: int, resources: Dict[str, int],
                           enemy_count: int, building_count: int) -> Strategy:
        """
        Determine the high-level strategy based on game state.
        """
        # Immediate threat -- switch to defense
        if enemy_count > 5:
            return Strategy.DEFENSIVE

        # Low health -- retreat and heal
        if self.state and self.state.player_health / self.state.player_max_health < 0.3:
            return Strategy.DEFENSIVE

        # Early game -- focus on resources
        if wave <= 3 and building_count < 10:
            return Strategy.EARLY_GAME

        # Mid game with good resources -- build up
        if wave <= 10 and resources.get("copper", 0) > 500:
            return Strategy.BUILDING

        # Late game -- prepare for heavy waves
        if wave > 15:
            return Strategy.DEFENSIVE

        # Default: continue current strategy
        return self.current_strategy

    def decide(self, state: GameState) -> List[Decision]:
        """
        Decision Phase: Analyze state and decide on actions.
        Returns a prioritized list of decisions.
        """
        decisions = []

        # Strategy-specific decision making
        if state.strategy == Strategy.DEFENSIVE:
            decisions.extend(self.defensive_decisions(state))
        elif state.strategy == Strategy.EARLY_GAME:
            decisions.extend(self.early_game_decisions(state))
        elif state.strategy == Strategy.BUILDING:
            decisions.extend(self.building_decisions(state))
        elif state.strategy == Strategy.OFFENSIVE:
            decisions.extend(self.offensive_decisions(state))

        # Always consider shooting if enemies are nearby
        if state.nearby_enemies:
            closest = min(state.nearby_enemies,
                          key=lambda e: ((e["x"] - state.player_x) ** 2 +
                                         (e["y"] - state.player_y) ** 2))
            decisions.append(Decision(
                action="shoot",
                parameters={"x": closest["x"], "y": closest["y"]},
                reasoning=f"Closest enemy {closest['type']} at distance "
                          f"{((closest['x']-state.player_x)**2 + (closest['y']-state.player_y)**2)**0.5:.0f}",
                priority=1  # Highest priority
            ))

        # Sort by priority
        decisions.sort(key=lambda d: d.priority)

        return decisions

    def defensive_decisions(self, state: GameState) -> List[Decision]:
        """Generate defensive decisions."""
        decisions = []

        # Command units to attack nearest enemies
        if state.nearby_enemies:
            target = state.nearby_enemies[0]
            decisions.append(Decision(
                action="command_attack",
                parameters={
                    "x": target["x"],
                    "y": target["y"],
                    "unit_filter": {"maxUnits": 20, "radius": 300}
                },
                reasoning="Command units to attack approaching enemies",
                priority=2
            ))

        # Build more turrets if we have resources
        if state.resources.get("copper", 0) > 100 and state.resources.get("lead", 0) > 50:
            # Place turret near player
            tx = int(state.player_x / 8) + random.randint(-5, 5)
            ty = int(state.player_y / 8) + random.randint(-5, 5)
            decisions.append(Decision(
                action="build",
                parameters={
                    "x": tx,
                    "y": ty,
                    "block": "duo",
                    "rotation": 0
                },
                reasoning=f"Build defensive turret at ({tx}, {ty})",
                priority=5
            ))

        return decisions

    def early_game_decisions(self, state: GameState) -> List[Decision]:
        """Generate early game resource gathering decisions."""
        decisions = []

        # Move to and mine copper
        if state.resources.get("copper", 0) < 300:
            # Look for a drill or ore location
            # For simplicity, move in a search pattern
            offset = random.randint(-100, 100)
            decisions.append(Decision(
                action="move",
                parameters={
                    "x": state.player_x + offset,
                    "y": state.player_y + offset
                },
                reasoning="Search for copper ore deposits",
                priority=3
            ))

            decisions.append(Decision(
                action="mine",
                parameters={
                    "x": state.player_x + offset,
                    "y": state.player_y + offset
                },
                reasoning="Mine copper ore",
                priority=4
            ))

        # Build basic production
        if state.resources.get("copper", 0) > 200:
            tx = int(state.player_x / 8) + 3
            ty = int(state.player_y / 8)
            decisions.append(Decision(
                action="build",
                parameters={
                    "x": tx,
                    "y": ty,
                    "block": "mechanical-drill"
                },
                reasoning="Build copper drill for passive income",
                priority=4
            ))

        return decisions

    def building_decisions(self, state: GameState) -> List[Decision]:
        """Generate base building decisions."""
        decisions = []

        # Build conveyors for logistics
        px = int(state.player_x / 8)
        py = int(state.player_y / 8)

        # Simple conveyor line
        for i in range(3):
            decisions.append(Decision(
                action="build",
                parameters={
                    "x": px + i,
                    "y": py + 2,
                    "block": "conveyor",
                    "rotation": 0  # Right
                },
                reasoning=f"Build conveyor segment {i+1}",
                priority=6
            ))

        # Build a router for distribution
        decisions.append(Decision(
            action="build",
            parameters={
                "x": px + 3,
                "y": py + 2,
                "block": "router"
            },
            reasoning="Build router for item distribution",
            priority=6
        ))

        return decisions

    def offensive_decisions(self, state: GameState) -> List[Decision]:
        """Generate offensive decisions."""
        decisions = []

        # Command units to push forward
        if state.nearby_enemies:
            # Target the enemy core if we know where it is
            enemy_buildings = self.api.get_buildings(team="crux", limit=10)
            if enemy_buildings["count"] > 0:
                target = enemy_buildings["buildings"][0]
                decisions.append(Decision(
                    action="command_attack",
                    parameters={
                        "x": target["worldX"],
                        "y": target["worldY"],
                        "unit_filter": {"maxUnits": 30, "radius": 400}
                    },
                    reasoning="Command all units to attack enemy base",
                    priority=2
                ))

        return decisions

    def act(self, decisions: List[Decision]) -> None:
        """
        Action Phase: Execute the decided actions.
        Only executes the top-priority decision to avoid overwhelming the queue.
        """
        if not decisions:
            print("[AI] No decisions to execute")
            return

        # Execute the highest priority decision
        decision = decisions[0]
        print(f"[AI] Executing: {decision.action}")
        print(f"[AI] Reason: {decision.reasoning}")

        try:
            if decision.action == "move":
                result = self.api.move(**decision.parameters)
                print(f"[AI] Move result: {result['message']}")

            elif decision.action == "shoot":
                result = self.api.shoot(**decision.parameters)
                print(f"[AI] Shoot result: {result['message']}")

            elif decision.action == "mine":
                result = self.api.mine(**decision.parameters)
                print(f"[AI] Mine result: {result['message']}")

            elif decision.action == "build":
                result = self.api.build(**decision.parameters)
                print(f"[AI] Build result: {result['message']}")

            elif decision.action == "command_attack":
                params = decision.parameters
                result = self.api.command_units(
                    command="attack",
                    x=params["x"],
                    y=params["y"],
                    unit_filter=params.get("unit_filter")
                )
                print(f"[AI] Command result: {result['message']}")

        except Exception as e:
            print(f"[AI] Action failed: {e}")

        # Store decision for learning
        self.decision_history.append(decision)

    def run(self, max_ticks: int = 1000, tick_interval: float = 1.0):
        """
        Main AI loop: Observe -> Decide -> Act.
        """
        print("=" * 50)
        print("Mindustry AI Agent Starting")
        print("=" * 50)

        # Verify connection
        health = self.api.health()
        print(f"[AI] Connected to: {health['message']}")
        print(f"[AI] Mod version: {health['version']}")

        for tick in range(1, max_ticks + 1):
            print(f"\n{'=' * 50}")
            print(f"[AI] Tick {tick} - Strategy: {self.current_strategy.value}")
            print(f"{'=' * 50}")

            try:
                # Phase 1: Observe
                print("\n[AI] --- OBSERVE ---")
                state = self.observe()
                self.current_strategy = state.strategy

                print(f"[AI] Player: ({state.player_x:.0f}, {state.player_y:.0f}) "
                      f"HP:{state.player_health:.0f}/{state.player_max_health:.0f}")
                print(f"[AI] Wave: {state.wave} | Enemies: {state.enemy_count} | "
                      f"Buildings: {state.building_count}")
                print(f"[AI] Copper: {state.resources.get('copper', 0)} | "
                      f"Lead: {state.resources.get('lead', 0)}")

                # Phase 2: Decide
                print("\n[AI] --- DECIDE ---")
                decisions = self.decide(state)

                if decisions:
                    print(f"[AI] Generated {len(decisions)} decisions:")
                    for i, d in enumerate(decisions[:5]):  # Show top 5
                        print(f"[AI]   {i+1}. [{d.action}] P{d.priority}: {d.reasoning}")
                else:
                    print("[AI] No decisions generated")

                # Phase 3: Act
                print("\n[AI] --- ACT ---")
                self.act(decisions)

            except Exception as e:
                print(f"[AI] ERROR in tick {tick}: {e}")
                import traceback
                traceback.print_exc()

            # Wait before next tick
            time.sleep(tick_interval)

        print("\n[AI] Max ticks reached. Shutting down.")
        print(f"[AI] Total decisions made: {len(self.decision_history)}")


def main():
    """Entry point for the AI agent."""
    ai = MindustryAI()

    try:
        ai.run(max_ticks=100, tick_interval=1.0)
    except KeyboardInterrupt:
        print("\n[AI] Interrupted by user")
        print(f"[AI] Decisions made: {len(ai.decision_history)}")


if __name__ == "__main__":
    main()
```

---

## Advanced Patterns

### Error Handling and Retry

```python
import time
from requests.exceptions import RequestException


def robust_api_call(api_func, max_retries=3, delay=1.0):
    """Wrapper for resilient API calls with retry logic."""
    for attempt in range(max_retries):
        try:
            return api_func()
        except RequestException as e:
            print(f"[Robust] API call failed (attempt {attempt + 1}/{max_retries}): {e}")
            if attempt < max_retries - 1:
                time.sleep(delay * (attempt + 1))  # Exponential backoff
            else:
                raise


# Usage
result = robust_api_call(lambda: api.get_state())
```

### Batch Processing

```python
def batch_build(api: MindustryAPI, placements: List[Dict], delay: float = 0.1):
    """Build multiple structures with rate limiting."""
    results = []
    for placement in placements:
        try:
            result = api.build(**placement)
            results.append(result)
            time.sleep(delay)
        except Exception as e:
            print(f"[Batch] Failed to build at ({placement.get('x')}, {placement.get('y')}): {e}")
            results.append(None)
    return results


# Usage: Build a 3x3 wall
wall_placements = []
for dx in range(-1, 2):
    for dy in range(-1, 2):
        wall_placements.append({
            "x": 20 + dx,
            "y": 20 + dy,
            "block": "copper-wall"
        })

results = batch_build(api, wall_placements)
```

### State Machine for Complex Behaviors

```python
from enum import Enum, auto


class AgentState(Enum):
    IDLE = auto()
    MOVING = auto()
    MINING = auto()
    BUILDING = auto()
    COMBAT = auto()
    RETREATING = auto()


class StateMachineAI:
    def __init__(self):
        self.api = MindustryAPI()
        self.state = AgentState.IDLE
        self.target = None
        self.state_entry_time = 0

    def transition_to(self, new_state: AgentState, target=None):
        print(f"[State] {self.state.name} -> {new_state.name}")
        self.state = new_state
        self.target = target
        self.state_entry_time = time.time()

    def update(self):
        player = self.api.get_player()
        state_duration = time.time() - self.state_entry_time

        if self.state == AgentState.IDLE:
            # Decide what to do next
            enemies = self.api.get_units(team="crux", x=player["x"], y=player["y"], radius=200)
            if enemies["count"] > 0:
                self.transition_to(AgentState.COMBAT)
            elif player.get("item") is None:
                self.transition_to(AgentState.MINING)
            else:
                self.transition_to(AgentState.BUILDING)

        elif self.state == AgentState.COMBAT:
            enemies = self.api.get_units(team="crux", x=player["x"], y=player["y"], radius=200)
            if enemies["count"] == 0:
                self.transition_to(AgentState.IDLE)
            elif player["health"] / player["maxHealth"] < 0.3:
                self.transition_to(AgentState.RETREATING)
            else:
                # Shoot at nearest enemy
                nearest = min(enemies["units"],
                              key=lambda e: (e["x"] - player["x"]) ** 2 + (e["y"] - player["y"]) ** 2)
                self.api.shoot(nearest["x"], nearest["y"])

        elif self.state == AgentState.RETREATING:
            # Move to core
            buildings = self.api.get_buildings(type="core", limit=1)
            if buildings["count"] > 0:
                core = buildings["buildings"][0]
                self.api.move(core["worldX"], core["worldY"])

            if player["health"] / player["maxHealth"] > 0.8:
                self.transition_to(AgentState.IDLE)
            elif state_duration > 30:
                # Timeout, try something else
                self.transition_to(AgentState.IDLE)

        elif self.state == AgentState.MINING:
            # Mine at target or find ore
            if self.target:
                self.api.mine(self.target["x"], self.target["y"])
                if state_duration > 15:
                    self.transition_to(AgentState.IDLE)
            else:
                self.transition_to(AgentState.IDLE)

        elif self.state == AgentState.BUILDING:
            # Build something useful
            if state_duration < 5:
                tx = int(player["x"] / 8) + 2
                ty = int(player["y"] / 8)
                self.api.build(tx, ty, "conveyor", rotation=0)
            else:
                self.transition_to(AgentState.IDLE)
```

### Multi-Agent Coordination

```python
class MultiAgentCoordinator:
    """
    Coordinate multiple AI agents (if running multiple instances)
    or coordinate different aspects of a single agent's behavior.
    """

    def __init__(self):
        self.api = MindustryAPI()
        self.roles = {
            "miner": {"focus": "copper,lead", "strategy": "gather"},
            "builder": {"focus": "construction", "strategy": "expand"},
            "defender": {"focus": "combat", "strategy": "defend"},
        }
        self.current_role = "miner"

    def assign_role(self, role: str):
        """Switch the agent's current role."""
        if role in self.roles:
            self.current_role = role
            print(f"[Coordinator] Role changed to: {role}")
            print(f"[Coordinator] Focus: {self.roles[role]['focus']}")

    def execute_role(self):
        """Execute actions based on current role."""
        role = self.current_role

        if role == "miner":
            self._execute_miner()
        elif role == "builder":
            self._execute_builder()
        elif role == "defender":
            self._execute_defender()

    def _execute_miner(self):
        """Miner role: Find and mine resources."""
        player = self.api.get_player()
        resources = self.api.get_resources()

        # Priority: copper > lead > coal > titanium
        priorities = ["copper", "lead", "coal", "titanium"]
        for resource in priorities:
            if resources.get("resources", {}).get(resource, 0) < 500:
                # Move to find and mine this resource
                print(f"[Miner] Need more {resource}, searching...")
                # Implementation would scan for ore tiles
                break

    def _execute_builder(self):
        """Builder role: Construct base infrastructure."""
        player = self.api.get_player()
        # Build conveyors, turrets, production buildings
        pass

    def _execute_defender(self):
        """Defender role: Protect the base."""
        # Scan for enemies, shoot, command units
        pass

    def run_coordinator(self, tick_interval=2.0):
        """Main coordination loop."""
        while True:
            state = self.api.get_state()
            enemies = state.get("unitCount", 0)  # Simplified

            # Dynamic role switching based on situation
            if enemies > 5 and self.current_role != "defender":
                self.assign_role("defender")
            elif enemies == 0 and self.current_role == "defender":
                self.assign_role("miner")

            self.execute_role()
            time.sleep(tick_interval)
```

---

## Summary

These examples demonstrate the core patterns for building AI agents with the Mindustry AI Agent Mod:

| Example | Focus | Complexity |
|---------|-------|------------|
| Scout the Map | Observation, filtering, reporting | Beginner |
| Build Conveyor Line | Action sequencing, rotation logic | Beginner |
| Defend the Base | Continuous monitoring, reactive AI | Intermediate |
| Mining Automation | State tracking, goal-oriented behavior | Intermediate |
| Full AI Loop | Observe-decide-act cycle, strategy switching | Advanced |

For more details on the API, see [API_REFERENCE.md](API_REFERENCE.md).  
For development information, see [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md).
