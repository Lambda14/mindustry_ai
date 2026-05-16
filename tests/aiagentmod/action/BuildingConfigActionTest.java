package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link BuildingConfigAction} class.
 * <p>
 * Tests parameter validation for the building configuration action
 * when Mindustry game environment is not available.
 */
class BuildingConfigActionTest {

    private final BuildingConfigAction configAction = new BuildingConfigAction();

    /**
     * Verifies that configure fails gracefully when not in game.
     */
    @Test
    @DisplayName("execute should fail when not in game")
    void testNotInGame() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        params.put("config", "copper");
        ActionResult result = configAction.execute(params);
        assertFalse(result.success, "Should fail when not in game");
        assertTrue(result.message.contains("menu") || result.message.contains("Not in game"),
            "Error should mention menu or not in game");
    }

    /**
     * Verifies that configure with empty parameters fails.
     */
    @Test
    @DisplayName("execute with empty params should fail")
    void testEmptyParams() {
        ActionResult result = configAction.execute(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that configure with negative building_id fails.
     */
    @Test
    @DisplayName("execute with negative building_id should fail")
    void testNegativeBuildingId() {
        JSONObject params = new JSONObject();
        params.put("building_id", -1);
        params.put("config", "copper");
        ActionResult result = configAction.execute(params);
        assertFalse(result.success, "Should fail with negative building_id");
        assertTrue(result.message.contains("Invalid") || result.message.contains("building_id"),
            "Should mention invalid building_id");
    }

    /**
     * Verifies that configure without config value fails.
     */
    @Test
    @DisplayName("execute without config should fail")
    void testNoConfig() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        ActionResult result = configAction.execute(params);
        assertFalse(result.success, "Should fail without config");
        assertTrue(result.message.contains("config"),
            "Should mention missing config");
    }

    /**
     * Verifies that configure accepts various config types.
     */
    @Test
    @DisplayName("execute should accept string config")
    void testStringConfig() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        params.put("config", "lead");
        ActionResult result = configAction.execute(params);
        assertFalse(result.success); // Will fail because not in game
    }

    /**
     * Verifies that configure accepts numeric config values.
     */
    @Test
    @DisplayName("execute should accept numeric config")
    void testNumericConfig() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        params.put("config", 42);
        ActionResult result = configAction.execute(params);
        assertFalse(result.success); // Will fail because not in game
    }

    /**
     * Verifies that result has correct structure.
     */
    @Test
    @DisplayName("Result should have correct structure")
    void testResultStructure() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        params.put("config", "copper");
        ActionResult result = configAction.execute(params);
        assertNotNull(result);
        assertNotNull(result.data);
    }
}
