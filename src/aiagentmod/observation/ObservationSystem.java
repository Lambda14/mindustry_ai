package aiagentmod.observation;

import mindustry.Vars;
import mindustry.gen.Groups;
import arc.util.Log;

public class ObservationSystem {
    
    private final WorldScanner worldScanner;
    private final PlayerScanner playerScanner;
    private final UnitScanner unitScanner;
    private final BuildingScanner buildingScanner;
    private final ResourceScanner resourceScanner;
    
    public ObservationSystem() {
        this.worldScanner = new WorldScanner();
        this.playerScanner = new PlayerScanner();
        this.unitScanner = new UnitScanner();
        this.buildingScanner = new BuildingScanner();
        this.resourceScanner = new ResourceScanner();
    }
    
    public String scanFullState() {
        Log.info("[AIAgentMod] scanFullState called");
        try {
            StringBuilder r = new StringBuilder("{");
            r.append("\"status\":\"in_game\"");
            r.append(",\"in_game\":true");
            r.append(",\"timestamp\":").append(System.currentTimeMillis());
            
            // Wave
            try { r.append(",\"wave\":").append(Vars.state.wave); } catch (Exception e) { r.append(",\"wave\":0"); }
            
            // Map
            try { 
                String name = Vars.state.map != null ? Vars.state.map.name() : "unknown";
                r.append(",\"map_name\":\"").append(name).append("\"");
            } catch (Exception e) { r.append(",\"map_name\":\"unknown\""); }
            
            // Counts
            try { r.append(",\"units_total\":").append(Groups.unit.size()); } catch (Exception e) { r.append(",\"units_total\":0"); }
            try { r.append(",\"buildings_total\":").append(Groups.build.size()); } catch (Exception e) { r.append(",\"buildings_total\":0"); }
            try { r.append(",\"is_paused\":").append(Vars.state.isPaused()); } catch (Exception e) { r.append(",\"is_paused\":false"); }
            
            // Player
            r.append(",\"player\":");
            try { r.append(playerScanner.scanPlayer()); } catch (Exception e) { r.append("{\"error\":\"").append(e.getMessage()).append("\"}"); }
            
            // Units
            r.append(",\"nearby_units\":");
            try {
                if (Vars.player != null) r.append(unitScanner.scanUnits(null, Vars.player.x, Vars.player.y, 500));
                else r.append("{\"units\":[],\"count\":0}");
            } catch (Exception e) { r.append("{\"units\":[],\"count\":0,\"error\":\"").append(e.getMessage()).append("\"}"); }
            
            // Buildings
            r.append(",\"nearby_buildings\":");
            try {
                if (Vars.player != null) r.append(buildingScanner.scanBuildings(null, Vars.player.x, Vars.player.y, 500));
                else r.append("{\"buildings\":[],\"count\":0}");
            } catch (Exception e) { r.append("{\"buildings\":[],\"count\":0,\"error\":\"").append(e.getMessage()).append("\"}"); }
            
            // Resources
            r.append(",\"resources\":");
            try { r.append(resourceScanner.scanResources(null)); } catch (Exception e) { r.append("{\"error\":\"").append(e.getMessage()).append("\"}"); }
            
            r.append("}");
            Log.info("[AIAgentMod] scanFullState success");
            return r.toString();
        } catch (Exception e) {
            Log.err("[AIAgentMod] scanFullState FAILED: @", e);
            return "{\"status\":\"error\",\"message\":\"" + e.getClass().getSimpleName() + ": " + e.getMessage() + "\"}";
        }
    }
    
    public String scanMap() {
        try { return worldScanner.scanMap(); }
        catch (Exception e) { Log.err("[AIAgentMod] scanMap: @", e); return "{\"error\":\"" + e.getMessage() + "\"}"; }
    }
    
    public String scanPlayer() {
        try { return playerScanner.scanPlayer(); }
        catch (Exception e) { Log.err("[AIAgentMod] scanPlayer: @", e); return "{\"error\":\"" + e.getMessage() + "\"}"; }
    }
    
    public String scanUnits(String team, float x, float y, float radius) {
        try { return unitScanner.scanUnits(team, x, y, radius); }
        catch (Exception e) { return "{\"units\":[],\"count\":0,\"error\":\"" + e.getMessage() + "\"}"; }
    }
    
    public String scanBuildings(String team, float x, float y, float radius) {
        try { return buildingScanner.scanBuildings(team, x, y, radius); }
        catch (Exception e) { return "{\"buildings\":[],\"count\":0,\"error\":\"" + e.getMessage() + "\"}"; }
    }
    
    public String scanResources(String team) {
        try { return resourceScanner.scanResources(team); }
        catch (Exception e) { return "{\"error\":\"" + e.getMessage() + "\"}"; }
    }
    
    public String getStatus() {
        try {
            return "{\"in_game\":" + !Vars.state.isMenu()
                + ",\"wave\":" + Vars.state.wave
                + ",\"units\":" + Groups.unit.size()
                + ",\"buildings\":" + Groups.build.size() + "}";
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
