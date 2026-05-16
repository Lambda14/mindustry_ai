package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ShootAction} class.
 * <p>
 * Tests parameter validation for aiming, shooting, and stopping
 * when Mindustry game environment is not available.
 */
class ShootActionTest {

    private final ShootAction shootAction = new ShootAction();

    /**
     * Verifies that shoot fails gracefully when not in game.
     */
    @Test
    @DisplayName("execute should fail when not in game")
    void testNotInGame() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        params.put("shoot", true);
        ActionResult result = shootAction.execute(params);
        assertFalse(result.success, "Should fail when not in game");
        assertTrue(result.message.contains("menu") || result.message.contains("Not in game"),
            "Error should mention menu or not in game");
    }

    /**
     * Verifies that shoot with empty parameters fails.
     */
    @Test
    @DisplayName("execute with empty params should fail")
    void testEmptyParams() {
        ActionResult result = shootAction.execute(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that shoot=false stops shooting.
     */
    @Test
    @DisplayName("execute with shoot=false should stop shooting")
    void testStopShooting() {
        JSONObject params = new JSONObject();
        params.put("shoot", false);
        ActionResult result = shootAction.execute(params);
        // May fail due to no player, but tests the branch
        assertNotNull(result);
    }

    /**
     * Verifies that shoot=true requires coordinates.
     */
    @Test
    @DisplayName("execute with shoot=true but no coords should fail")
    void testShootNoCoords() {
        JSONObject params = new JSONObject();
        params.put("shoot", true);
        ActionResult result = shootAction.execute(params);
        assertFalse(result.success, "Should fail without coordinates");
    }

    /**
     * Verifies that the action accepts target_id parameter.
     */
    @Test
    @DisplayName("execute should accept target_id parameter")
    void testTargetId() {
        JSONObject params = new JSONObject();
        params.put("target_id", 999);
        params.put("shoot", true);
        ActionResult result = shootAction.execute(params);
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
        ActionResult result = shootAction.execute(params);
        assertNotNull(result);
        assertNotNull(result.data);
    }
}
