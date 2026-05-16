package aiagentmod.action;

import aiagentmod.api.SimpleJson;
import aiagentmod.api.SimpleMap;

/**
 * Main coordinator for the action system.
 * Routes action requests to the appropriate action handler.
 */
public class ActionSystem {
    
    public final MoveAction move;
    public final BuildAction build;
    public final MineAction mine;
    public final ShootAction shoot;
    public final UnitCommandAction unitCommand;
    public final BuildingConfigAction buildingConfig;
    public final RotateAction rotate;
    public final ActionQueue queue;
    
    public ActionSystem() {
        this.move = new MoveAction();
        this.build = new BuildAction();
        this.mine = new MineAction();
        this.shoot = new ShootAction();
        this.unitCommand = new UnitCommandAction();
        this.buildingConfig = new BuildingConfigAction();
        this.rotate = new RotateAction();
        this.queue = new ActionQueue();
    }
    
    public ActionResult execute(String actionType, SimpleMap params) {
        if (actionType == null || actionType.isEmpty()) {
            return ActionResult.error("Action type not specified");
        }
        
        switch (actionType) {
            case "move": return move.execute(params);
            case "build": return build.execute(params);
            case "break": return build.executeBreak(params);
            case "mine": return mine.execute(params);
            case "shoot": return shoot.execute(params);
            case "unit_command": return unitCommand.execute(params);
            case "configure": return buildingConfig.execute(params);
            case "rotate": return rotate.execute(params);
            default:
                return ActionResult.error("Unknown action type: '" + actionType + "'. " +
                    "Supported types: move, build, break, mine, shoot, unit_command, configure, rotate");
        }
    }
    
    public String getAvailableActions() {
        return "{"
            + "\"move\":\"Move player to position {x, y}\","
            + "\"build\":\"Place block {x, y, block, rotation, config}\","
            + "\"break\":\"Remove block {x, y}\","
            + "\"mine\":\"Mine resources {x, y, stop}\","
            + "\"shoot\":\"Aim and shoot {x, y, shoot}\","
            + "\"unit_command\":\"Command units {unit_ids, command, x, y, target_id}\","
            + "\"configure\":\"Configure building {building_id, config}\","
            + "\"rotate\":\"Rotate building {building_id, direction}\""
            + "}";
    }
}
