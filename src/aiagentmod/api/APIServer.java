package aiagentmod.api;

import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;
import arc.util.Log;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class APIServer extends Thread {

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public APIServer(int port) {
        this.port = port;
        setDaemon(true);
        setName("AIAgentMod-API");
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            running = true;
            Log.info("[AIAgentMod] API on port @", port);
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket client = serverSocket.accept();
                    executor.submit(() -> {
                        try { handleClient(client); } 
                        catch (Throwable e) { Log.err("[AIAgentMod] Client: @", e.getMessage()); }
                        finally {
                            // Ensure socket is closed
                            try { client.close(); } catch (Throwable ignored) {}
                        }
                    });
                } catch (SocketException e) {
                    if (running) Log.warn("[AIAgentMod] Socket: @", e.getMessage());
                }
            }
        } catch (Throwable e) {
            Log.err("[AIAgentMod] Server: @", e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private void handleClient(Socket client) {
        try {
            client.setSoTimeout(5000);

            // Use DataInputStream for reliable reading
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            // Read request line
            String requestLine = readLine(in);
            if (requestLine == null || requestLine.isEmpty()) {
                send(out, 400, "{\"error\":\"empty\"}");
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                send(out, 400, "{\"error\":\"bad_request\"}");
                return;
            }

            String method = parts[0];
            String uri = parts[1];
            String path = uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;

            Log.info("[AIAgentMod] @ @", method, path);

            // Read headers
            int contentLength = 0;
            String line;
            while ((line = readLine(in)) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    try { contentLength = Integer.parseInt(line.substring(15).trim()); } 
                    catch (Throwable ignored) {}
                }
            }

            // Read body
            String body = "";
            if (contentLength > 0) {
                byte[] buf = new byte[contentLength];
                int read = 0;
                while (read < contentLength) {
                    int r = in.read(buf, read, contentLength - read);
                    if (r < 0) break;
                    read += r;
                }
                body = new String(buf, 0, read, "UTF-8");
            }

            // Route
            String json;
            if (method.equals("OPTIONS")) {
                json = "{}";
            } else if (path.equals("/") || path.equals("/api")) {
                json = "{\"name\":\"AI Agent Mod\",\"version\":\"1.0.0\"}";
            } else if (path.equals("/api/debug") && method.equals("GET")) {
                json = "{\"debug\":\"ok\",\"ts\":" + System.currentTimeMillis() + "}";
            } else if (path.equals("/api/vars") && method.equals("GET")) {
                try {
                    StringBuilder d = new StringBuilder("{");
                    d.append("\"state_null\":").append(Vars.state == null);
                    d.append(",\"world_null\":").append(Vars.world == null);
                    d.append(",\"player_null\":").append(Vars.player == null);
                    d.append(",\"in_menu\":").append(Vars.state != null ? Vars.state.isMenu() : "null");
                    d.append("}");
                    json = d.toString();
                } catch (Throwable e) { json = "{\"error\":\"" + e.getClass().getSimpleName() + "\",\"msg\":\"" + e.getMessage() + "\"}"; }
            } else if (path.equals("/api/state") && method.equals("GET")) {
                json = handleState();
            } else if (path.equals("/api/map") && method.equals("GET")) {
                json = handleMap();
            } else if (path.equals("/api/player") && method.equals("GET")) {
                json = handlePlayer();
            } else if (path.equals("/api/units") && method.equals("GET")) {
                json = handleUnits();
            } else if (path.equals("/api/buildings") && method.equals("GET")) {
                json = handleBuildings();
            } else if (path.equals("/api/resources") && method.equals("GET")) {
                json = handleResources();
            } else if (path.startsWith("/api/action/") && method.equals("POST")) {
                String action = path.substring("/api/action/".length());
                json = handleAction(action, body);
            } else {
                json = "{\"error\":\"not_found\",\"path\":\"" + path + "\"}";
            }

            send(out, 200, json);

        } catch (Throwable e) {
            Log.err("[AIAgentMod] Handler: @", e.getMessage());
            try {
                OutputStream out = client.getOutputStream();
                send(out, 500, "{\"error\":\"internal\"}");
            } catch (Throwable ignored) {}
        }
    }

    // Read line from InputStream (handles \r\n)
    private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') continue;
            if (b == '\n') break;
            buf.write(b);
        }
        if (buf.size() == 0 && b == -1) return null;
        return buf.toString("UTF-8").trim();
    }

    // Send HTTP response with Connection: close
    private void send(OutputStream out, int status, String json) throws IOException {
        String statusText = status == 200 ? "OK" : status == 404 ? "Not Found" : status == 400 ? "Bad Request" : "Error";
        byte[] body = json.getBytes("UTF-8");

        StringBuilder h = new StringBuilder();
        h.append("HTTP/1.1 ").append(status).append(" ").append(statusText).append("\r\n");
        h.append("Content-Type: application/json\r\n");
        h.append("Content-Length: ").append(body.length).append("\r\n");
        h.append("Access-Control-Allow-Origin: *\r\n");
        h.append("Connection: close\r\n");
        h.append("\r\n");

        out.write(h.toString().getBytes("UTF-8"));
        out.write(body);
        out.flush();
    }

    // ===== HANDLERS =====

    private String handleState() {
        try {
            StringBuilder r = new StringBuilder("{");
            r.append("\"status\":\"ok\"");

            // Wave info
            try { r.append(",\"wave\":").append(Vars.state.wave); } catch (Throwable e) { r.append(",\"wave\":0"); }
            try { r.append(",\"waveTime\":").append(Math.round(Vars.state.wavetime)); } catch (Throwable e) { r.append(",\"waveTime\":0"); }

            // Unit and building counts
            r.append(",\"units\":");
            try { r.append(Groups.unit.size()); } catch (Throwable e) { r.append("0"); }
            r.append(",\"buildings\":");
            try { r.append(Groups.build.size()); } catch (Throwable e) { r.append("0"); }

            // Player info - each field individually protected
            r.append(",\"player\":");
            try {
                Player p = Vars.player;
                if (p == null) {
                    r.append("null");
                } else {
                    r.append("{");
                    // Position
                    try { r.append("\"x\":").append(Math.round(p.x)); } catch (Throwable e) { r.append("\"x\":0"); }
                    try { r.append(",\"y\":").append(Math.round(p.y)); } catch (Throwable e) { r.append(",\"y\":0"); }
                    // Team
                    try { r.append(",\"team\":\"").append(p.team().name).append("\""); } catch (Throwable e) { r.append(",\"team\":\"?\""); }
                    // Dead flag
                    try { r.append(",\"dead\":").append(p.dead()); } catch (Throwable e) { r.append(",\"dead\":false"); }
                    // Unit info
                    try {
                        Unit u = p.unit();
                        if (u != null) {
                            try { r.append(",\"unitX\":").append(Math.round(u.x)); } catch (Throwable e) { r.append(",\"unitX\":0"); }
                            try { r.append(",\"unitY\":").append(Math.round(u.y)); } catch (Throwable e) { r.append(",\"unitY\":0"); }
                            try { r.append(",\"health\":").append(Math.round(u.health)); } catch (Throwable e) { r.append(",\"health\":0"); }
                            try { r.append(",\"maxHealth\":").append(Math.round(u.maxHealth)); } catch (Throwable e) { r.append(",\"maxHealth\":0"); }
                            try { r.append(",\"type\":\"").append(u.type.name).append("\""); } catch (Throwable e) { r.append(",\"type\":\"unknown\""); }
                        } else {
                            r.append(",\"hasUnit\":false");
                        }
                    } catch (Throwable e) {
                        r.append(",\"unitError\":\"").append(e.getClass().getSimpleName()).append("\"");
                    }
                    r.append("}");
                }
            } catch (Throwable e) {
                r.append("{\"error\":\"").append(e.getClass().getSimpleName()).append("\"}");
            }

            r.append("}");
            return r.toString();
        } catch (Throwable e) {
            return "{\"status\":\"error\",\"msg\":\"" + e.getClass().getSimpleName() + "\"}";
        }
    }

    private String handleMap() {
        try {
            if (Vars.world == null) return "{\"error\":\"world_null\"}";
            StringBuilder r = new StringBuilder("{");
            r.append("\"width\":").append(Vars.world.width());
            r.append(",\"height\":").append(Vars.world.height());
            r.append(",\"tilesize\":").append(Vars.tilesize);
            r.append(",\"ores\":[");
            boolean first = true;
            int step = 5;
            for (int x = 0; x < Vars.world.width(); x += step) {
                for (int y = 0; y < Vars.world.height(); y += step) {
                    try {
                        Tile t = Vars.world.tile(x, y);
                        if (t == null) continue;
                        // Check overlay layer
                        mindustry.world.Block overlay = t.overlay();
                        if (overlay != null && overlay.itemDrop != null) {
                            if (!first) r.append(",");
                            first = false;
                            r.append("{\"type\":\"").append(overlay.itemDrop.name).append("\",\"x\":").append(x).append(",\"y\":").append(y).append("}");
                            continue;
                        }
                        // Check floor layer
                        mindustry.world.Block floor = t.floor();
                        if (floor != null && floor.itemDrop != null) {
                            if (!first) r.append(",");
                            first = false;
                            r.append("{\"type\":\"").append(floor.itemDrop.name).append("\",\"x\":").append(x).append(",\"y\":").append(y).append("}");
                        }
                    } catch (Throwable ignored) {}
                }
            }
            r.append("]}");
            return r.toString();
        } catch (Throwable e) { return "{\"error\":\"" + e.getClass().getSimpleName() + "\"}"; }
    }

    private String handlePlayer() {
        try {
            if (Vars.player == null) return "{\"error\":\"no_player\"}";
            Player p = Vars.player;
            StringBuilder r = new StringBuilder("{");
            try { r.append("\"x\":").append(Math.round(p.x)); } catch (Throwable e) { r.append("\"x\":0"); }
            try { r.append(",\"y\":").append(Math.round(p.y)); } catch (Throwable e) { r.append(",\"y\":0"); }
            try { r.append(",\"team\":\"").append(p.team().name).append("\""); } catch (Throwable e) { r.append(",\"team\":\"?\""); }
            try { r.append(",\"dead\":").append(p.dead()); } catch (Throwable e) { r.append(",\"dead\":false"); }
            try {
                Unit u = p.unit();
                if (u != null) {
                    try { r.append(",\"health\":").append(Math.round(u.health)); } catch (Throwable e) { r.append(",\"health\":0"); }
                    try { r.append(",\"maxHealth\":").append(Math.round(u.maxHealth)); } catch (Throwable e) { r.append(",\"maxHealth\":0"); }
                    try { r.append(",\"type\":\"").append(u.type.name).append("\""); } catch (Throwable e) { r.append(",\"type\":\"unknown\""); }
                    try { r.append(",\"unitX\":").append(Math.round(u.x)); } catch (Throwable e) { r.append(",\"unitX\":0"); }
                    try { r.append(",\"unitY\":").append(Math.round(u.y)); } catch (Throwable e) { r.append(",\"unitY\":0"); }
                } else {
                    r.append(",\"hasUnit\":false");
                }
            } catch (Throwable e) {
                r.append(",\"unitError\":\"").append(e.getClass().getSimpleName()).append("\"");
            }
            r.append("}");
            return r.toString();
        } catch (Throwable e) { return "{\"error\":\"" + e.getClass().getSimpleName() + "\"}"; }
    }

    private String handleUnits() {
        try {
            StringBuilder r = new StringBuilder("{\"units\":[");
            final boolean[] first = {true};
            final int[] count = {0};
            Groups.unit.each(u -> {
                if (count[0] >= 500 || u == null) return;
                try {
                    if (!first[0]) r.append(",");
                    first[0] = false;
                    r.append("{\"id\":").append(u.id).append(",\"type\":\"").append(u.type.name).append("\",\"x\":").append(Math.round(u.x)).append(",\"y\":").append(Math.round(u.y)).append("}");
                    count[0]++;
                } catch (Throwable ignored) {}
            });
            r.append("],\"count\":").append(count[0]).append("}");
            return r.toString();
        } catch (Throwable e) { return "{\"units\":[],\"count\":0,\"error\":\"" + e.getMessage() + "\"}"; }
    }

    private String handleBuildings() {
        try {
            StringBuilder r = new StringBuilder("{\"buildings\":[");
            final boolean[] first = {true};
            final int[] count = {0};
            Groups.build.each(b -> {
                if (count[0] >= 500 || b == null) return;
                try {
                    if (!first[0]) r.append(",");
                    first[0] = false;
                    r.append("{\"id\":").append(b.id).append(",\"type\":\"").append(b.block.name).append("\",\"x\":").append(Math.round(b.x)).append(",\"y\":").append(Math.round(b.y)).append("}");
                    count[0]++;
                } catch (Throwable ignored) {}
            });
            r.append("],\"count\":").append(count[0]).append("}");
            return r.toString();
        } catch (Throwable e) { return "{\"buildings\":[],\"count\":0,\"error\":\"" + e.getMessage() + "\"}"; }
    }

    private String handleResources() {
        try {
            if (Vars.player == null) return "{\"error\":\"no_player\"}";
            mindustry.game.Team team = Vars.player.team();
            StringBuilder r = new StringBuilder("{");
            r.append("\"team\":\"").append(team.name).append("\"");
            try {
                if (team.core() != null && team.core().items != null) {
                    r.append(",\"items\":{");
                    boolean first = true;
                    for (mindustry.type.Item item : Vars.content.items()) {
                        try {
                            int amt = team.core().items.get(item);
                            if (amt > 0) {
                                if (!first) r.append(",");
                                first = false;
                                r.append("\"").append(item.name).append("\":").append(amt);
                            }
                        } catch (Throwable ignored) {}
                    }
                    r.append("}");
                }
            } catch (Throwable ignored) {}
            r.append("}");
            return r.toString();
        } catch (Throwable e) { return "{\"error\":\"" + e.getMessage() + "\"}"; }
    }

    private String handleAction(String action, String body) {
        Log.info("[AIAgentMod] Action: @ body='@'", action, body);
        return "{\"success\":true,\"action\":\"" + action + "\"}";
    }

    public void processQueuedActions() { /* synchronous now */ }

    public void stopServer() {
        running = false;
        executor.shutdownNow();
        try { if (serverSocket != null) serverSocket.close(); } catch (Throwable ignored) {}
    }

    public boolean isRunning() { return running; }
}
