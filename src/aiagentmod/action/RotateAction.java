package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Call;
import aiagentmod.api.SimpleMap;
import aiagentmod.api.SimpleJson;

public class RotateAction {
    
    public ActionResult execute(SimpleMap params) {
        int buildingId = params.optInt("building_id", -1);
        if (buildingId < 0) return ActionResult.error("Invalid building_id");
        
        if (Vars.state.isMenu()) return ActionResult.error("Not in game");
        
        Building building = Groups.build.getByID(buildingId);
        if (building == null) return ActionResult.error("Building not found: " + buildingId);
        if (!building.isValid()) return ActionResult.error("Building not valid");
        if (!building.block.quickRotate && building.block.size > 1) {
            return ActionResult.error("Building cannot be rotated");
        }
        
        int previousRotation = building.rotation;
        int direction = params.optInt("direction", 1);
        boolean clockwise = direction >= 0;
        
        try { Call.rotateBlock(Vars.player, building, clockwise); }
        catch (Exception e) { return ActionResult.error("Rotation failed: " + e.getMessage()); }
        
        String data = SimpleJson.obj("building_id", SimpleJson.num(buildingId),
            "building_type", SimpleJson.str(building.block.name),
            "previous_rotation", SimpleJson.num(previousRotation),
            "new_rotation", SimpleJson.num(building.rotation));
        
        return ActionResult.ok("Building rotated", data);
    }
}
