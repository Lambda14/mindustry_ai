package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import arc.math.geom.Vec2;
import org.json.JSONObject;

/**
 * Action to move the player/unit to a target position.
 * 
 * Parameters:
 * - x: target world X coordinate
 * - y: target world Y coordinate
 * - stop_distance (optional): distance to stop at (default: 5)
 */
public class MoveAction {
    
    public ActionResult execute(JSONObject params) {
        // Validate game state
        if (Vars.state.isMenu()) {
            return ActionResult.error("Not in game (currently in menu)");
        }
        
        if (Vars.player == null) {
            return ActionResult.error("Player not available");
        }
        
        if (Vars.player.dead()) {
            return ActionResult.error("Player is dead");
        }
        
        // Parse parameters
        float targetX = params.optFloat("x", -1);
        float targetY = params.optFloat("y", -1);
        
        if (targetX < 0 && targetY < 0) {
            // Try parsing as tile coordinates
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
        if (unit == null) {
            return ActionResult.error("No unit controlled by player");
        }
        
        // Get current position
        float currentX = unit.x;
        float currentY = unit.y;
        
        // Calculate distance
        float distance = unit.dst(targetX, targetY);
        float stopDistance = params.optFloat("stop_distance", 5f);
        
        // Move unit toward target using velocity-based approach
        Vec2 target = new Vec2(targetX, targetY);
        
        // If close enough, stop
        if (distance <= stopDistance) {
            unit.vel.set(0, 0);
            return ActionResult.ok("Already at target (within stop distance)", 
                createMoveData(currentX, currentY, targetX, targetY, 0));
        }
        
        // Move toward target
        unit.approach(target);
        
        // Update player position to match unit
        Vars.player.x = unit.x;
        Vars.player.y = unit.y;
        
        JSONObject data = createMoveData(currentX, currentY, targetX, targetY, distance);
        
        return ActionResult.ok("Moving to target (" + Math.round(distance) + " units away)", data);
    }
    
    private JSONObject createMoveData(float fromX, float fromY, float toX, float toY, float distance) {
        JSONObject data = new JSONObject();
        data.put("from_x", Math.round(fromX * 100f) / 100f);
        data.put("from_y", Math.round(fromY * 100f) / 100f);
        data.put("target_x", Math.round(toX * 100f) / 100f);
        data.put("target_y", Math.round(toY * 100f) / 100f);
        data.put("distance", Math.round(distance * 100f) / 100f);
        return data;
    }
}
