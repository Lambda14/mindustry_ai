package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import org.json.JSONObject;

/**
 * Action to configure a building (e.g., set sorter item, conveyor direction, etc.).
 * 
 * Parameters:
 * - building_id: ID of the building to configure
 * - config: configuration value (string for item/liquid names, number for colors, etc.)
 */
public class BuildingConfigAction {
    
    /**
     * Configures a building with the specified config value.
     */
    public ActionResult execute(JSONObject params) {
        // Parse and validate parameters FIRST
        int buildingId = params.optInt("building_id", -1);
        if (buildingId < 0) {
            return ActionResult.error("Invalid building_id: " + buildingId);
        }
        
        // Process config
        Object config = params.opt("config");
        if (config == null || config == JSONObject.NULL) {
            return ActionResult.error("No config value provided");
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
        
        Object processedConfig = processConfig(building, config);
        
        try {
            Call.tileConfig(Vars.player, building, processedConfig);
        } catch (Exception e) {
            return ActionResult.error("Configuration failed: " + e.getMessage());
        }
        
        JSONObject data = new JSONObject();
        data.put("building_id", buildingId);
        data.put("building_type", building.block.name);
        data.put("building_localized", building.block.localizedName);
        data.put("config_raw", config.toString());
        data.put("config_processed", processedConfig != null ? processedConfig.toString() : "null");
        data.put("previous_config", building.config() != null ? building.config().toString() : "null");
        
        return ActionResult.ok("Configuration applied to " + building.block.localizedName, data);
    }
    
    /**
     * Processes the config value to convert string names to proper Mindustry content types.
     */
    private Object processConfig(Building building, Object config) {
        if (!(config instanceof String)) {
            return config;
        }
        
        String str = ((String) config).toLowerCase().trim();
        
        // Try to find as item
        Item item = Vars.content.item(str);
        if (item != null) return item;
        
        // Try to find as liquid
        Liquid liquid = Vars.content.liquid(str);
        if (liquid != null) return liquid;
        
        // Try to find as unit type
        UnitType unitType = Vars.content.unit(str);
        if (unitType != null) return unitType;
        
        // Try to find as block
        mindustry.world.Block block = Vars.content.block(str);
        if (block != null) return block;
        
        // Handle boolean strings
        if (str.equals("true") || str.equals("on")) return Boolean.TRUE;
        if (str.equals("false") || str.equals("off")) return Boolean.FALSE;
        
        // Handle numeric strings
        try {
            if (str.contains(".")) {
                return Float.parseFloat(str);
            } else {
                return Integer.parseInt(str);
            }
        } catch (NumberFormatException e) {
            // Not a number, return as string
        }
        
        // Return as-is (string)
        return str;
    }
}
