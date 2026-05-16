package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import org.json.JSONArray;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link UnitCommandAction} class.
 * <p>
 * Tests parameter validation for the unit command action
 * when Mindustry game environment is not available.
 */
class UnitCommandActionTest {

    private final UnitCommandAction unitCommandAction = new UnitCommandAction();

    /**
     * Verifies that unit command fails gracefully when not in game.
     */
    @Test
    @DisplayName("execute should fail when not in game")
    void testNotInGame() {
        JSONObject params = createValidParams();
        ActionResult result = unitCommandAction.execute(params);
        assertFalse(result.success, "Should fail when not in game");
        assertTrue(result.message.contains("menu") || result.message.contains("Not in game"),
            "Error should mention menu or not in game");
    }

    /**
     * Verifies that unit command with empty parameters fails.
     */
    @Test
    @DisplayName("execute with empty params should fail")
    void testEmptyParams() {
        ActionResult result = unitCommandAction.execute(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that unit command without unit_ids fails.
     */
    @Test
    @DisplayName("execute without unit_ids should fail")
    void testNoUnitIds() {
        JSONObject params = new JSONObject();
        params.put("command", "move");
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = unitCommandAction.execute(params);
        assertFalse(result.success, "Should fail without unit_ids");
    }

    /**
     * Verifies that unit command with empty unit_ids array fails.
     */
    @Test
    @DisplayName("execute with empty unit_ids array should fail")
    void testEmptyUnitIds() {
        JSONObject params = new JSONObject();
        params.put("unit_ids", new JSONArray());
        params.put("command", "move");
        ActionResult result = unitCommandAction.execute(params);
        assertFalse(result.success, "Should fail with empty unit_ids");
    }

    /**
     * Verifies that unit command accepts different command types.
     */
    @Test
    @DisplayName("execute should accept various command types")
    void testVariousCommands() {
        String[] commands = {"move", "attack", "mine", "repair", "rebuild", "assist", "rally"};
        for (String cmd : commands) {
            JSONObject params = createValidParams();
            params.put("command", cmd);
            ActionResult result = unitCommandAction.execute(params);
            assertNotNull(result, "Result should not be null for command: " + cmd);
        }
    }

    /**
     * Verifies that unit command accepts target_id parameter.
     */
    @Test
    @DisplayName("execute should accept target_id parameter")
    void testTargetId() {
        JSONObject params = createValidParams();
        params.put("target_id", 42);
        params.remove("x");
        params.remove("y");
        ActionResult result = unitCommandAction.execute(params);
        assertNotNull(result);
    }

    /**
     * Verifies that unit command accepts position parameters.
     */
    @Test
    @DisplayName("execute should accept x,y position parameters")
    void testPositionParams() {
        JSONObject params = createValidParams();
        params.put("x", 500);
        params.put("y", 600);
        ActionResult result = unitCommandAction.execute(params);
        assertNotNull(result);
    }

    /**
     * Verifies that result has correct structure.
     */
    @Test
    @DisplayName("Result should have correct structure")
    void testResultStructure() {
        JSONObject params = createValidParams();
        ActionResult result = unitCommandAction.execute(params);
        assertNotNull(result);
        assertNotNull(result.data);
    }

    /**
     * Helper method to create a valid parameters object for unit commands.
     */
    private JSONObject createValidParams() {
        JSONObject params = new JSONObject();
        JSONArray unitIds = new JSONArray();
        unitIds.put(1);
        unitIds.put(2);
        params.put("unit_ids", unitIds);
        params.put("command", "move");
        params.put("x", 100);
        params.put("y", 200);
        return params;
    }
}
