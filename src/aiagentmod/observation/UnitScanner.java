package aiagentmod.observation;

import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.gen.Groups;
import mindustry.ai.types.CommandAI;
import arc.util.Log;
import aiagentmod.api.SimpleJson;

public class UnitScanner {
    
    private static final int MAX_UNITS = 500;
    
    public String scanUnits(String teamName, float x, float y, float radius) {
        try {
            if (Vars.state == null) return "{\"units\":[],\"count\":0,\"error\":\"state_null\"}";
            if (Vars.state.isMenu()) return "{\"units\":[],\"count\":0,\"error\":\"in_menu\"}";
            if (Vars.world == null) return "{\"units\":[],\"count\":0,\"error\":\"world_null\"}";
            
            StringBuilder units = new StringBuilder("[");
            final int[] count = {0};
            final boolean[] first = {true};
            float radiusSq = radius * radius;
            
            Groups.unit.each(u -> {
                if (count[0] >= MAX_UNITS) return;
                if (u == null) return;
                try {
                    if (teamName != null && !u.team.name.equals(teamName)) return;
                    if (radius > 0) {
                        float dx = u.x - x, dy = u.y - y;
                        if (dx * dx + dy * dy > radiusSq) return;
                    }
                    if (!first[0]) units.append(",");
                    first[0] = false;
                    units.append(serializeUnit(u));
                    count[0]++;
                } catch (Exception ignored) {}
            });
            
            return "{\"units\":" + units.append("]").toString()
                + ",\"count\":" + count[0] + "}";
        } catch (Exception e) {
            Log.err("[AIAgentMod] UnitScanner: @", e);
            return "{\"units\":[],\"count\":0,\"error\":\"" + e.getClass().getSimpleName() + "\"}";
        }
    }
    
    private String serializeUnit(Unit u) {
        try {
            SimpleJson j = SimpleJson.object()
                .put("id", u.id)
                .put("type", u.type.name)
                .put("x", Math.round(u.x * 100f) / 100f)
                .put("y", Math.round(u.y * 100f) / 100f)
                .put("health", Math.round(u.health * 100f) / 100f)
                .put("team", u.team.name)
                .put("is_player", u.isPlayer())
                .put("is_shooting", u.isShooting())
                .put("is_mining", u.mining());
            
            if (u.controller() instanceof CommandAI) {
                CommandAI ai = (CommandAI) u.controller();
                j.put("has_command_ai", "true");
                try {
                    if (ai.currentCommand() != null) j.put("command", ai.name());
                } catch (Exception ignored) {}
            } else {
                j.put("has_command_ai", "false");
            }
            
            if (u.stack != null && u.stack.item != null) {
                j.put("item", u.stack.item.name).put("item_amount", u.stack.amount);
            }
            
            return j.toString();
        } catch (Exception e) {
            return "{\"id\":" + u.id + ",\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
