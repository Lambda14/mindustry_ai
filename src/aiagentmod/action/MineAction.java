package aiagentmod.action;

import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.gen.Unit;
import aiagentmod.api.SimpleMap;
import aiagentmod.api.SimpleJson;

public class MineAction {
    
    public ActionResult execute(SimpleMap params) {
        if (Vars.state.isMenu()) return ActionResult.error("Not in game");
        if (Vars.player == null || Vars.player.dead()) return ActionResult.error("Player not available");
        if (params.optBoolean("stop", false)) return stopMining();
        
        float worldX = params.optFloat("x", -1);
        float worldY = params.optFloat("y", -1);
        
        if (worldX < 0 && worldY < 0) {
            int tileX = params.optInt("tile_x", -1);
            int tileY = params.optInt("tile_y", -1);
            if (tileX >= 0 && tileY >= 0) {
                Tile t = Vars.world.tile(tileX, tileY);
                if (t != null) { worldX = t.worldx(); worldY = t.worldy(); }
            }
        }
        
        if (worldX < 0 || worldY < 0) return ActionResult.error("Invalid coordinates");
        
        Unit unit = Vars.player.unit();
        if (unit == null) return ActionResult.error("No unit");
        if (!unit.canMine()) return ActionResult.error("Unit cannot mine");
        
        Tile tile = Vars.world.tileWorld(worldX, worldY);
        if (tile == null || !(tile.overlay() instanceof OreBlock) || tile.overlay().itemDrop == null) {
            Tile oreTile = findNearestOre(unit.x, unit.y, unit.type.mineRange);
            if (oreTile != null) tile = oreTile;
            else return ActionResult.error("No mineable ore at or near [" + worldX + "," + worldY + "]");
        }
        
        unit.mineTile = tile;
        float distance = unit.dst(tile.worldx(), tile.worldy());
        if (distance > unit.type.mineRange * 0.8f) {
            unit.approach(new arc.math.geom.Vec2(tile.worldx(), tile.worldy()));
        }
        
        String data = SimpleJson.obj("target_x", SimpleJson.num(tile.worldx()),
            "target_y", SimpleJson.num(tile.worldy()), "tile_x", SimpleJson.num(tile.x),
            "tile_y", SimpleJson.num(tile.y), "ore", SimpleJson.str(tile.overlay().itemDrop.name),
            "distance", SimpleJson.num(distance), "mine_range", SimpleJson.num(unit.type.mineRange));
        
        return ActionResult.ok("Mining started: " + tile.overlay().itemDrop.name, data);
    }
    
    public ActionResult stopMining() {
        if (Vars.player == null || Vars.player.unit() == null) return ActionResult.error("No unit");
        Vars.player.unit().mineTile = null;
        return ActionResult.ok("Mining stopped");
    }
    
    private Tile findNearestOre(float x, float y, float range) {
        Tile nearest = null;
        float nearestDist = Float.MAX_VALUE;
        int tileRange = (int)(range / Vars.tilesize) + 1;
        int centerX = (int)(x / Vars.tilesize);
        int centerY = (int)(y / Vars.tilesize);
        
        for (int dx = -tileRange; dx <= tileRange; dx++) {
            for (int dy = -tileRange; dy <= tileRange; dy++) {
                Tile tile = Vars.world.tile(centerX + dx, centerY + dy);
                if (tile == null || !(tile.overlay() instanceof OreBlock) || tile.overlay().itemDrop == null) continue;
                float dist = arc.math.geom.Vec2.dst(x, y, tile.worldx(), tile.worldy());
                if (dist < range && dist < nearestDist) { nearestDist = dist; nearest = tile; }
            }
        }
        return nearest;
    }
}
