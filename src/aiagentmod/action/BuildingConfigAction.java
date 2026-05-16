package aiagentmod.action;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import aiagentmod.api.SimpleMap;
import aiagentmod.api.SimpleJson;

public class BuildingConfigAction {
    
    public ActionResult execute(SimpleMap params) {
        int buildingId = params.optInt("building_id", -1);
        if (buildingId < 0) return ActionResult.error("Invalid building_id");
        
        Object config = params.get("config");
        if (config == null) return ActionResult.error("No config value provided");
        
        if (Vars.state.isMenu()) return ActionResult.error("Not in game");
        
        Building building = Groups.build.getByID(buildingId);
        if (building == null) return ActionResult.error("Building not found: " + buildingId);
        if (!building.isValid()) return ActionResult.error("Building not valid");
        
        Object processedConfig = processConfig(building, config.toString());
        
        try { Call.tileConfig(Vars.player, building, processedConfig); }
        catch (Exception e) { return ActionResult.error("Config failed: " + e.getMessage()); }
        
        String data = SimpleJson.obj("building_id", SimpleJson.num(buildingId),
            "building_type", SimpleJson.str(building.block.name),
            "config", SimpleJson.str(config.toString()));
        
        return ActionResult.ok("Configuration applied", data);
    }
    
    private Object processConfig(Building building, String config) {
        String str = config.toLowerCase().trim();
        
        Item item = Vars.content.item(str);
        if (item != null) return item;
        
        Liquid liquid = Vars.content.liquid(str);
        if (liquid != null) return liquid;
        
        UnitType unitType = Vars.content.unit(str);
        if (unitType != null) return unitType;
        
        mindustry.world.Block block = Vars.content.block(str);
        if (block != null) return block;
        
        if (str.equals("true") || str.equals("on")) return Boolean.TRUE;
        if (str.equals("false") || str.equals("off")) return Boolean.FALSE;
        
        try {
            if (str.contains(".")) return Float.parseFloat(str);
            else return Integer.parseInt(str);
        } catch (NumberFormatException e) { return str; }
    }
}
