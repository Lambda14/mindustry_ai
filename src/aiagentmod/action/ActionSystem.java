package aiagentmod.action;

import org.json.JSONObject;

/**
 * Main coordinator for the action system.
 * Routes action requests to the appropriate action handler.
 * 
 * Supported action types:
 * - move: Move player/unit to position
 * - build: Place a block
 * - break: Remove a block
 * - mine: Mine resources at position
 * - shoot: Aim and shoot at target
 * - unit_command: Command multiple units
 * - configure: Configure a building
 * - rotate: Rotate a building
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
    
    /**
     * Executes an action by type with the given parameters.
     * 
     * @param actionType Type of action to execute
     * @param params JSON parameters for the action
     * @return Result of the action execution
     */
    public ActionResult execute(String actionType, JSONObject params) {
        if (actionType == null || actionType.isEmpty()) {
            return ActionResult.error("Action type not specified");
        }
        
        switch (actionType) {
            case "move":
                return move.execute(params);
                
            case "build":
                return build.execute(params);
                
            case "break":
                return build.executeBreak(params);
                
            case "mine":
                return mine.execute(params);
                
            case "shoot":
                return shoot.execute(params);
                
            case "unit_command":
                return unitCommand.execute(params);
                
            case "configure":
                return buildingConfig.execute(params);
                
            case "rotate":
                return rotate.execute(params);
                
            default:
                return ActionResult.error("Unknown action type: '" + actionType + "'. " +
                    "Supported types: move, build, break, mine, shoot, unit_command, configure, rotate");
        }
    }
    
    /**
     * Gets a list of all available action types with descriptions.
     * 
     * @return JSON object with action types and their descriptions
     */
    public JSONObject getAvailableActions() {
        JSONObject actions = new JSONObject();
        
        actions.put("move", "Move player to position {x, y}");
        actions.put("build", "Place block {x, y, block, rotation, config}");
        actions.put("break", "Remove block {x, y}");
        actions.put("mine", "Mine resources {x, y, stop}");
        actions.put("shoot", "Aim and shoot {x, y, shoot}");
        actions.put("unit_command", "Command units {unit_ids, command, x, y, target_id}");
        actions.put("configure", "Configure building {building_id, config}");
        actions.put("rotate", "Rotate building {building_id, direction}");
        
        return actions;
    }
}
