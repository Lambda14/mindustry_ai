package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.gen.Groups;
import mindustry.gen.Call;
import mindustry.gen.Building;
import mindustry.ai.types.CommandAI;
import mindustry.type.UnitCommand;
import arc.math.geom.Vec2;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Action to command multiple units.
 * 
 * Parameters:
 * - unit_ids: array of unit IDs to command
 * - command: command type ("move", "attack", "mine", "repair", "rebuild", "assist", "rally")
 * - x: target world X coordinate (for position-based commands)
 * - y: target world Y coordinate (for position-based commands)
 * - target_id (optional): target unit/building ID to attack/interact with
 */
public class UnitCommandAction {
    
    /**
     * Sends a command to the specified units.
     */
    public ActionResult execute(JSONObject params) {
        // Validate game state
        if (Vars.state.isMenu()) {
            return ActionResult.error("Not in game (currently in menu)");
        }
        
        if (Vars.player == null) {
            return ActionResult.error("Player not available");
        }
        
        // Parse unit IDs
        JSONArray unitIdsJson = params.optJSONArray("unit_ids");
        if (unitIdsJson == null || unitIdsJson.length() == 0) {
            // Try to use selected units if no IDs provided
            return ActionResult.error("No unit_ids specified");
        }
        
        int[] unitIds = new int[unitIdsJson.length()];
        for (int i = 0; i < unitIdsJson.length(); i++) {
            unitIds[i] = unitIdsJson.getInt(i);
        }
        
        // Parse command type
        String commandStr = params.optString("command", "move").toLowerCase();
        
        // Parse target position
        float targetX = params.optFloat("x", -1);
        float targetY = params.optFloat("y", -1);
        
        // Parse target entity
        int targetId = params.optInt("target_id", -1);
        Building buildTarget = null;
        Unit unitTarget = null;
        Vec2 posTarget = null;
        
        if (targetId >= 0) {
            Unit targetUnit = Groups.unit.getByID(targetId);
            if (targetUnit != null) {
                unitTarget = targetUnit;
            } else {
                Building targetBuilding = Groups.build.getByID(targetId);
                if (targetBuilding != null) {
                    buildTarget = targetBuilding;
                }
            }
        }
        
        if (targetX >= 0 && targetY >= 0) {
            posTarget = new Vec2(targetX, targetY);
        }
        
        // Use target entity position if no explicit position
        if (posTarget == null && unitTarget != null) {
            posTarget = new Vec2(unitTarget.x, unitTarget.y);
        }
        if (posTarget == null && buildTarget != null) {
            posTarget = new Vec2(buildTarget.x, buildTarget.y);
        }
        
        // Send command to move/attack position
        Call.commandUnits(Vars.player, unitIds, 
            buildTarget, 
            unitTarget, 
            posTarget, 
            false,  // don't queue
            true    // final batch
        );
        
        // Set command mode
        UnitCommand cmd = parseCommand(commandStr);
        if (cmd != null) {
            Call.setUnitCommand(Vars.player, unitIds, cmd);
        }
        
        // Build response data
        JSONObject data = new JSONObject();
        data.put("units_commanded", unitIds.length);
        data.put("command", commandStr);
        
        JSONArray unitsJson = new JSONArray();
        int validCount = 0;
        for (int id : unitIds) {
            Unit u = Groups.unit.getByID(id);
            if (u != null) {
                JSONObject uJson = new JSONObject();
                uJson.put("id", id);
                uJson.put("type", u.type.name);
                uJson.put("x", Math.round(u.x * 100f) / 100f);
                uJson.put("y", Math.round(u.y * 100f) / 100f);
                unitsJson.put(uJson);
                validCount++;
            }
        }
        data.put("units", unitsJson);
        data.put("valid_units", validCount);
        
        if (posTarget != null) {
            data.put("target_x", Math.round(posTarget.x * 100f) / 100f);
            data.put("target_y", Math.round(posTarget.y * 100f) / 100f);
        }
        
        return ActionResult.ok("Command '" + commandStr + "' sent to " + validCount + " units", data);
    }
    
    /**
     * Parses a command string to UnitCommand enum.
     */
    private UnitCommand parseCommand(String command) {
        switch (command) {
            case "move":
            case "rally":
                return UnitCommand.moveCommand;
            case "attack":
            case "engage":
                return UnitCommand.attackCommand;
            case "mine":
                return UnitCommand.mineCommand;
            case "repair":
            case "heal":
                return UnitCommand.repairCommand;
            case "rebuild":
                return UnitCommand.rebuildCommand;
            case "assist":
            case "help":
                return UnitCommand.assistCommand;
            default:
                // Try to find by exact name
                for (UnitCommand cmd : mindustry.Vars.content.unitCommands()) {
                    if (cmd.name().equalsIgnoreCase(command) || 
                        cmd.name().replace("Command", "").equalsIgnoreCase(command)) {
                        return cmd;
                    }
                }
                return UnitCommand.moveCommand; // default
        }
    }
}
