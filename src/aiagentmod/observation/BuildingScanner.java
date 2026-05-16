package aiagentmod.observation;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import mindustry.world.modules.PowerModule;
import mindustry.type.Item;
import mindustry.type.Liquid;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Scans buildings on the map with filtering by team, position, and radius.
 */
public class BuildingScanner {
    
    /** Maximum buildings returned in a single scan */
    private static final int MAX_BUILDINGS = 500;
    
    /**
     * Scans buildings with optional filtering.
     */
    public JSONObject scanBuildings(String teamName, float x, float y, float radius) {
        JSONObject result = new JSONObject();
        JSONArray buildings = new JSONArray();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            result.put("buildings", buildings);
            result.put("count", 0);
            return result;
        }
        
        final int[] count = {0};
        float radiusSq = radius * radius;
        
        Groups.build.each(b -> {
            if (count[0] >= MAX_BUILDINGS) return;
            
            // Team filter
            if (teamName != null && !b.team.name.equals(teamName)) return;
            
            // Radius filter
            if (radius > 0) {
                float dx = b.x - x;
                float dy = b.y - y;
                if (dx * dx + dy * dy > radiusSq) return;
            }
            
            buildings.put(serializeBuilding(b));
            count[0]++;
        });
        
        result.put("buildings", buildings);
        result.put("count", count[0]);
        result.put("total_in_world", Groups.build.size());
        result.put("limit", MAX_BUILDINGS);
        
        return result;
    }
    
    /**
     * Scans all buildings.
     */
    public JSONObject scanAllBuildings() {
        return scanBuildings(null, 0, 0, 0);
    }
    
    /**
     * Scans buildings by team.
     */
    public JSONObject scanTeamBuildings(String teamName) {
        return scanBuildings(teamName, 0, 0, 0);
    }
    
    /**
     * Scans nearby buildings.
     */
    public JSONObject scanNearbyBuildings(float x, float y, float radius) {
        return scanBuildings(null, x, y, radius);
    }
    
    /**
     * Gets a single building by ID.
     */
    public JSONObject scanBuilding(int buildingId) {
        Building build = Groups.build.getByID(buildingId);
        if (build == null) {
            return new JSONObject()
                .put("error", "building_not_found")
                .put("id", buildingId);
        }
        return serializeBuilding(build);
    }
    
    /**
     * Scans all core buildings.
     */
    public JSONObject scanCores() {
        JSONObject result = new JSONObject();
        JSONArray cores = new JSONArray();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            return result;
        }
        
        Groups.build.each(b -> {
            if (b.block.name.contains("core")) {
                JSONObject core = serializeBuilding(b);
                cores.put(core);
            }
        });
        
        result.put("cores", cores);
        result.put("count", cores.length());
        return result;
    }
    
    /**
     * Serializes a building to JSON.
     */
    public JSONObject serializeBuilding(Building b) {
        JSONObject build = new JSONObject();
        
        build.put("id", b.id);
        build.put("type", b.block.name);
        build.put("type_localized", b.block.localizedName);
        build.put("x", Math.round(b.x * 100f) / 100f);
        build.put("y", Math.round(b.y * 100f) / 100f);
        build.put("tile_x", b.tileX());
        build.put("tile_y", b.tileY());
        build.put("health", Math.round(b.health * 100f) / 100f);
        build.put("max_health", Math.round(b.maxHealth * 100f) / 100f);
        build.put("team", b.team.name);
        build.put("rotation", b.rotation);
        build.put("size", b.block.size);
        build.put("is_valid", b.isValid());
        build.put("is_enabled", b.enabled);
        build.put("armor", Math.round(b.block.armor * 100f) / 100f);
        build.put("can_command", b.isCommandable());
        build.put("priority", b.priority);
        
        // Power
        if (b.power != null) {
            JSONObject power = new JSONObject();
            power.put("status", Math.round(b.power.status * 1000f) / 1000f);
            power.put("graph_count", b.power.graph.getAll().size());
            build.put("power", power);
        }
        
        // Items
        if (b.items != null && b.items.total() > 0) {
            JSONObject items = new JSONObject();
            for (Item item : Vars.content.items()) {
                int amount = b.items.get(item);
                if (amount > 0) {
                    items.put(item.name, amount);
                }
            }
            if (items.length() > 0) {
                build.put("items", items);
            }
            build.put("item_total", b.items.total());
            build.put("item_capacity", b.block.itemCapacity);
        }
        
        // Liquids
        if (b.liquids != null) {
            JSONObject liquids = new JSONObject();
            float totalLiquids = 0f;
            for (Liquid liquid : Vars.content.liquids()) {
                float amount = b.liquids.get(liquid);
                if (amount > 0.01f) {
                    liquids.put(liquid.name, Math.round(amount * 100f) / 100f);
                    totalLiquids += amount;
                }
            }
            if (liquids.length() > 0) {
                build.put("liquids", liquids);
                build.put("liquid_total", Math.round(totalLiquids * 100f) / 100f);
                build.put("liquid_capacity", Math.round(b.block.liquidCapacity * 100f) / 100f);
            }
        }
        
        // Efficiency and progress
        build.put("efficiency", Math.round(b.efficiency * 100f) / 100f);
        build.put("progress", Math.round(b.progress * 100f) / 100f);
        build.put("warmup", Math.round(b.warmup() * 100f) / 100f);
        
        // Config
        if (b.config() != null) {
            build.put("config", b.config().toString());
        }
        
        // Category
        build.put("category", b.block.category.name());
        
        // Destroy bullet
        build.put("has_destroy_bullet", b.block.destroyBullet != null);
        
        // Destroy effects
        build.put("instant_deconstruct", b.block.instantDeconstruct);
        
        return build;
    }
}
