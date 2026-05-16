package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.gen.Groups;
import mindustry.gen.Call;
import mindustry.gen.Building;
import mindustry.type.UnitCommand;
import arc.math.geom.Vec2;
import aiagentmod.api.SimpleMap;
import aiagentmod.api.SimpleJson;

public class UnitCommandAction {
    
    public ActionResult execute(SimpleMap params) {
        if (Vars.state.isMenu()) return ActionResult.error("Not in game");
        if (Vars.player == null) return ActionResult.error("Player not available");
        
        String unitIdsStr = params.optString("unit_ids", "");
        if (unitIdsStr.isEmpty()) return ActionResult.error("No unit_ids specified");
        
        // Parse comma-separated unit IDs
        String[] parts = unitIdsStr.replaceAll("[\\[\\]]", "").split(",");
        int[] unitIds = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { unitIds[i] = Integer.parseInt(parts[i].trim()); } catch (NumberFormatException e) { unitIds[i] = -1; }
        }
        
        String commandStr = params.optString("command", "move").toLowerCase();
        float targetX = params.optFloat("x", -1);
        float targetY = params.optFloat("y", -1);
        int targetId = params.optInt("target_id", -1);
        
        Building buildTarget = null;
        Unit unitTarget = null;
        Vec2 posTarget = null;
        
        if (targetId >= 0) {
            Unit target = Groups.unit.getByID(targetId);
            if (target != null) unitTarget = target;
            else { Building b = Groups.build.getByID(targetId); if (b != null) buildTarget = b; }
        }
        
        if (targetX >= 0 && targetY >= 0) posTarget = new Vec2(targetX, targetY);
        if (posTarget == null && unitTarget != null) posTarget = new Vec2(unitTarget.x, unitTarget.y);
        if (posTarget == null && buildTarget != null) posTarget = new Vec2(buildTarget.x, buildTarget.y);
        
        Call.commandUnits(Vars.player, unitIds, buildTarget, unitTarget, posTarget, false, true);
        
        UnitCommand cmd = parseCommand(commandStr);
        if (cmd != null) Call.setUnitCommand(Vars.player, unitIds, cmd);
        
        String data = SimpleJson.obj("units_commanded", SimpleJson.num(unitIds.length),
            "command", SimpleJson.str(commandStr));
        
        return ActionResult.ok("Command '" + commandStr + "' sent to " + unitIds.length + " units", data);
    }
    
    private UnitCommand parseCommand(String command) {
        switch (command) {
            case "move": case "rally": return UnitCommand.moveCommand;
            case "attack": case "engage": return UnitCommand.attackCommand;
            case "mine": return UnitCommand.mineCommand;
            case "repair": case "heal": return UnitCommand.repairCommand;
            case "rebuild": return UnitCommand.rebuildCommand;
            case "assist": case "help": return UnitCommand.assistCommand;
            default:
                for (UnitCommand cmd : Vars.content.unitCommands()) {
                    if (cmd.name().equalsIgnoreCase(command)) return cmd;
                }
                return UnitCommand.moveCommand;
        }
    }
}
