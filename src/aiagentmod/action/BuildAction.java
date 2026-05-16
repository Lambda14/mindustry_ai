package aiagentmod.action;

import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.gen.Unit;
import aiagentmod.api.SimpleMap;
import aiagentmod.api.SimpleJson;

public class BuildAction {
    
    public ActionResult execute(SimpleMap params) {
        int tileX = params.optInt("x", -1);
        int tileY = params.optInt("y", -1);
        String blockName = params.optString("block", "");
        
        if (tileX < 0 || tileY < 0) return ActionResult.error("Invalid tile coordinates");
        if (blockName.isEmpty()) return ActionResult.error("Block name not specified");
        
        if (Vars.state.isMenu()) return ActionResult.error("Not in game (currently in menu)");
        if (Vars.player == null || Vars.player.dead()) return ActionResult.error("Player not available or dead");
        
        Block block = Vars.content.block(blockName);
        if (block == null) {
            block = Vars.content.block("core-" + blockName);
            if (block == null) return ActionResult.error("Block not found: " + blockName);
        }
        
        int rotation = params.optInt("rotation", 0);
        
        if (!Build.validPlace(block, Vars.player.team(), tileX, tileY, rotation)) {
            return ActionResult.error("Cannot place '" + blockName + "' at [" + tileX + "," + tileY + "]");
        }
        
        Unit unit = Vars.player.unit();
        if (unit == null) return ActionResult.error("No unit to build with");
        
        try {
            Build.beginPlace(unit, block, Vars.player.team(), tileX, tileY, rotation, null);
        } catch (Exception e) {
            return ActionResult.error("Build failed: " + e.getMessage());
        }
        
        String data = SimpleJson.obj("x", SimpleJson.num(tileX), "y", SimpleJson.num(tileY),
            "block", SimpleJson.str(block.name), "block_localized", SimpleJson.str(block.localizedName),
            "rotation", SimpleJson.num(rotation), "world_x", SimpleJson.num(tileX * Vars.tilesize),
            "world_y", SimpleJson.num(tileY * Vars.tilesize));
        
        return ActionResult.ok("Build initiated: " + block.localizedName + " at [" + tileX + "," + tileY + "]", data);
    }
    
    public ActionResult executeBreak(SimpleMap params) {
        int tileX = params.optInt("x", -1);
        int tileY = params.optInt("y", -1);
        
        if (tileX < 0 || tileY < 0) return ActionResult.error("Invalid tile coordinates");
        if (Vars.state.isMenu()) return ActionResult.error("Not in game");
        if (Vars.player == null || Vars.player.dead()) return ActionResult.error("Player not available");
        
        Tile tile = Vars.world.tile(tileX, tileY);
        if (tile == null || (tile.build == null && (tile.block() == null || tile.block().name.equals("air")))) {
            return ActionResult.error("No building to break at [" + tileX + "," + tileY + "]");
        }
        
        String blockName = tile.block().name;
        
        if (!Build.validBreak(Vars.player.team(), tileX, tileY)) {
            return ActionResult.error("Cannot break at [" + tileX + "," + tileY + "]");
        }
        
        try {
            Build.beginBreak(Vars.player.unit(), Vars.player.team(), tileX, tileY);
        } catch (Exception e) {
            return ActionResult.error("Break failed: " + e.getMessage());
        }
        
        String data = SimpleJson.obj("x", SimpleJson.num(tileX), "y", SimpleJson.num(tileY),
            "block", SimpleJson.str(blockName), "world_x", SimpleJson.num(tileX * Vars.tilesize),
            "world_y", SimpleJson.num(tileY * Vars.tilesize));
        
        return ActionResult.ok("Break initiated: " + blockName + " at [" + tileX + "," + tileY + "]", data);
    }
}
