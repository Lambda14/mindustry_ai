package aiagentmod.observation;

import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.Floor;
import arc.struct.Seq;
import arc.struct.ObjectSet;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Scans the game world/map for terrain, ores, and strategic information.
 */
public class WorldScanner {
    
    /**
     * Scans the full map and returns comprehensive map information.
     */
    public JSONObject scanMap() {
        JSONObject result = new JSONObject();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            return result;
        }
        
        result.put("width", Vars.world.width());
        result.put("height", Vars.world.height());
        result.put("tilesize", Vars.tilesize);
        result.put("world_width", Vars.world.unitWidth());
        result.put("world_height", Vars.world.unitHeight());
        result.put("map_name", Vars.state.map != null ? Vars.state.map.name() : "unknown");
        result.put("map_author", Vars.state.map != null ? Vars.state.map.author() : "unknown");
        result.put("map_description", Vars.state.map != null ? Vars.state.map.description() : "");
        
        // Scan for ores
        result.put("ores", scanOres());
        
        // Scan for enemy spawn points
        result.put("spawns", scanSpawns());
        
        // Player spawn/core position
        if (Vars.player != null && Vars.player.core() != null) {
            JSONObject core = new JSONObject();
            core.put("x", Vars.player.core().x);
            core.put("y", Vars.player.core().y);
            core.put("tile_x", Vars.player.core().tileX());
            core.put("tile_y", Vars.player.core().tileY());
            result.put("player_core", core);
        }
        
        // Wave info
        result.put("wave", Vars.state.wave);
        result.put("wave_time", Math.round(Vars.state.wavetime * 100f) / 100f);
        
        return result;
    }
    
    /**
     * Scans a single tile at the given coordinates.
     */
    public JSONObject scanTile(int x, int y) {
        JSONObject result = new JSONObject();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            return result;
        }
        
        Tile tile = Vars.world.tile(x, y);
        if (tile == null) {
            result.put("error", "out_of_bounds");
            return result;
        }
        
        result.put("x", x);
        result.put("y", y);
        result.put("world_x", tile.worldx());
        result.put("world_y", tile.worldy());
        result.put("floor", tile.floor().name);
        result.put("floor_localized", tile.floor().localizedName);
        result.put("block", tile.block().name);
        result.put("block_localized", tile.block().localizedName);
        result.put("overlay", tile.overlay().name);
        result.put("team", tile.team().name);
        result.put("has_building", tile.build != null);
        result.put("solid", tile.solid());
        result.put("floor_danger", tile.floor().isDeep() || tile.floor().damageTaken > 0);
        
        if (tile.build != null) {
            JSONObject building = new JSONObject();
            building.put("type", tile.build.block.name);
            building.put("id", tile.build.id);
            building.put("health", Math.round(tile.build.health));
            building.put("max_health", Math.round(tile.build.maxHealth));
            building.put("team", tile.build.team.name);
            building.put("rotation", tile.build.rotation);
            building.put("enabled", tile.build.enabled);
            result.put("building", building);
        }
        
        // Check for ore
        if (tile.overlay() instanceof OreBlock) {
            OreBlock ore = (OreBlock) tile.overlay();
            if (ore.itemDrop != null) {
                result.put("ore", ore.itemDrop.name);
            }
        }
        
        return result;
    }
    
    /**
     * Scans a region of tiles around a center point.
     */
    public JSONObject scanRegion(int centerX, int centerY, int radius) {
        JSONObject result = new JSONObject();
        JSONArray tiles = new JSONArray();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            return result;
        }
        
        int startX = Math.max(0, centerX - radius);
        int startY = Math.max(0, centerY - radius);
        int endX = Math.min(Vars.world.width() - 1, centerX + radius);
        int endY = Math.min(Vars.world.height() - 1, centerY + radius);
        
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                Tile tile = Vars.world.tile(x, y);
                if (tile == null) continue;
                
                JSONObject tileJson = new JSONObject();
                tileJson.put("x", x);
                tileJson.put("y", y);
                tileJson.put("floor", tile.floor().name);
                tileJson.put("block", tile.block().name);
                tileJson.put("solid", tile.solid());
                
                if (tile.overlay() instanceof OreBlock && ((OreBlock) tile.overlay()).itemDrop != null) {
                    tileJson.put("ore", ((OreBlock) tile.overlay()).itemDrop.name);
                }
                
                if (tile.build != null) {
                    tileJson.put("building_type", tile.build.block.name);
                    tileJson.put("building_team", tile.build.team.name);
                }
                
                tiles.put(tileJson);
            }
        }
        
        result.put("center_x", centerX);
        result.put("center_y", centerY);
        result.put("radius", radius);
        result.put("tiles", tiles);
        result.put("count", tiles.length());
        
        return result;
    }
    
    /**
     * Scans for all ore deposits on the map.
     */
    private JSONArray scanOres() {
        JSONArray ores = new JSONArray();
        ObjectSet<String> foundOres = new ObjectSet<>();
        
        // Sample ore positions (not every tile to save performance)
        int step = Math.max(1, Math.min(Vars.world.width(), Vars.world.height()) / 20);
        
        for (int x = 0; x < Vars.world.width(); x += step) {
            for (int y = 0; y < Vars.world.height(); y += step) {
                Tile tile = Vars.world.tile(x, y);
                if (tile == null || !(tile.overlay() instanceof OreBlock)) continue;
                
                OreBlock ore = (OreBlock) tile.overlay();
                if (ore.itemDrop == null) continue;
                
                String oreName = ore.itemDrop.name;
                if (foundOres.contains(oreName)) continue;
                foundOres.add(oreName);
                
                JSONObject oreInfo = new JSONObject();
                oreInfo.put("type", oreName);
                oreInfo.put("sample_x", x);
                oreInfo.put("sample_y", y);
                oreInfo.put("sample_world_x", tile.worldx());
                oreInfo.put("sample_world_y", tile.worldy());
                ores.put(oreInfo);
            }
        }
        
        return ores;
    }
    
    /**
     * Scans for enemy spawn points.
     */
    private JSONArray scanSpawns() {
        JSONArray spawns = new JSONArray();
        
        if (Vars.spawner != null) {
            for (var spawn : Vars.spawner.getSpawns()) {
                if (spawn == null) continue;
                JSONObject spawnInfo = new JSONObject();
                spawnInfo.put("x", spawn.x);
                spawnInfo.put("y", spawn.y);
                spawnInfo.put("world_x", spawn.worldx());
                spawnInfo.put("world_y", spawn.worldy());
                spawns.put(spawnInfo);
            }
        }
        
        return spawns;
    }
}
