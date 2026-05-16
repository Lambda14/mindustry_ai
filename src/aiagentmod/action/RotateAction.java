package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Call;
import org.json.JSONObject;

/**
 * Action to rotate a building.
 * 
 * Parameters:
 * - building_id: ID of the building to rotate
 * - direction: 1 for clockwise, -1 for counter-clockwise (default: 1)
 */
public class RotateAction {
    
    /**
     * Rotates a building in the specified direction.
     */
    public ActionResult execute(JSONObject params) {
        // Parse and validate parameters FIRST
        int buildingId = params.optInt("building_id", -1);
        if (buildingId < 0) {
            return ActionResult.error("Invalid building_id: " + buildingId);
        }
        
        // Validate game state
        if (Vars.state.isMenu()) {
            return ActionResult.error("Not in game (currently in menu)");
        }
        
        if (Vars.player == null) {
            return ActionResult.error("Player not available");
        }
        
        Building building = Groups.build.getByID(buildingId);
        if (building == null) {
            return ActionResult.error("Building not found with ID: " + buildingId);
        }
        
        if (!building.isValid()) {
            return ActionResult.error("Building is not valid (may be destroyed)");
        }
        
        // Check if building can be rotated
        if (!building.block.quickRotate && building.block.size > 1) {
            return ActionResult.error("Building '" + building.block.name + "' cannot be rotated");
        }
        
        int previousRotation = building.rotation;
        
        // Parse direction
        int direction = params.optInt("direction", 1);
        boolean clockwise = direction >= 0;
        
        try {
            Call.rotateBlock(Vars.player, building, clockwise);
        } catch (Exception e) {
            return ActionResult.error("Rotation failed: " + e.getMessage());
        }
        
        JSONObject data = new JSONObject();
        data.put("building_id", buildingId);
        data.put("building_type", building.block.name);
        data.put("building_localized", building.block.localizedName);
        data.put("previous_rotation", previousRotation);
        data.put("new_rotation", building.rotation);
        data.put("direction", clockwise ? "clockwise" : "counter-clockwise");
        
        return ActionResult.ok("Building rotated: " + building.block.localizedName + 
            " from rotation " + previousRotation + " to " + building.rotation, data);
    }
}
