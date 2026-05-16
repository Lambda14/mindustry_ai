package aiagentmod.observation;

import mindustry.Vars;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.modules.PowerModule;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Scans team resources including items in cores, power, and production stats.
 */
public class ResourceScanner {
    
    /**
     * Scans resources for a team.
     * 
     * @param teamName Team name (null for player's team)
     */
    public JSONObject scanResources(String teamName) {
        JSONObject result = new JSONObject();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            return result;
        }
        
        // Determine team
        Team team;
        if (teamName != null) {
            team = findTeamByName(teamName);
            if (team == null) {
                result.put("error", "team_not_found");
                result.put("requested_team", teamName);
                return result;
            }
        } else {
            if (Vars.player == null) {
                result.put("error", "no_player_and_no_team_specified");
                return result;
            }
            team = Vars.player.team();
        }
        
        result.put("team", team.name);
        result.put("team_id", team.id);
        
        // Core items
        JSONObject items = new JSONObject();
        if (team.core() != null && team.core().items != null) {
            for (Item item : Vars.content.items()) {
                int amount = team.core().items.get(item);
                if (amount > 0) {
                    items.put(item.name, amount);
                }
            }
        }
        result.put("items", items);
        
        // Core info
        if (team.core() != null) {
            JSONObject core = new JSONObject();
            core.put("x", Math.round(team.core().x * 100f) / 100f);
            core.put("y", Math.round(team.core().y * 100f) / 100f);
            core.put("health", Math.round(team.core().health * 100f) / 100f);
            core.put("max_health", Math.round(team.core().maxHealth * 100f) / 100f);
            core.put("storage_capacity", team.core().block.itemCapacity);
            result.put("core", core);
        }
        
        // Team stats
        JSONObject stats = new JSONObject();
        stats.put("unit_count", team.data().unitCount);
        stats.put("building_count", team.data().buildings.size());
        result.put("stats", stats);
        
        // Game rules
        JSONObject rules = new JSONObject();
        rules.put("wave", Vars.state.wave);
        rules.put("wave_spacing", Math.round(Vars.state.rules.waveSpacing * 100f) / 100f);
        rules.put("wave_timer", Vars.state.rules.waveTimer);
        rules.put("enemies", Vars.state.rules.waveTeam.data().unitCount);
        rules.put("unit_cap", Vars.state.rules.unitCap);
        rules.put("build_speed_multiplier", Math.round(Vars.state.rules.buildSpeedMultiplier * 100f) / 100f);
        rules.put("unit_build_speed_multiplier", Math.round(Vars.state.rules.unitBuildSpeedMultiplier * 100f) / 100f);
        rules.put("unit_damage_multiplier", Math.round(Vars.state.rules.unitDamageMultiplier * 100f) / 100f);
        rules.put("unit_cost_multiplier", Math.round(Vars.state.rules.unitCostMultiplier * 100f) / 100f);
        result.put("rules", rules);
        
        // Available content
        JSONObject available = new JSONObject();
        JSONArray itemTypes = new JSONArray();
        for (Item item : Vars.content.items()) {
            JSONObject itemJson = new JSONObject();
            itemJson.put("name", item.name);
            itemJson.put("localized", item.localizedName);
            itemJson.put("color", item.color.toString());
            itemJson.put("hardness", item.hardness);
            itemJson.put("cost", Math.round(item.cost * 100f) / 100f);
            itemTypes.put(itemJson);
        }
        available.put("items", itemTypes);
        
        JSONArray liquidTypes = new JSONArray();
        for (Liquid liquid : Vars.content.liquids()) {
            JSONObject liquidJson = new JSONObject();
            liquidJson.put("name", liquid.name);
            liquidJson.put("localized", liquid.localizedName);
            liquidJson.put("color", liquid.color.toString());
            liquidTypes.put(liquidJson);
        }
        available.put("liquids", liquidTypes);
        
        result.put("available_content", available);
        
        return result;
    }
    
    /**
     * Scans resources for all active teams.
     */
    public JSONObject scanAllTeams() {
        JSONObject result = new JSONObject();
        JSONArray teams = new JSONArray();
        
        if (Vars.state.isMenu()) {
            result.put("error", "not_in_game");
            return result;
        }
        
        // Scan all teams that have cores
        Groups.build.each(b -> {
            if (b.block.name.contains("core")) {
                JSONObject teamResources = scanResources(b.team.name);
                if (!teamResources.has("error")) {
                    teams.put(teamResources);
                }
            }
        });
        
        result.put("teams", teams);
        result.put("count", teams.length());
        return result;
    }
    
    /**
     * Gets all item types available in the game.
     */
    public JSONObject scanItemTypes() {
        JSONObject result = new JSONObject();
        JSONArray items = new JSONArray();
        
        for (Item item : Vars.content.items()) {
            JSONObject itemJson = new JSONObject();
            itemJson.put("name", item.name);
            itemJson.put("localized", item.localizedName);
            itemJson.put("color", item.color.toString());
            itemJson.put("hardness", item.hardness);
            itemJson.put("cost", Math.round(item.cost * 100f) / 100f);
            itemJson.put("flammability", Math.round(item.flammability * 100f) / 100f);
            itemJson.put("radioactivity", Math.round(item.radioactivity * 100f) / 100f);
            itemJson.put("explosiveness", Math.round(item.explosiveness * 100f) / 100f);
            itemJson.put("charge", Math.round(item.charge * 100f) / 100f);
            items.put(itemJson);
        }
        
        result.put("items", items);
        result.put("count", items.length());
        return result;
    }
    
    /**
     * Finds a team by name.
     */
    private Team findTeamByName(String name) {
        for (Team team : Team.all) {
            if (team != null && team.name.equals(name)) {
                return team;
            }
        }
        // Try to find by ID
        try {
            int id = Integer.parseInt(name);
            if (id >= 0 && id < Team.all.length && Team.all[id] != null) {
                return Team.all[id];
            }
        } catch (NumberFormatException e) {
            // Not a number, continue
        }
        return null;
    }
}
