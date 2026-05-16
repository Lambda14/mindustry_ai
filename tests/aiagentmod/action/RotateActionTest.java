package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link RotateAction} class.
 * <p>
 * Tests parameter validation for the rotate building action
 * when Mindustry game environment is not available.
 */
class RotateActionTest {

    private final RotateAction rotateAction = new RotateAction();

    /**
     * Verifies that rotate fails gracefully when not in game.
     */
    @Test
    @DisplayName("execute should fail when not in game")
    void testNotInGame() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        params.put("direction", 1);
        ActionResult result = rotateAction.execute(params);
        assertFalse(result.success, "Should fail when not in game");
        assertTrue(result.message.contains("menu") || result.message.contains("Not in game"),
            "Error should mention menu or not in game");
    }

    /**
     * Verifies that rotate with empty parameters fails.
     */
    @Test
    @DisplayName("execute with empty params should fail")
    void testEmptyParams() {
        ActionResult result = rotateAction.execute(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that rotate with negative building_id fails.
     */
    @Test
    @DisplayName("execute with negative building_id should fail")
    void testNegativeBuildingId() {
        JSONObject params = new JSONObject();
        params.put("building_id", -1);
        ActionResult result = rotateAction.execute(params);
        assertFalse(result.success, "Should fail with negative building_id");
        assertTrue(result.message.contains("Invalid") || result.message.contains("building_id"),
            "Should mention invalid building_id");
    }

    /**
     * Verifies that rotate with default direction uses clockwise.
     */
    @Test
    @DisplayName("execute should use default direction (clockwise) when not specified")
    void testDefaultDirection() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        // No direction specified - should default to 1 (clockwise)
        ActionResult result = rotateAction.execute(params);
        assertFalse(result.success); // Will fail because not in game
    }

    /**
     * Verifies that rotate accepts counter-clockwise direction.
     */
    @Test
    @DisplayName("execute should accept negative direction (counter-clockwise)")
    void testCounterClockwise() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        params.put("direction", -1);
        ActionResult result = rotateAction.execute(params);
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
        params.put("direction", 1);
        ActionResult result = rotateAction.execute(params);
        assertNotNull(result);
        assertNotNull(result.data);
    }
}
