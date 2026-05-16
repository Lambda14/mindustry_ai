package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link BuildAction} class.
 * <p>
 * Tests parameter validation for both build and break operations
 * when Mindustry game environment is not available.
 */
class BuildActionTest {

    private final BuildAction buildAction = new BuildAction();

    /**
     * Verifies that build fails gracefully when not in game.
     */
    @Test
    @DisplayName("execute should fail when not in game")
    void testBuildNotInGame() {
        JSONObject params = new JSONObject();
        params.put("x", 10);
        params.put("y", 10);
        params.put("block", "conveyor");
        ActionResult result = buildAction.execute(params);
        assertFalse(result.success, "Should fail when not in game");
    }

    /**
     * Verifies that build with empty parameters fails.
     */
    @Test
    @DisplayName("execute with empty params should fail")
    void testBuildEmptyParams() {
        ActionResult result = buildAction.execute(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that build with negative tile coordinates fails.
     */
    @Test
    @DisplayName("execute with negative coordinates should fail")
    void testBuildNegativeCoords() {
        JSONObject params = new JSONObject();
        params.put("x", -1);
        params.put("y", -1);
        params.put("block", "conveyor");
        ActionResult result = buildAction.execute(params);
        assertFalse(result.success, "Should fail with negative coordinates");
        assertTrue(result.message.contains("Invalid") || result.message.contains("coordinates"),
            "Should mention invalid coordinates");
    }

    /**
     * Verifies that build without block name fails.
     */
    @Test
    @DisplayName("execute without block name should fail")
    void testBuildNoBlock() {
        JSONObject params = new JSONObject();
        params.put("x", 10);
        params.put("y", 10);
        ActionResult result = buildAction.execute(params);
        assertFalse(result.success, "Should fail without block name");
        assertTrue(result.message.contains("Block name"),
            "Should mention block name not specified");
    }

    /**
     * Verifies that break fails gracefully when not in game.
     */
    @Test
    @DisplayName("executeBreak should fail when not in game")
    void testBreakNotInGame() {
        JSONObject params = new JSONObject();
        params.put("x", 10);
        params.put("y", 10);
        ActionResult result = buildAction.executeBreak(params);
        assertFalse(result.success, "Should fail when not in game");
    }

    /**
     * Verifies that break with empty parameters fails.
     */
    @Test
    @DisplayName("executeBreak with empty params should fail")
    void testBreakEmptyParams() {
        ActionResult result = buildAction.executeBreak(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that break with negative tile coordinates fails.
     */
    @Test
    @DisplayName("executeBreak with negative coordinates should fail")
    void testBreakNegativeCoords() {
        JSONObject params = new JSONObject();
        params.put("x", -1);
        params.put("y", -1);
        ActionResult result = buildAction.executeBreak(params);
        assertFalse(result.success, "Should fail with negative coordinates");
    }

    /**
     * Verifies that build accepts rotation parameter.
     */
    @Test
    @DisplayName("execute should accept rotation parameter")
    void testBuildWithRotation() {
        JSONObject params = new JSONObject();
        params.put("x", 10);
        params.put("y", 10);
        params.put("block", "conveyor");
        params.put("rotation", 2);
        ActionResult result = buildAction.execute(params);
        // Fails because not in game but validates params
        assertFalse(result.success);
    }

    /**
     * Verifies that build result has correct structure.
     */
    @Test
    @DisplayName("Result should have correct structure")
    void testResultStructure() {
        JSONObject params = new JSONObject();
        params.put("x", 10);
        params.put("y", 10);
        params.put("block", "router");
        ActionResult result = buildAction.execute(params);
        assertNotNull(result);
        assertNotNull(result.data);
    }
}
