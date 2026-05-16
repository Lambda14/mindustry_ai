package aiagentmod.api;

import aiagentmod.observation.*;
import aiagentmod.action.*;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST API server for AI Agent Mod.
 * Uses NanoHTTPD as the embedded HTTP server.
 * 
 * All endpoints return JSON responses with CORS headers enabled.
 * Actions that modify game state are queued and executed on the main game thread.
 * 
 * Base URL: http://localhost:8089/api/
 * 
 * @author AI Agent
 * @version 1.0.0
 */
public class APIServer extends NanoHTTPD {
    
    private final ObservationSystem observation;
    private final ActionSystem actionSystem;
    private final ExecutorService executor;
    
    /** Queue for actions that must run on main thread */
    private final ConcurrentLinkedQueue<Runnable> actionQueue = new ConcurrentLinkedQueue<>();
    
    /** Maximum actions to process per tick */
    private static final int MAX_ACTIONS_PER_TICK = 100;
    
    /** Maximum units/buildings returned in a single request */
    private static final int MAX_RESULTS_LIMIT = 500;
    
    public APIServer(int port) throws IOException {
        super(port);
        this.observation = new ObservationSystem();
        this.actionSystem = new ActionSystem();
        this.executor = Executors.newFixedThreadPool(4);
        
        // Enable CORS
        // AsyncRunner not needed for tests
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        
        try {
            // Handle CORS preflight
            if (method == Method.OPTIONS) {
                return addCorsHeaders(newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, ""));
            }
            
            // Route to appropriate handler
            Response response;
            
            if (uri.equals("/api/state") && method == Method.GET) {
                response = handleState(session);
            } else if (uri.equals("/api/map") && method == Method.GET) {
                response = handleMap(session);
            } else if (uri.equals("/api/player") && method == Method.GET) {
                response = handlePlayer(session);
            } else if (uri.equals("/api/units") && method == Method.GET) {
                response = handleUnits(session);
            } else if (uri.equals("/api/buildings") && method == Method.GET) {
                response = handleBuildings(session);
            } else if (uri.equals("/api/resources") && method == Method.GET) {
                response = handleResources(session);
            } else if (uri.equals("/api/action/move") && method == Method.POST) {
                response = handleActionMove(session);
            } else if (uri.equals("/api/action/build") && method == Method.POST) {
                response = handleActionBuild(session);
            } else if (uri.equals("/api/action/break") && method == Method.POST) {
                response = handleActionBreak(session);
            } else if (uri.equals("/api/action/mine") && method == Method.POST) {
                response = handleActionMine(session);
            } else if (uri.equals("/api/action/shoot") && method == Method.POST) {
                response = handleActionShoot(session);
            } else if (uri.equals("/api/action/unit_command") && method == Method.POST) {
                response = handleActionUnitCommand(session);
            } else if (uri.equals("/api/action/configure") && method == Method.POST) {
                response = handleActionConfigure(session);
            } else if (uri.equals("/api/action/rotate") && method == Method.POST) {
                response = handleActionRotate(session);
            } else if (uri.equals("/") || uri.equals("/api")) {
                response = handleRoot(session);
            } else {
                response = jsonResponse(Status.NOT_FOUND, "{\"error\":\"Not found\",\"available\":[\"/api/state\",\"/api/map\",\"/api/player\",\"/api/units\",\"/api/buildings\",\"/api/resources\",\"/api/action/*\"]}");
            }
            
            return addCorsHeaders(response);
            
        } catch (Exception e) {
            return addCorsHeaders(jsonResponse(Status.INTERNAL_ERROR, 
                "{\"error\":\"Internal server error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}"));
        }
    }
    
    // ============ GET ENDPOINTS ============
    
    private Response handleState(IHTTPSession session) {
        try {
            JSONObject state = observation.scanFullState();
            return jsonResponse(Status.OK, state.toString());
        } catch (Exception e) {
            return jsonResponse(Status.OK, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    private Response handleMap(IHTTPSession session) {
        try {
            JSONObject map = observation.scanMap();
            return jsonResponse(Status.OK, map.toString());
        } catch (Exception e) {
            return jsonResponse(Status.OK, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    private Response handlePlayer(IHTTPSession session) {
        try {
            JSONObject player = observation.scanPlayer();
            return jsonResponse(Status.OK, player.toString());
        } catch (Exception e) {
            return jsonResponse(Status.OK, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    private Response handleUnits(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String team = params.get("team");
            float x = parseFloat(params.get("x"), 0);
            float y = parseFloat(params.get("y"), 0);
            float radius = parseFloat(params.get("radius"), 0);
            
            JSONObject units = observation.scanUnits(team, x, y, radius);
            return jsonResponse(Status.OK, units.toString());
        } catch (Exception e) {
            return jsonResponse(Status.OK, "{\"error\":\"" + escapeJson(e.getMessage()) + "\",\"units\":[],\"count\":0}");
        }
    }
    
    private Response handleBuildings(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String team = params.get("team");
            float x = parseFloat(params.get("x"), 0);
            float y = parseFloat(params.get("y"), 0);
            float radius = parseFloat(params.get("radius"), 0);
            
            JSONObject buildings = observation.scanBuildings(team, x, y, radius);
            return jsonResponse(Status.OK, buildings.toString());
        } catch (Exception e) {
            return jsonResponse(Status.OK, "{\"error\":\"" + escapeJson(e.getMessage()) + "\",\"buildings\":[],\"count\":0}");
        }
    }
    
    private Response handleResources(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String team = params.get("team");
            
            JSONObject resources = observation.scanResources(team);
            return jsonResponse(Status.OK, resources.toString());
        } catch (Exception e) {
            return jsonResponse(Status.OK, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    // ============ POST ACTION ENDPOINTS ============
    
    private Response handleActionMove(IHTTPSession session) {
        return queueAction("move", session);
    }
    
    private Response handleActionBuild(IHTTPSession session) {
        return queueAction("build", session);
    }
    
    private Response handleActionBreak(IHTTPSession session) {
        return queueAction("break", session);
    }
    
    private Response handleActionMine(IHTTPSession session) {
        return queueAction("mine", session);
    }
    
    private Response handleActionShoot(IHTTPSession session) {
        return queueAction("shoot", session);
    }
    
    private Response handleActionUnitCommand(IHTTPSession session) {
        return queueAction("unit_command", session);
    }
    
    private Response handleActionConfigure(IHTTPSession session) {
        return queueAction("configure", session);
    }
    
    private Response handleActionRotate(IHTTPSession session) {
        return queueAction("rotate", session);
    }
    
    // ============ ROOT ENDPOINT ============
    
    private Response handleRoot(IHTTPSession session) {
        String info = "{"
            + "\"name\":\"AI Agent Mod API\","
            + "\"version\":\"1.0.0\","
            + "\"endpoints\":{"
            + "  \"GET /api/state\":\"Full game state snapshot\","
            + "  \"GET /api/map\":\"Map information\","
            + "  \"GET /api/player\":\"Player details\","
            + "  \"GET /api/units?team=&x=&y=&radius=\":\"List units\","
            + "  \"GET /api/buildings?team=&x=&y=&radius=\":\"List buildings\","
            + "  \"GET /api/resources?team=\":\"Team resources\","
            + "  \"POST /api/action/move\":\"Move player {x,y}\","
            + "  \"POST /api/action/build\":\"Build block {x,y,block,rotation,config}\","
            + "  \"POST /api/action/break\":\"Break block {x,y}\","
            + "  \"POST /api/action/mine\":\"Mine at {x,y}\","
            + "  \"POST /api/action/shoot\":\"Shoot {x,y,shoot}\","
            + "  \"POST /api/action/unit_command\":\"Command units {unit_ids,command,x,y,target_id}\","
            + "  \"POST /api/action/configure\":\"Configure building {building_id,config}\","
            + "  \"POST /api/action/rotate\":\"Rotate building {building_id,direction}\""
            + "}"
            + "}";
        return jsonResponse(Status.OK, info);
    }
    
    // ============ HELPER METHODS ============
    
    /**
     * Queues an action for execution on the main game thread.
     */
    private Response queueAction(String actionType, IHTTPSession session) {
        try {
            final String body = readBody(session);
            final JSONObject params = body.isEmpty() ? new JSONObject() : new JSONObject(body);
            
            // Create a holder for the result
            final ActionResult[] resultHolder = new ActionResult[1];
            resultHolder[0] = ActionResult.error("Action queued but not yet executed");
            
            // Queue the action for main thread execution
            actionQueue.offer(() -> {
                try {
                    ActionResult result = actionSystem.execute(actionType, params);
                    resultHolder[0] = result;
                } catch (Exception e) {
                    resultHolder[0] = ActionResult.error("Action execution failed: " + e.getMessage());
                }
            });
            
            // Wait a short time for immediate execution (single player only)
            // On multiplayer, action will be queued and executed asynchronously
            long startTime = System.currentTimeMillis();
            while (resultHolder[0].message.equals("Action queued but not yet executed") 
                    && System.currentTimeMillis() - startTime < 100) {
                Thread.sleep(5);
            }
            
            return jsonResponse(Status.OK, resultHolder[0].toJson().toString());
            
        } catch (Exception e) {
            return jsonResponse(Status.BAD_REQUEST, 
                "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    /**
     * Processes queued actions. Must be called on the main game thread every tick.
     */
    public void processQueuedActions() {
        int processed = 0;
        while (!actionQueue.isEmpty() && processed < MAX_ACTIONS_PER_TICK) {
            Runnable action = actionQueue.poll();
            if (action != null) {
                try {
                    action.run();
                } catch (Exception e) {
                    System.err.println("[AIAgentMod] Action execution error: " + e.getMessage());
                }
                processed++;
            }
        }
    }
    
    /**
     * Reads the request body from the session.
     */
    private String readBody(IHTTPSession session) throws IOException {
        Map<String, String> files = new java.util.HashMap<>();
        session.parseBody(files);
        String body = session.getQueryParameterString();
        if (body == null || body.isEmpty()) {
            for (String filename : files.values()) {
                java.io.File file = new java.io.File(filename);
                if (file.exists()) {
                    body = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                    break;
                }
            }
        }
        return body != null ? body : "";
    }
    
    /**
     * Creates a JSON response with proper content type.
     */
    private Response jsonResponse(Status status, String json) {
        return newFixedLengthResponse(status, "application/json", json);
    }
    
    /**
     * Adds CORS headers to any response.
     */
    private Response addCorsHeaders(Response response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.addHeader("Access-Control-Max-Age", "86400");
        return response;
    }
    
    /**
     * Escapes a string for use in JSON.
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Parses a float parameter with default fallback.
     */
    private float parseFloat(String value, float defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public void stop() {
        super.stop();
        if (executor != null) {
            executor.shutdown();
        }
        actionQueue.clear();
    }
}
