package aiagentmod.observation;

import mindustry.Vars;
import mindustry.game.Team;
import mindustry.type.Item;
import arc.util.Log;
import aiagentmod.api.SimpleJson;

public class ResourceScanner {
    
    public String scanResources(String teamName) {
        try {
            if (Vars.state == null) return "{\"error\":\"state_null\"}";
            if (Vars.state.isMenu()) return "{\"error\":\"in_menu\"}";
            
            Team team;
            if (teamName != null) {
                team = findTeam(teamName);
                if (team == null) return "{\"error\":\"team_not_found\"}";
            } else {
                if (Vars.player == null) return "{\"error\":\"no_player\"}";
                team = Vars.player.team();
            }
            
            SimpleJson r = SimpleJson.object()
                .put("team", team.name)
                .put("team_id", team.id);
            
            // Core items
            StringBuilder items = new StringBuilder("{");
            boolean first = true;
            try {
                if (team.core() != null && team.core().items != null) {
                    for (Item item : Vars.content.items()) {
                        try {
                            int amt = team.core().items.get(item);
                            if (amt > 0) {
                                if (!first) items.append(",");
                                first = false;
                                items.append("\"").append(item.name).append("\":").append(amt);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}
            r.putRaw("items", items.append("}").toString());
            
            // Core info
            try {
                if (team.core() != null) {
                    r.put("core_health", Math.round(team.core().health * 100f) / 100f);
                }
            } catch (Exception ignored) {}
            
            // Stats
            try {
                r.put("unit_count", team.data().unitCount)
                 .put("building_count", team.data().buildings.size());
            } catch (Exception ignored) {}
            
            try { r.put("wave", Vars.state.wave); } catch (Exception ignored) {}
            
            return r.toString();
        } catch (Exception e) {
            Log.err("[AIAgentMod] ResourceScanner: @", e);
            return "{\"error\":\"" + e.getClass().getSimpleName() + "\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
    
    private Team findTeam(String name) {
        try {
            for (Team t : Team.all) {
                if (t != null && t.name.equals(name)) return t;
            }
            try {
                int id = Integer.parseInt(name);
                if (id >= 0 && id < Team.all.length && Team.all[id] != null) return Team.all[id];
            } catch (NumberFormatException ignored) {}
        } catch (Exception ignored) {}
        return null;
    }
}
