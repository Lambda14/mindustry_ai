package aiagentmod.observation;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.type.Item;
import mindustry.type.Liquid;
import arc.util.Log;
import aiagentmod.api.SimpleJson;

public class BuildingScanner {
    
    private static final int MAX_BUILDINGS = 500;
    
    public String scanBuildings(String teamName, float x, float y, float radius) {
        try {
            if (Vars.state == null) return "{\"buildings\":[],\"count\":0,\"error\":\"state_null\"}";
            if (Vars.state.isMenu()) return "{\"buildings\":[],\"count\":0,\"error\":\"in_menu\"}";
            if (Vars.world == null) return "{\"buildings\":[],\"count\":0,\"error\":\"world_null\"}";
            
            StringBuilder buildings = new StringBuilder("[");
            final int[] count = {0};
            final boolean[] first = {true};
            float radiusSq = radius * radius;
            
            Groups.build.each(b -> {
                if (count[0] >= MAX_BUILDINGS) return;
                if (b == null) return;
                try {
                    if (teamName != null && !b.team.name.equals(teamName)) return;
                    if (radius > 0) {
                        float dx = b.x - x, dy = b.y - y;
                        if (dx * dx + dy * dy > radiusSq) return;
                    }
                    if (!first[0]) buildings.append(",");
                    first[0] = false;
                    buildings.append(serializeBuilding(b));
                    count[0]++;
                } catch (Exception ignored) {}
            });
            
            return "{\"buildings\":" + buildings.append("]").toString()
                + ",\"count\":" + count[0] + "}";
        } catch (Exception e) {
            Log.err("[AIAgentMod] BuildingScanner: @", e);
            return "{\"buildings\":[],\"count\":0,\"error\":\"" + e.getClass().getSimpleName() + "\"}";
        }
    }
    
    private String serializeBuilding(Building b) {
        try {
            SimpleJson j = SimpleJson.object()
                .put("id", b.id)
                .put("type", b.block.name)
                .put("x", Math.round(b.x * 100f) / 100f)
                .put("y", Math.round(b.y * 100f) / 100f)
                .put("health", Math.round(b.health * 100f) / 100f)
                .put("team", b.team.name)
                .put("rotation", b.rotation)
                .put("enabled", b.enabled)
                .put("is_valid", b.isValid());
            
            if (b.power != null) {
                j.put("power_status", Math.round(b.power.status * 1000f) / 1000f);
            }
            
            if (b.items != null && b.items.total() > 0) {
                j.put("item_total", b.items.total()).put("item_capacity", b.block.itemCapacity);
            }
            
            return j.toString();
        } catch (Exception e) {
            return "{\"id\":" + b.id + ",\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
