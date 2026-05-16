package aiagentmod.observation;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Helper utilities for JSON serialization of common game types.
 */
public class JsonSerializer {
    
    /**
     * Creates a JSON point object with world coordinates.
     */
    public static JSONObject point(float worldX, float worldY) {
        JSONObject point = new JSONObject();
        point.put("x", Math.round(worldX * 100f) / 100f);
        point.put("y", Math.round(worldY * 100f) / 100f);
        return point;
    }
    
    /**
     * Creates a JSON point with both tile and world coordinates.
     */
    public static JSONObject point(int tileX, int tileY, float worldX, float worldY) {
        JSONObject point = new JSONObject();
        point.put("tile_x", tileX);
        point.put("tile_y", tileY);
        point.put("world_x", Math.round(worldX * 100f) / 100f);
        point.put("world_y", Math.round(worldY * 100f) / 100f);
        return point;
    }
    
    /**
     * Creates a JSON rectangle object.
     */
    public static JSONObject rect(float x, float y, float width, float height) {
        JSONObject rect = new JSONObject();
        rect.put("x", Math.round(x * 100f) / 100f);
        rect.put("y", Math.round(y * 100f) / 100f);
        rect.put("width", Math.round(width * 100f) / 100f);
        rect.put("height", Math.round(height * 100f) / 100f);
        return rect;
    }
    
    /**
     * Serializes a Mindustry color to RGBA components.
     */
    public static JSONObject color(arc.graphics.Color color) {
        JSONObject c = new JSONObject();
        c.put("r", Math.round(color.r * 255));
        c.put("g", Math.round(color.g * 255));
        c.put("b", Math.round(color.b * 255));
        c.put("a", Math.round(color.a * 255));
        c.put("hex", color.toString());
        return c;
    }
    
    /**
     * Converts a game state to a summary JSON.
     */
    public static JSONObject gameState() {
        JSONObject state = new JSONObject();
        state.put("is_menu", mindustry.Vars.state.isMenu());
        state.put("is_playing", !mindustry.Vars.state.isMenu());
        state.put("is_paused", mindustry.Vars.state.isPaused());
        state.put("is_game_over", mindustry.Vars.state.isGameOver());
        state.put("wave", mindustry.Vars.state.wave);
        state.put("map_name", mindustry.Vars.state.map != null ? mindustry.Vars.state.map.name() : "unknown");
        return state;
    }
    
    /**
     * Creates a quick summary JSON.
     */
    public static JSONObject summary() {
        JSONObject summary = new JSONObject();
        summary.put("in_game", !mindustry.Vars.state.isMenu());
        summary.put("units", mindustry.gen.Groups.unit.size());
        summary.put("buildings", mindustry.gen.Groups.build.size());
        
        if (mindustry.Vars.player != null && mindustry.Vars.player.unit() != null) {
            summary.put("player_health", Math.round(mindustry.Vars.player.unit().health * 100f) / 100f);
        }
        
        return summary;
    }
}
