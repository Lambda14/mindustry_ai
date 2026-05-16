package aiagentmod.action;

import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.gen.Unit;
import org.json.JSONObject;

/**
 * Action to mine resources at a position.
 * 
 * Parameters:
 * - x: world X coordinate (or tile_x for tile coordinates)
 * - y: world Y coordinate (or tile_y for tile coordinates)
 * - stop (optional): if true, stops mining instead of starting
 */
public class MineAction {
    
    /**
     * Starts mining at the specified position.
     */
    public ActionResult execute(JSONObject params) {
        // Validate game state
        if (Vars.state.isMenu()) {
            return ActionResult.error("Not in game (currently in menu)");
        }
        
        if (Vars.player == null || Vars.player.dead()) {
            return ActionResult.error("Player not available or dead");
        }
        
        // Check if stop was requested
        if (params.optBoolean("stop", false)) {
            return stopMining();
        }
        
        // Parse parameters
        float worldX = params.optFloat("x", -1);
        float worldY = params.optFloat("y", -1);
        
        // If world coords not provided, try tile coords
        if (worldX < 0 && worldY < 0) {
            int tileX = params.optInt("tile_x", -1);
            int tileY = params.optInt("tile_y", -1);
            if (tileX >= 0 && tileY >= 0) {
                Tile tile = Vars.world.tile(tileX, tileY);
                if (tile != null) {
                    worldX = tile.worldx();
                    worldY = tile.worldy();
                }
            }
        }
        
        if (worldX < 0 || worldY < 0) {
            return ActionResult.error("Invalid coordinates: provide x/y (world) or tile_x/tile_y");
        }
        
        Unit unit = Vars.player.unit();
        if (unit == null) {
            return ActionResult.error("No unit to mine with");
        }
        
        if (!unit.canMine()) {
            return ActionResult.error("Current unit cannot mine (unit type: " + unit.type.name + ")");
        }
        
        // Find the tile at the position
        Tile tile = Vars.world.tileWorld(worldX, worldY);
        if (tile == null) {
            return ActionResult.error("No tile at position [" + worldX + "," + worldY + "]");
        }
        
        // Check if there's ore to mine
        if (!(tile.overlay() instanceof OreBlock) || tile.overlay().itemDrop == null) {
            // Try to find nearest ore in range
            Tile oreTile = findNearestOre(unit.x, unit.y, unit.type.mineRange);
            if (oreTile != null) {
                tile = oreTile;
            } else {
                return ActionResult.error("No mineable ore at or near [" + worldX + "," + worldY + "]");
            }
        }
        
        // Set mining target
        unit.mineTile = tile;
        
        // Move toward the ore if not in range
        float distance = unit.dst(tile.worldx(), tile.worldy());
        if (distance > unit.type.mineRange * 0.8f) {
            unit.approach(new arc.math.geom.Vec2(tile.worldx(), tile.worldy()));
        }
        
        JSONObject data = new JSONObject();
        data.put("target_x", Math.round(tile.worldx() * 100f) / 100f);
        data.put("target_y", Math.round(tile.worldy() * 100f) / 100f);
        data.put("tile_x", tile.x);
        data.put("tile_y", tile.y);
        data.put("ore", tile.overlay().itemDrop.name);
        data.put("distance", Math.round(distance * 100f) / 100f);
        data.put("mine_range", Math.round(unit.type.mineRange * 100f) / 100f);
        data.put("mine_speed", Math.round(unit.type.mineSpeed * 100f) / 100f);
        
        return ActionResult.ok("Mining started: " + tile.overlay().itemDrop.name, data);
    }
    
    /**
     * Stops the current mining operation.
     */
    public ActionResult stopMining() {
        if (Vars.player == null || Vars.player.unit() == null) {
            return ActionResult.error("No unit to stop mining");
        }
        
        Unit unit = Vars.player.unit();
        Tile previousMine = unit.mineTile;
        unit.mineTile = null;
        
        JSONObject data = new JSONObject();
        if (previousMine != null && previousMine.overlay() != null && previousMine.overlay().itemDrop != null) {
            data.put("previous_ore", previousMine.overlay().itemDrop.name);
            data.put("previous_tile_x", previousMine.x);
            data.put("previous_tile_y", previousMine.y);
        }
        data.put("was_mining", previousMine != null);
        
        return ActionResult.ok("Mining stopped", data);
    }
    
    /**
     * Finds the nearest ore tile within range.
     */
    private Tile findNearestOre(float x, float y, float range) {
        Tile nearest = null;
        float nearestDist = Float.MAX_VALUE;
        
        int tileRange = (int) (range / Vars.tilesize) + 1;
        int centerX = (int) (x / Vars.tilesize);
        int centerY = (int) (y / Vars.tilesize);
        
        for (int dx = -tileRange; dx <= tileRange; dx++) {
            for (int dy = -tileRange; dy <= tileRange; dy++) {
                int tx = centerX + dx;
                int ty = centerY + dy;
                
                Tile tile = Vars.world.tile(tx, ty);
                if (tile == null) continue;
                if (!(tile.overlay() instanceof OreBlock)) continue;
                if (tile.overlay().itemDrop == null) continue;
                
                float dist = arc.math.geom.Vec2.dst(x, y, tile.worldx(), tile.worldy());
                if (dist < range && dist < nearestDist) {
                    nearestDist = dist;
                    nearest = tile;
                }
            }
        }
        
        return nearest;
    }
}
