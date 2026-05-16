package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Unit;
import org.json.JSONObject;

/**
 * Action to aim and shoot at a target position.
 * 
 * Parameters:
 * - x: target world X coordinate
 * - y: target world Y coordinate
 * - shoot (optional): if false, stops shooting (default: true)
 */
public class ShootAction {
    
    /**
     * Aims and optionally shoots at the target position.
     */
    public ActionResult execute(JSONObject params) {
        // Validate game state
        if (Vars.state.isMenu()) {
            return ActionResult.error("Not in game (currently in menu)");
        }
        
        if (Vars.player == null || Vars.player.dead()) {
            return ActionResult.error("Player not available or dead");
        }
        
        Unit unit = Vars.player.unit();
        if (unit == null) {
            return ActionResult.error("No unit to shoot with");
        }
        
        boolean shouldShoot = params.optBoolean("shoot", true);
        
        if (shouldShoot) {
            // Parse target position
            float targetX = params.optFloat("x", -1);
            float targetY = params.optFloat("y", -1);
            
            // Try getting from target entity
            if (targetX < 0 || targetY < 0) {
                int targetId = params.optInt("target_id", -1);
                if (targetId >= 0) {
                    var targetUnit = mindustry.gen.Groups.unit.getByID(targetId);
                    if (targetUnit != null) {
                        targetX = targetUnit.x;
                        targetY = targetUnit.y;
                    } else {
                        var targetBuilding = mindustry.gen.Groups.build.getByID(targetId);
                        if (targetBuilding != null) {
                            targetX = targetBuilding.x;
                            targetY = targetBuilding.y;
                        }
                    }
                }
            }
            
            if (targetX < 0 || targetY < 0) {
                return ActionResult.error("Invalid target: provide x/y coordinates or target_id");
            }
            
            // Set aim position
            unit.aimX = targetX;
            unit.aimY = targetY;
            unit.isShooting = true;
            
            // Rotate toward target
            float angle = arc.math.Angle.angle(unit.x, unit.y, targetX, targetY);
            unit.rotation = angle;
            
            JSONObject data = new JSONObject();
            data.put("aim_x", Math.round(targetX * 100f) / 100f);
            data.put("aim_y", Math.round(targetY * 100f) / 100f);
            data.put("angle", Math.round(angle * 100f) / 100f);
            data.put("shooting", true);
            
            return ActionResult.ok("Shooting at target [" + Math.round(targetX) + "," + Math.round(targetY) + "]", data);
            
        } else {
            // Stop shooting
            unit.isShooting = false;
            
            JSONObject data = new JSONObject();
            data.put("shooting", false);
            data.put("previous_aim_x", Math.round(unit.aimX * 100f) / 100f);
            data.put("previous_aim_y", Math.round(unit.aimY * 100f) / 100f);
            
            return ActionResult.ok("Stopped shooting", data);
        }
    }
}
