package aiagentmod.observation;

import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.gen.Groups;
import mindustry.gen.Building;
import mindustry.ai.types.CommandAI;
import mindustry.ai.types.LogicAI;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Scans units on the map with filtering by team, position, and radius.
 */
public class UnitScanner {
    
    /** Maximum units returned in a single scan */
    private static final int MAX_UNITS = 500;
    
    /**
     * Scans units with optional filtering.
     * 
     * @param teamName Filter by team name (null for all teams)
     * @param x Center X for radius filter (0 if no radius filter)
     * @param y Center Y for radius filter (0 if no radius filter)
     * @param radius Radius for filtering (0 for no radius filter)
     */
    public JSONObject scanUnits(String teamName, float x, float y, float radius) {
        JSONObject result = new JSONObject();
        JSONArray units = new JSONArray();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            result.put("units", units);
            result.put("count", 0);
            return result;
        }
        
        final int[] count = {0};
        float radiusSq = radius * radius;
        
        Groups.unit.each(u -> {
            if (count[0] >= MAX_UNITS) return;
            
            // Team filter
            if (teamName != null && !u.team.name.equals(teamName)) return;
            
            // Radius filter
            if (radius > 0) {
                float dx = u.x - x;
                float dy = u.y - y;
                if (dx * dx + dy * dy > radiusSq) return;
            }
            
            units.put(serializeUnit(u));
            count[0]++;
        });
        
        result.put("units", units);
        result.put("count", count[0]);
        result.put("total_in_world", Groups.unit.size());
        result.put("limit", MAX_UNITS);
        
        return result;
    }
    
    /**
     * Scans all units on the map.
     */
    public JSONObject scanAllUnits() {
        return scanUnits(null, 0, 0, 0);
    }
    
    /**
     * Scans units belonging to a specific team.
     */
    public JSONObject scanTeamUnits(String teamName) {
        return scanUnits(teamName, 0, 0, 0);
    }
    
    /**
     * Scans units near a position.
     */
    public JSONObject scanNearbyUnits(float x, float y, float radius) {
        return scanUnits(null, x, y, radius);
    }
    
    /**
     * Gets a single unit by ID.
     */
    public JSONObject scanUnit(int unitId) {
        Unit unit = Groups.unit.getByID(unitId);
        if (unit == null) {
            return new JSONObject()
                .put("error", "unit_not_found")
                .put("id", unitId);
        }
        return serializeUnit(unit);
    }
    
    /**
     * Counts units per team.
     */
    public JSONObject countUnitsByTeam() {
        JSONObject result = new JSONObject();
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        
        Groups.unit.each(u -> {
            String teamName = u.team.name;
            counts.put(teamName, counts.getOrDefault(teamName, 0) + 1);
        });
        
        for (var entry : counts.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        
        result.put("total", Groups.unit.size());
        return result;
    }
    
    /**
     * Serializes a unit to JSON.
     */
    public JSONObject serializeUnit(Unit u) {
        JSONObject unit = new JSONObject();
        
        unit.put("id", u.id);
        unit.put("type", u.type.name);
        unit.put("type_localized", u.type.localizedName);
        unit.put("x", Math.round(u.x * 100f) / 100f);
        unit.put("y", Math.round(u.y * 100f) / 100f);
        unit.put("health", Math.round(u.health * 100f) / 100f);
        unit.put("max_health", Math.round(u.maxHealth * 100f) / 100f);
        unit.put("shield", Math.round(u.shield * 100f) / 100f);
        unit.put("team", u.team.name);
        unit.put("rotation", Math.round(u.rotation * 100f) / 100f);
        unit.put("elevation", Math.round(u.elevation * 100f) / 100f);
        
        // State flags
        unit.put("is_player", u.isPlayer());
        unit.put("is_ai", u.isAI());
        unit.put("is_flying", u.isFlying());
        unit.put("is_grounded", u.isGrounded());
        unit.put("is_boss", u.isBoss());
        unit.put("is_hovering", u.isHovering());
        unit.put("dead", u.dead());
        unit.put("is_shooting", u.isShooting());
        unit.put("is_mining", u.mining());
        unit.put("is_boosting", u.boosting);
        
        // Movement
        unit.put("vel_x", Math.round(u.vel.x * 100f) / 100f);
        unit.put("vel_y", Math.round(u.vel.y * 100f) / 100f);
        
        // Command AI
        if (u.controller() instanceof CommandAI) { CommandAI ai = (CommandAI) u.controller();
            unit.put("has_command_ai", true);
            unit.put("command", ai.currentCommand() != null ? ai.currentCommand().name() : "none");
            unit.put("command_localized", ai.currentCommand() != null ? ai.currentCommand().localized : "none");
            
            if (ai.targetPos != null) {
                JSONObject target = new JSONObject();
                target.put("x", Math.round(ai.targetPos.x * 100f) / 100f);
                target.put("y", Math.round(ai.targetPos.y * 100f) / 100f);
                unit.put("target_pos", target);
            }
            
            if (ai.attackTarget != null) {
                JSONObject attackTarget = new JSONObject();
                attackTarget.put("x", Math.round(ai.attackTarget.getX() * 100f) / 100f);
                attackTarget.put("y", Math.round(ai.attackTarget.getY() * 100f) / 100f);
                unit.put("attack_target", attackTarget);
            }
        } else if (u.controller() != null && u.controller().getClass().getSimpleName().equals("LogicAI")) {
            unit.put("has_command_ai", false);
            unit.put("ai_type", "logic");
        } else {
            unit.put("has_command_ai", false);
            unit.put("ai_type", u.controller() != null ? u.controller().getClass().getSimpleName() : "none");
        }
        
        // Items
        if (u.stack != null && u.stack.item != null) {
            JSONObject item = new JSONObject();
            item.put("type", u.stack.item.name);
            item.put("amount", u.stack.amount);
            item.put("capacity", u.type.itemCapacity);
            unit.put("item", item);
        }
        
        // Payload
        unit.put("has_payload", u instanceof mindustry.gen.Payloadc && ((mindustry.gen.Payloadc) u).hasPayload());
        
        // Owner
        if (u.isPlayer() && u.getPlayer() != null) {
            unit.put("player_name", u.getPlayer().name);
        }
        
        // Spawned by core
        unit.put("spawned_by_core", u.spawnedByCore);
        
        // Stats
        unit.put("hit_size", Math.round(u.type.hitSize * 100f) / 100f);
        unit.put("speed", Math.round(u.type.speed * 100f) / 100f);
        unit.put("armor", Math.round(u.type.armor * 100f) / 100f);
        unit.put("build_speed", Math.round(u.type.buildSpeed * 100f) / 100f);
        unit.put("mine_speed", Math.round(u.type.mineSpeed * 100f) / 100f);
        
        // Aiming
        unit.put("aim_x", Math.round(u.aimX * 100f) / 100f);
        unit.put("aim_y", Math.round(u.aimY * 100f) / 100f);
        
        return unit;
    }
}
