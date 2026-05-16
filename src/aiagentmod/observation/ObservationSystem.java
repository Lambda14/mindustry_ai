package aiagentmod.observation;

import mindustry.Vars;
import org.json.JSONObject;

/**
 * Main coordinator for the observation system.
 * Provides a unified interface for scanning all game state.
 * All operations are read-only and thread-safe.
 */
public class ObservationSystem {
    
    private final WorldScanner worldScanner;
    private final PlayerScanner playerScanner;
    private final UnitScanner unitScanner;
    private final BuildingScanner buildingScanner;
    private final ResourceScanner resourceScanner;
    
    public ObservationSystem() {
        this.worldScanner = new WorldScanner();
        this.playerScanner = new PlayerScanner();
        this.unitScanner = new UnitScanner();
        this.buildingScanner = new BuildingScanner();
        this.resourceScanner = new ResourceScanner();
    }
    
    /**
     * Returns a complete snapshot of the current game state.
     * This includes player, nearby units, nearby buildings, resources, and world info.
     */
    public JSONObject scanFullState() {
        JSONObject result = new JSONObject();
        
        if (Vars.state.isMenu()) {
            result.put("status", "in_menu");
            result.put("in_game", false);
            return result;
        }
        
        result.put("status", "in_game");
        result.put("in_game", true);
        result.put("timestamp", System.currentTimeMillis());
        
        // Game info
        JSONObject game = new JSONObject();
        game.put("is_menu", Vars.state.isMenu());
        game.put("is_playing", !Vars.state.isMenu());
        game.put("is_paused", Vars.state.isPaused());
        game.put("is_game_over", Vars.state.isGameOver());
        game.put("map_name", Vars.state.map != null ? Vars.state.map.name() : "unknown");
        game.put("wave", Vars.state.wave);
        game.put("wave_time", Math.round(Vars.state.wavetime * 100f) / 100f);
        game.put("wave_spacing", Math.round(Vars.state.rules.waveSpacing * 100f) / 100f);
        game.put("units_total", Vars.state.rules.waveTeam.data().unitCount);
        result.put("game", game);
        
        // Player
        result.put("player", playerScanner.scanPlayer());
        
        // Nearby units (around player)
        if (Vars.player != null) {
            result.put("nearby_units", unitScanner.scanNearbyUnits(Vars.player.x, Vars.player.y, 500));
        } else {
            result.put("nearby_units", new JSONObject().put("units", new org.json.JSONArray()).put("count", 0));
        }
        
        // Nearby buildings
        if (Vars.player != null) {
            result.put("nearby_buildings", buildingScanner.scanNearbyBuildings(Vars.player.x, Vars.player.y, 500));
        } else {
            result.put("nearby_buildings", new JSONObject().put("buildings", new org.json.JSONArray()).put("count", 0));
        }
        
        // Resources
        if (Vars.player != null) {
            result.put("resources", resourceScanner.scanResources(null));
        }
        
        // Summary stats
        result.put("summary", getSummary());
        
        return result;
    }
    
    /**
     * Scans the world/map information.
     */
    public JSONObject scanMap() {
        return worldScanner.scanMap();
    }
    
    /**
     * Scans a specific tile.
     */
    public JSONObject scanTile(int x, int y) {
        return worldScanner.scanTile(x, y);
    }
    
    /**
     * Scans a region of the map.
     */
    public JSONObject scanRegion(int centerX, int centerY, int radius) {
        return worldScanner.scanRegion(centerX, centerY, radius);
    }
    
    /**
     * Scans the player state.
     */
    public JSONObject scanPlayer() {
        return playerScanner.scanPlayer();
    }
    
    /**
     * Scans units with optional filtering.
     */
    public JSONObject scanUnits(String team, float x, float y, float radius) {
        return unitScanner.scanUnits(team, x, y, radius);
    }
    
    /**
     * Scans all units.
     */
    public JSONObject scanAllUnits() {
        return unitScanner.scanAllUnits();
    }
    
    /**
     * Scans a specific unit by ID.
     */
    public JSONObject scanUnit(int unitId) {
        return unitScanner.scanUnit(unitId);
    }
    
    /**
     * Scans buildings with optional filtering.
     */
    public JSONObject scanBuildings(String team, float x, float y, float radius) {
        return buildingScanner.scanBuildings(team, x, y, radius);
    }
    
    /**
     * Scans all buildings.
     */
    public JSONObject scanAllBuildings() {
        return buildingScanner.scanAllBuildings();
    }
    
    /**
     * Scans a specific building by ID.
     */
    public JSONObject scanBuilding(int buildingId) {
        return buildingScanner.scanBuilding(buildingId);
    }
    
    /**
     * Scans core buildings.
     */
    public JSONObject scanCores() {
        return buildingScanner.scanCores();
    }
    
    /**
     * Scans team resources.
     */
    public JSONObject scanResources(String team) {
        return resourceScanner.scanResources(team);
    }
    
    /**
     * Returns a quick summary of key game metrics.
     */
    public JSONObject getSummary() {
        JSONObject summary = new JSONObject();
        
        if (Vars.state.isMenu()) {
            summary.put("status", "in_menu");
            return summary;
        }
        
        summary.put("status", "playing");
        summary.put("wave", Vars.state.wave);
        summary.put("units_in_world", mindustry.gen.Groups.unit.size());
        summary.put("buildings_in_world", mindustry.gen.Groups.build.size());
        
        if (Vars.player != null && Vars.player.unit() != null && !Vars.player.unit().dead()) {
            summary.put("player_health", Math.round(Vars.player.unit().health * 100f) / 100f);
            summary.put("player_max_health", Math.round(Vars.player.unit().maxHealth * 100f) / 100f);
            summary.put("player_unit_type", Vars.player.unit().type.name);
        }
        
        if (Vars.player != null && Vars.player.team() != null && Vars.player.team().core() != null) {
            summary.put("core_health", Math.round(Vars.player.team().core().health * 100f) / 100f);
            summary.put("core_max_health", Math.round(Vars.player.team().core().maxHealth * 100f) / 100f);
        }
        
        return summary;
    }
    
    /**
     * Returns a lightweight status check.
     */
    public JSONObject getStatus() {
        JSONObject status = new JSONObject();
        status.put("in_game", !Vars.state.isMenu());
        status.put("is_paused", Vars.state.isPaused());
        status.put("has_player", Vars.player != null);
        status.put("player_alive", Vars.player != null && !Vars.player.dead());
        status.put("wave", Vars.state.wave);
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}
