package aiagentmod.observation;

import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import arc.util.Log;
import aiagentmod.api.SimpleJson;

public class PlayerScanner {
    
    public String scanPlayer() {
        try {
            if (Vars.state == null) return "{\"error\":\"state_null\"}";
            if (Vars.state.isMenu()) return "{\"in_game\":false}";
            if (Vars.player == null) return "{\"in_game\":true,\"error\":\"no_player\"}";
            
            Player player = Vars.player;
            SimpleJson r = SimpleJson.object()
                .put("in_game", "true")
                .put("name", str(player.name))
                .put("x", Math.round(player.x * 100f) / 100f)
                .put("y", Math.round(player.y * 100f) / 100f)
                .put("is_dead", player.dead())
                .put("is_builder", player.isBuilder());
            
            try {
                r.put("team", player.team().name);
            } catch (Exception ignored) { r.put("team", "unknown"); }
            
            Unit unit = player.unit();
            if (unit != null && !unit.dead()) {
                serializeUnit(r, unit);
            } else {
                r.put("has_unit", "false");
            }
            
            return r.toString();
        } catch (Exception e) {
            Log.err("[AIAgentMod] PlayerScanner: @", e);
            return "{\"error\":\"" + e.getClass().getSimpleName() + "\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
    
    private void serializeUnit(SimpleJson r, Unit unit) {
        try {
            r.put("has_unit", "true")
             .put("unit_type", str(unit.type.name))
             .put("health", Math.round(unit.health * 100f) / 100f)
             .put("max_health", Math.round(unit.maxHealth * 100f) / 100f)
             .put("shield", Math.round(unit.shield * 100f) / 100f)
             .put("rotation", Math.round(unit.rotation * 100f) / 100f)
             .put("is_flying", unit.isFlying())
             .put("is_mining", unit.mining())
             .put("is_shooting", unit.isShooting())
             .put("vel_x", Math.round(unit.vel.x * 100f) / 100f)
             .put("vel_y", Math.round(unit.vel.y * 100f) / 100f)
             .put("aim_x", Math.round(unit.aimX * 100f) / 100f)
             .put("aim_y", Math.round(unit.aimY * 100f) / 100f)
             .put("build_range", Math.round(unit.type.buildRange * 100f) / 100f)
             .put("mine_range", Math.round(unit.type.mineRange * 100f) / 100f);
            
            if (unit.stack != null && unit.stack.item != null) {
                r.put("item", str(unit.stack.item.name)).put("item_amount", unit.stack.amount);
            } else {
                r.put("item", "none").put("item_amount", 0);
            }
            
            if (unit.mineTile != null) {
                r.put("mine_tile_x", unit.mineTile.x).put("mine_tile_y", unit.mineTile.y);
            }
        } catch (Exception e) {
            r.put("unit_error", e.getMessage());
        }
    }
    
    private String str(String s) { return s != null ? s : "unknown"; }
}
