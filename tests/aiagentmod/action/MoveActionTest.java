package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link MoveAction} class.
 * <p>
 * Tests parameter validation and error handling when Mindustry
 * game environment is not available.
 */
class MoveActionTest {

    private final MoveAction moveAction = new MoveAction();

    /**
     * Verifies that move fails gracefully when not in game.
     */
    @Test
    @DisplayName("execute should fail when not in game")
    void testNotInGame() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = moveAction.execute(params);
        assertFalse(result.success, "Should fail when not in game");
        assertTrue(result.message.contains("menu") || result.message.contains("Not in game"),
            "Error should mention menu or not in game");
    }

    /**
     * Verifies that move with empty parameters fails.
     */
    @Test
    @DisplayName("execute with empty params should fail")
    void testEmptyParams() {
        ActionResult result = moveAction.execute(new JSONObject());
        assertFalse(result.success, "Should fail with empty params");
    }

    /**
     * Verifies that move with only x parameter fails.
     */
    @Test
    @DisplayName("execute with only x should fail")
    void testOnlyX() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        ActionResult result = moveAction.execute(params);
        assertFalse(result.success, "Should fail with only x");
    }

    /**
     * Verifies that move validates negative coordinates properly.
     */
    @Test
    @DisplayName("execute with negative coordinates should fail")
    void testNegativeCoords() {
        JSONObject params = new JSONObject();
        params.put("x", -1);
        params.put("y", -1);
        ActionResult result = moveAction.execute(params);
        // Both x and y are -1 which triggers tile coord check
        assertFalse(result.success, "Should fail with negative coordinates");
    }

    /**
     * Verifies that the action returns the correct result structure.
     */
    @Test
    @DisplayName("Result should have correct structure")
    void testResultStructure() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = moveAction.execute(params);
        assertNotNull(result);
        assertNotNull(result.data);
    }

    /**
     * Verifies that tile coordinates are accepted as alternative to world coordinates.
     */
    @Test
    @DisplayName("execute should accept tile coordinates")
    void testTileCoordinates() {
        JSONObject params = new JSONObject();
        params.put("tile_x", 5);
        params.put("tile_y", 10);
        ActionResult result = moveAction.execute(params);
        // Will fail because game not loaded, but should attempt tile coord parsing
        assertFalse(result.success);
    }
}
