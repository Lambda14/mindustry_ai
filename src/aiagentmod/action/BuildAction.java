package aiagentmod.action;

import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import org.json.JSONObject;

/**
 * Action to build or break blocks.
 * 
 * Build parameters:
 * - x: tile X coordinate
 * - y: tile Y coordinate
 * - block: block name (e.g., "conveyor", "router", "turret-duo")
 * - rotation (optional): rotation 0-3 (default: 0)
 * - config (optional): block-specific configuration
 * 
 * Break parameters:
 * - x: tile X coordinate
 * - y: tile Y coordinate
 */
public class BuildAction {
    
    /**
     * Places a block at the specified position.
     */
    public ActionResult execute(JSONObject params) {
        // Parse and validate parameters FIRST
        int tileX = params.optInt("x", -1);
        int tileY = params.optInt("y", -1);
        String blockName = params.optString("block", "");
        
        if (tileX < 0 || tileY < 0) {
            return ActionResult.error("Invalid tile coordinates: x=" + tileX + ", y=" + tileY);
        }
        
        if (blockName.isEmpty()) {
            return ActionResult.error("Block name not specified");
        }
        
        // Validate game state
        if (Vars.state.isMenu()) {
            return ActionResult.error("Not in game (currently in menu)");
        }
        
        if (Vars.player == null || Vars.player.dead()) {
            return ActionResult.error("Player not available or dead");
        }
        int rotation = params.optInt("rotation", 0);
        Object config = params.has("config") ? params.get("config") : null;
        
        // Find block
        Block block = Vars.content.block(blockName);
        if (block == null) {
            // Try with common prefixes
            block = Vars.content.block("core-" + blockName);
            if (block == null) {
                block = Vars.content.block("block-" + blockName);
            }
            if (block == null) {
                return ActionResult.error("Block not found: " + blockName + ". Use the internal block name (e.g., 'conveyor', 'router').");
            }
        }
        
        // Validate placement
        if (!Build.validPlace(block, Vars.player.team(), tileX, tileY, rotation)) {
            return ActionResult.error("Cannot place '" + blockName + "' at tile [" + tileX + "," + tileY + "]");
        }
        
        // Place block
        Unit unit = Vars.player.unit();
        if (unit == null) {
            return ActionResult.error("No unit to build with");
        }
        
        try {
            Build.beginPlace(unit, block, Vars.player.team(), tileX, tileY, rotation, config);
        } catch (Exception e) {
            return ActionResult.error("Build failed: " + e.getMessage());
        }
        
        JSONObject data = new JSONObject();
        data.put("x", tileX);
        data.put("y", tileY);
        data.put("block", block.name);
        data.put("block_localized", block.localizedName);
        data.put("rotation", rotation);
        data.put("world_x", tileX * Vars.tilesize);
        data.put("world_y", tileY * Vars.tilesize);
        
        return ActionResult.ok("Build initiated: " + block.localizedName + " at [" + tileX + "," + tileY + "]", data);
    }
    
    /**
     * Breaks/removes a block at the specified position.
     */
    public ActionResult executeBreak(JSONObject params) {
        // Validate game state
        if (Vars.state.isMenu()) {
            return ActionResult.error("Not in game (currently in menu)");
        }
        
        if (Vars.player == null || Vars.player.dead()) {
            return ActionResult.error("Player not available or dead");
        }
        
        // Parse parameters
        int tileX = params.optInt("x", -1);
        int tileY = params.optInt("y", -1);
        
        if (tileX < 0 || tileY < 0) {
            return ActionResult.error("Invalid tile coordinates: x=" + tileX + ", y=" + tileY);
        }
        
        // Check if there's a building to break
        Tile tile = Vars.world.tile(tileX, tileY);
        if (tile == null) {
            return ActionResult.error("Tile [" + tileX + "," + tileY + "] does not exist");
        }
        
        if (tile.build == null && tile.block() == null || tile.block().name.equals("air")) {
            return ActionResult.error("No building to break at [" + tileX + "," + tileY + "]");
        }
        
        String blockName = tile.block().name;
        
        // Validate break
        if (!Build.validBreak(Vars.player.team(), tileX, tileY)) {
            return ActionResult.error("Cannot break at [" + tileX + "," + tileY + "] (invalid position or not your building)");
        }
        
        // Break block
        Unit unit = Vars.player.unit();
        if (unit == null) {
            return ActionResult.error("No unit to break with");
        }
        
        try {
            Build.beginBreak(unit, Vars.player.team(), tileX, tileY);
        } catch (Exception e) {
            return ActionResult.error("Break failed: " + e.getMessage());
        }
        
        JSONObject data = new JSONObject();
        data.put("x", tileX);
        data.put("y", tileY);
        data.put("block", blockName);
        data.put("world_x", tileX * Vars.tilesize);
        data.put("world_y", tileY * Vars.tilesize);
        
        return ActionResult.ok("Break initiated: " + blockName + " at [" + tileX + "," + tileY + "]", data);
    }
}
