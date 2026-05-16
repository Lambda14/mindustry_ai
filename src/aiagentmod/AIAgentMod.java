package aiagentmod;

import aiagentmod.api.APIServer;
import mindustry.Vars;
import mindustry.mod.Mod;
import arc.util.Log;

/**
 * AI Agent Mod - REST API for AI agents to observe and control Mindustry.
 *
 * API runs on http://localhost:8089 when mod is loaded.
 * Actions execute synchronously (no external dependencies).
 *
 * @author AI Agent
 * @version 1.0.0
 */
public class AIAgentMod extends Mod {

    public static AIAgentMod instance;
    private APIServer apiServer;
    public int apiPort = 8089;
    public boolean apiEnabled = true;
    public static final String version = "1.0.0";

    public AIAgentMod() {
        instance = this;
    }

    @Override
    public void init() {
        Log.info("[AIAgentMod] Init v@", version);

        if (!Vars.headless && !Vars.android && apiEnabled) {
            startAPIServer();
        }

        Log.info("[AIAgentMod] Ready");
    }

    private void startAPIServer() {
        try {
            apiServer = new APIServer(apiPort);
            apiServer.start();
            Log.info("[AIAgentMod] API: http://localhost:@", apiPort);
        } catch (Exception e) {
            Log.err("[AIAgentMod] API start failed: @", e.getMessage());
        }
    }

    public void stopAPIServer() {
        if (apiServer != null) {
            apiServer.stopServer();
            apiServer = null;
            Log.info("[AIAgentMod] API stopped");
        }
    }

    public boolean isAPIServerRunning() {
        return apiServer != null && apiServer.isRunning();
    }
}
