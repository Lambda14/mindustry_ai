package aiagentmod.observation;

import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import org.json.JSONObject;

/**
 * Scans the local player state including position, health, unit, and inventory.
 */
public class PlayerScanner {
    
    /**
     * Returns comprehensive player information.
     */
    public JSONObject scanPlayer() {
        JSONObject result = new JSONObject();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            result.put("in_game", false);
            return result;
        }
        
        result.put("in_game", true);
        result.put("game_state", getGameState());
        
        Player player = Vars.player;
        if (player == null) {
            result.put("error", "no_player");
            return result;
        }
        
        // Basic player info
        result.put("name", player.name);
        result.put("id", player.id);
        result.put("x", Math.round(player.x * 100f) / 100f);
        result.put("y", Math.round(player.y * 100f) / 100f);
        result.put("is_dead", player.dead());
        result.put("team", player.team().name);
        result.put("color", player.color.toString());
        result.put("admin", player.admin);
        
        // Unit info
        Unit unit = player.unit();
        if (unit != null && !unit.dead()) {
            serializeUnit(result, unit);
        } else {
            result.put("has_unit", false);
        }
        
        // Builder info
        result.put("is_builder", player.isBuilder());
        result.put("building", player.unit() != null && player.unit().activelyBuilding());
        
        return result;
    }
    
    /**
     * Returns minimal player info for frequent updates.
     */
    public JSONObject scanPlayerMinimal() {
        JSONObject result = new JSONObject();
        
        if (Vars.state.isMenu() || Vars.player == null) {
            result.put("available", false);
            return result;
        }
        
        Player player = Vars.player;
        result.put("available", true);
        result.put("x", Math.round(player.x * 100f) / 100f);
        result.put("y", Math.round(player.y * 100f) / 100f);
        result.put("is_dead", player.dead());
        result.put("team", player.team().name);
        
        Unit unit = player.unit();
        if (unit != null && !unit.dead()) {
            result.put("health", Math.round(unit.health * 100f) / 100f);
            result.put("max_health", Math.round(unit.maxHealth * 100f) / 100f);
            result.put("shield", Math.round(unit.shield * 100f) / 100f);
            result.put("unit_type", unit.type.name);
            result.put("is_mining", unit.mining());
            result.put("is_shooting", unit.isShooting());
            result.put("velocity_x", Math.round(unit.vel.x * 100f) / 100f);
            result.put("velocity_y", Math.round(unit.vel.y * 100f) / 100f);
        }
        
        return result;
    }
    
    private void serializeUnit(JSONObject result, Unit unit) {
        result.put("has_unit", true);
        result.put("unit_id", unit.id);
        result.put("unit_type", unit.type.name);
        result.put("unit_localized", unit.type.localizedName);
        result.put("health", Math.round(unit.health * 100f) / 100f);
        result.put("max_health", Math.round(unit.maxHealth * 100f) / 100f);
        result.put("shield", Math.round(unit.shield * 100f) / 100f);
        result.put("rotation", Math.round(unit.rotation * 100f) / 100f);
        result.put("elevation", Math.round(unit.elevation * 100f) / 100f);
        
        // Movement
        result.put("is_flying", unit.isFlying());
        result.put("is_grounded", unit.isGrounded());
        result.put("is_mining", unit.mining());
        result.put("is_shooting", unit.isShooting());
        result.put("is_boosting", unit.boosting);
        result.put("velocity_x", Math.round(unit.vel.x * 100f) / 100f);
        result.put("velocity_y", Math.round(unit.vel.y * 100f) / 100f);
        
        // Mining
        if (unit.mineTile != null) {
            JSONObject mineTile = new JSONObject();
            mineTile.put("x", unit.mineTile.x);
            mineTile.put("y", unit.mineTile.y);
            if (unit.mineTile.overlay() != null && unit.mineTile.overlay().itemDrop != null) {
                mineTile.put("ore", unit.mineTile.overlay().itemDrop.name);
            }
            result.put("mine_tile", mineTile);
        }
        
        // Items/inventory
        if (unit.stack != null && unit.stack.item != null) {
            result.put("item", unit.stack.item.name);
            result.put("item_amount", unit.stack.amount);
            result.put("item_capacity", unit.type.itemCapacity);
        } else {
            result.put("item", JSONObject.NULL);
            result.put("item_amount", 0);
        }
        
        // Unit stats
        result.put("build_range", Math.round(unit.type.buildRange * 100f) / 100f);
        result.put("mine_range", Math.round(unit.type.mineRange * 100f) / 100f);
        result.put("hit_size", Math.round(unit.type.hitSize * 100f) / 100f);
        result.put("speed", Math.round(unit.type.speed * 100f) / 100f);
        result.put("rotate_speed", Math.round(unit.type.rotateSpeed * 100f) / 100f);
        result.put("armor", Math.round(unit.type.armor * 100f) / 100f);
        result.put("flying", unit.type.flying);
        result.put("can_boost", unit.type.canBoost);
        
        // Aiming
        result.put("aim_x", Math.round(unit.aimX * 100f) / 100f);
        result.put("aim_y", Math.round(unit.aimY * 100f) / 100f);
    }
    
    private String getGameState() {
        if (Vars.state.isMenu()) return "menu";
        if (Vars.state.isPaused()) return "paused";
        if (Vars.state.isGameOver()) return "game_over";
        return "playing";
    }
}
