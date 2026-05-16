package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Unit;
import arc.math.geom.Vec2;
import aiagentmod.api.SimpleMap;
import aiagentmod.api.SimpleJson;

public class MoveAction {
    public ActionResult execute(SimpleMap params) {
        if (Vars.state.isMenu()) return ActionResult.error("Not in game (currently in menu)");
        if (Vars.player == null) return ActionResult.error("Player not available");
        if (Vars.player.dead()) return ActionResult.error("Player is dead");
        
        float targetX = params.optFloat("x", -1);
        float targetY = params.optFloat("y", -1);
        
        if (targetX < 0 && targetY < 0) {
            int tileX = params.optInt("tile_x", -1);
            int tileY = params.optInt("tile_y", -1);
            if (tileX >= 0 && tileY >= 0) {
                targetX = tileX * Vars.tilesize;
                targetY = tileY * Vars.tilesize;
            } else {
                return ActionResult.error("Invalid coordinates: provide x/y (world) or tile_x/tile_y");
            }
        }
        
        Unit unit = Vars.player.unit();
        if (unit == null) return ActionResult.error("No unit controlled by player");
        
        float currentX = unit.x;
        float currentY = unit.y;
        float distance = unit.dst(targetX, targetY);
        float stopDistance = params.optFloat("stop_distance", 5f);
        
        if (distance <= stopDistance) {
            unit.vel.set(0, 0);
            return ActionResult.ok("Already at target (within stop distance)",
                SimpleJson.obj("from_x", SimpleJson.num(currentX), "from_y", SimpleJson.num(currentY),
                    "target_x", SimpleJson.num(targetX), "target_y", SimpleJson.num(targetY),
                    "distance", SimpleJson.num(0)));
        }
        
        unit.approach(new Vec2(targetX, targetY));
        Vars.player.x = unit.x;
        Vars.player.y = unit.y;
        
        String data = SimpleJson.obj("from_x", SimpleJson.num(currentX), "from_y", SimpleJson.num(currentY),
            "target_x", SimpleJson.num(targetX), "target_y", SimpleJson.num(targetY),
            "distance", SimpleJson.num(distance));
        
        return ActionResult.ok("Moving to target (" + Math.round(distance) + " units away)", data);
    }
}
