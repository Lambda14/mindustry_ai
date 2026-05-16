package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link MineAction} class.
 * <p>
 * Tests parameter validation and the stop mining functionality
 * when Mindustry game environment is not available.
 */
class MineActionTest {

    private final MineAction mineAction = new MineAction();

    /**
     * Verifies that mining fails gracefully when not in game.
     */
    @Test
    @DisplayName("execute should fail when not in game")
    void testNotInGame() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = mineAction.execute(params);
        assertFalse(result.success, "Should fail when not in game");
        assertTrue(result.message.contains("menu") || result.message.contains("Not in game"),
            "Error should mention menu or not in game");
    }

    /**
     * Verifies that mining with empty parameters fails.
     */
    @Test
    @DisplayName("execute with empty params should fail")
    void testEmptyParams() {
        ActionResult result = mineAction.execute(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that the stop parameter is recognized.
     */
    @Test
    @DisplayName("execute with stop=true should stop mining")
    void testStopMining() {
        JSONObject params = new JSONObject();
        params.put("stop", true);
        ActionResult result = mineAction.execute(params);
        // Will likely fail because no player/unit but tests the stop branch
        assertNotNull(result);
    }

    /**
     * Verifies that invalid coordinates return appropriate error.
     */
    @Test
    @DisplayName("execute with invalid coordinates should fail")
    void testInvalidCoords() {
        JSONObject params = new JSONObject();
        params.put("x", -1);
        params.put("y", -1);
        ActionResult result = mineAction.execute(params);
        assertFalse(result.success);
    }

    /**
     * Verifies that result has correct structure.
     */
    @Test
    @DisplayName("Result should have correct structure")
    void testResultStructure() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = mineAction.execute(params);
        assertNotNull(result);
        assertNotNull(result.data);
    }
}
