package aiagentmod.observation;

import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;
import arc.struct.ObjectSet;
import arc.util.Log;
import aiagentmod.api.SimpleJson;

public class WorldScanner {
    
    public String scanMap() {
        try {
            if (Vars.state == null) return "{\"error\":\"state_null\"}";
            if (Vars.state.isMenu()) return "{\"error\":\"in_menu\"}";
            if (Vars.world == null) return "{\"error\":\"world_null\"}";
            
            int w = Vars.world.width();
            int h = Vars.world.height();
            String mapName = "unknown";
            try { if (Vars.state.map != null) mapName = Vars.state.map.name(); } catch (Exception ignored) {}
            
            SimpleJson r = SimpleJson.object()
                .put("width", w)
                .put("height", h)
                .put("tilesize", Vars.tilesize)
                .put("map_name", mapName)
                .put("wave", safeWave())
                .put("ores", scanOres());
            
            return r.toString();
        } catch (Exception e) {
            Log.err("[AIAgentMod] WorldScanner.scanMap: @", e);
            return "{\"error\":\"" + e.getClass().getSimpleName() + "\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public String scanTile(int x, int y) {
        try {
            if (Vars.world == null) return "{\"error\":\"world_null\"}";
            Tile tile = Vars.world.tile(x, y);
            if (tile == null) return "{\"error\":\"out_of_bounds\"}";
            
            SimpleJson r = SimpleJson.object()
                .put("x", x).put("y", y)
                .put("floor", tile.floor().name)
                .put("block", tile.block().name);
            
            if (tile.overlay() instanceof OreBlock) {
                OreBlock ore = (OreBlock) tile.overlay();
                if (ore.itemDrop != null) r.put("ore", ore.itemDrop.name);
            }
            return r.toString();
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    private String scanOres() {
        try {
            if (Vars.world == null) return "[]";
            StringBuilder ores = new StringBuilder("[");
            ObjectSet<String> found = new ObjectSet<>();
            int step = Math.max(1, Math.min(Vars.world.width(), Vars.world.height()) / 20);
            boolean first = true;
            
            for (int x = 0; x < Vars.world.width(); x += step) {
                for (int y = 0; y < Vars.world.height(); y += step) {
                    try {
                        Tile tile = Vars.world.tile(x, y);
                        if (tile == null || !(tile.overlay() instanceof OreBlock)) continue;
                        OreBlock ore = (OreBlock) tile.overlay();
                        if (ore.itemDrop == null) continue;
                        String name = ore.itemDrop.name;
                        if (found.contains(name)) continue;
                        found.add(name);
                        if (!first) ores.append(",");
                        first = false;
                        ores.append("{\"type\":\"").append(name).append("\",\"x\":").append(x)
                            .append(",\"y\":").append(y).append("}");
                    } catch (Exception ignored) {}
                }
            }
            return ores.append("]").toString();
        } catch (Exception e) { return "[]"; }
    }
    
    private int safeWave() {
        try { return Vars.state.wave; } catch (Exception e) { return 0; }
    }
}
