package aiagentmod;

import aiagentmod.api.APIServer;
import mindustry.Vars;
import mindustry.mod.Mod;
import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;

/**
 * AI Agent Mod - Main entry point for the Mindustry AI Agent API.
 * 
 * This mod exposes a REST HTTP API that allows any AI agent to:
 * - Observe the full game state (player, units, buildings, resources, map)
 * - Control the player (move, build, mine, shoot)
 * - Command units (select, move, attack, patrol)
 * - Configure buildings (rotate, set config)
 * 
 * The API server starts automatically when the mod is loaded (desktop only).
 * Default API endpoint: http://localhost:8089
 * 
 * @author AI Agent
 * @version 1.0.0
 */
public class AIAgentMod extends Mod {
    
    /** Singleton instance of the mod */
    public static AIAgentMod instance;
    
    /** The REST API server */
    private APIServer apiServer;
    
    /** API server port (default: 8089) */
    public int apiPort = 8089;
    
    /** Whether the API is enabled */
    public boolean apiEnabled = true;
    
    /** Mod version */
    public static final String version = "1.0.0";
    
    public AIAgentMod() {
        instance = this;
        Log.info("[AIAgentMod] Constructor called v@", version);
    }
    
    @Override
    public void init() {
        super.init();
        Log.info("[AIAgentMod] Initializing AI Agent Mod v@...", version);
        
        // Only start API server on desktop (not on Android)
        if (!Vars.headless && !Vars.android && apiEnabled) {
            startAPIServer();
        } else if (Vars.headless) {
            Log.info("[AIAgentMod] Running in headless mode - API server not started");
        }
        
        // Register update callback for processing queued actions on main thread
        Events.run(Trigger.update, this::update);
        
        Log.info("[AIAgentMod] Initialization complete");
    }
    
    @Override
    public void loadContent() {
        Log.info("[AIAgentMod] Loading content...");
        // Content loading if needed in future versions
    }
    
    /**
     * Starts the REST API server on the configured port.
     */
    private void startAPIServer() {
        try {
            apiServer = new APIServer(apiPort);
            apiServer.start();
            Log.info("[AIAgentMod] API server started on http://localhost:@", apiPort);
            Log.info("[AIAgentMod] Available endpoints:");
            Log.info("  GET  /api/state     - Full game state");
            Log.info("  GET  /api/map       - Map information");
            Log.info("  GET  /api/player    - Player state");
            Log.info("  GET  /api/units     - Units list");
            Log.info("  GET  /api/buildings - Buildings list");
            Log.info("  GET  /api/resources - Team resources");
            Log.info("  POST /api/action/*  - Execute actions");
        } catch (Exception e) {
            Log.err("[AIAgentMod] Failed to start API server: @", e.getMessage());
        }
    }
    
    /**
     * Called every game tick. Processes queued actions on the main thread.
     */
    private void update() {
        if (apiServer != null) {
            apiServer.processQueuedActions();
        }
    }
    
    /**
     * Stops the API server. Called when mod is unloaded.
     */
    public void stopAPIServer() {
        if (apiServer != null) {
            apiServer.stop();
            apiServer = null;
            Log.info("[AIAgentMod] API server stopped");
        }
    }
    
    /**
     * Returns whether the API server is running.
     */
    public boolean isAPIServerRunning() {
        return apiServer != null && apiServer.isAlive();
    }
    
    /**
     * Gets the API server instance.
     */
    public APIServer getAPIServer() {
        return apiServer;
    }
}
