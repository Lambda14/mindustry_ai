package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.gen.Groups;
import aiagentmod.api.SimpleMap;
import aiagentmod.api.SimpleJson;

public class ShootAction {
    
    public ActionResult execute(SimpleMap params) {
        if (Vars.state.isMenu()) return ActionResult.error("Not in game");
        if (Vars.player == null || Vars.player.dead()) return ActionResult.error("Player dead");
        
        Unit unit = Vars.player.unit();
        if (unit == null) return ActionResult.error("No unit");
        
        boolean shouldShoot = params.optBoolean("shoot", true);
        
        if (shouldShoot) {
            float targetX = params.optFloat("x", -1);
            float targetY = params.optFloat("y", -1);
            
            if (targetX < 0 || targetY < 0) {
                int targetId = params.optInt("target_id", -1);
                if (targetId >= 0) {
                    var tu = Groups.unit.getByID(targetId);
                    if (tu != null) { targetX = tu.x; targetY = tu.y; }
                    else {
                        var tb = Groups.build.getByID(targetId);
                        if (tb != null) { targetX = tb.x; targetY = tb.y; }
                    }
                }
            }
            
            if (targetX < 0 || targetY < 0) return ActionResult.error("Invalid target");
            
            unit.aimX = targetX;
            unit.aimY = targetY;
            unit.isShooting = true;
            float angle = arc.math.Angle.angle(unit.x, unit.y, targetX, targetY);
            unit.rotation = angle;
            
            String data = SimpleJson.obj("aim_x", SimpleJson.num(targetX), "aim_y", SimpleJson.num(targetY),
                "angle", SimpleJson.num(angle), "shooting", "true");
            return ActionResult.ok("Shooting at target [" + Math.round(targetX) + "," + Math.round(targetY) + "]", data);
        } else {
            unit.isShooting = false;
            String data = SimpleJson.obj("shooting", "false");
            return ActionResult.ok("Stopped shooting", data);
        }
    }
}
