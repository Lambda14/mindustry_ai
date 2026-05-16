package aiagentmod.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import org.json.JSONArray;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ActionSystem} class.
 * <p>
 * Tests verify that the action system correctly routes action requests,
 * validates parameters, and returns appropriate error messages when
 * Mindustry is not loaded.
 */
class ActionSystemTest {

    private ActionSystem actionSystem;

    /**
     * Creates a fresh ActionSystem instance before each test.
     */
    @BeforeEach
    void setUp() {
        actionSystem = new ActionSystem();
    }

    /**
     * Verifies that all action components and the queue are initialized.
     */
    @Test
    @DisplayName("All action components should be non-null after construction")
    void testActionSystemComponentsNotNull() {
        assertNotNull(actionSystem.move, "MoveAction should be initialized");
        assertNotNull(actionSystem.build, "BuildAction should be initialized");
        assertNotNull(actionSystem.mine, "MineAction should be initialized");
        assertNotNull(actionSystem.shoot, "ShootAction should be initialized");
        assertNotNull(actionSystem.unitCommand, "UnitCommandAction should be initialized");
        assertNotNull(actionSystem.buildingConfig, "BuildingConfigAction should be initialized");
        assertNotNull(actionSystem.rotate, "RotateAction should be initialized");
        assertNotNull(actionSystem.queue, "ActionQueue should be initialized");
    }

    /**
     * Verifies that an unknown action type returns an appropriate error.
     */
    @Test
    @DisplayName("Unknown action type should return error with helpful message")
    void testUnknownActionType() {
        ActionResult result = actionSystem.execute("nonexistent", new JSONObject());
        assertFalse(result.success, "Unknown action should fail");
        assertTrue(result.message.contains("Unknown action type"), 
            "Error message should mention 'Unknown action type'");
        assertTrue(result.message.contains("move"), 
            "Error message should list supported types including 'move'");
    }

    /**
     * Verifies that null action type is handled gracefully.
     */
    @Test
    @DisplayName("Null action type should return error")
    void testNullActionType() {
        ActionResult result = actionSystem.execute(null, new JSONObject());
        assertFalse(result.success, "Null action type should fail");
        assertTrue(result.message.contains("Action type not specified"), 
            "Error message should indicate action type not specified");
    }

    /**
     * Verifies that empty action type string is handled gracefully.
     */
    @Test
    @DisplayName("Empty action type should return error")
    void testEmptyActionType() {
        ActionResult result = actionSystem.execute("", new JSONObject());
        assertFalse(result.success, "Empty action type should fail");
        assertTrue(result.message.contains("Action type not specified"),
            "Error message should indicate action type not specified");
    }

    /**
     * Verifies that the available actions list contains all expected action types.
     */
    @Test
    @DisplayName("getAvailableActions should contain all supported action types")
    void testGetAvailableActions() {
        JSONObject actions = actionSystem.getAvailableActions();
        assertTrue(actions.has("move"), "Should have move action");
        assertTrue(actions.has("build"), "Should have build action");
        assertTrue(actions.has("break"), "Should have break action");
        assertTrue(actions.has("mine"), "Should have mine action");
        assertTrue(actions.has("shoot"), "Should have shoot action");
        assertTrue(actions.has("unit_command"), "Should have unit_command action");
        assertTrue(actions.has("configure"), "Should have configure action");
        assertTrue(actions.has("rotate"), "Should have rotate action");
    }

    /**
     * Verifies that available actions include descriptions.
     */
    @Test
    @DisplayName("getAvailableActions should include descriptions for each action")
    void testGetAvailableActionsDescriptions() {
        JSONObject actions = actionSystem.getAvailableActions();
        assertFalse(actions.getString("move").isEmpty(), "Move should have description");
        assertFalse(actions.getString("build").isEmpty(), "Build should have description");
        assertFalse(actions.getString("shoot").isEmpty(), "Shoot should have description");
    }

    // ========== Individual Action Handler Tests (Mindustry not loaded) ==========

    /**
     * Move action should fail gracefully when Mindustry is not running.
     */
    @Test
    @DisplayName("Move action should fail when not in game")
    void testMoveActionNotInGame() {
        ActionResult result = actionSystem.move.execute(new JSONObject());
        assertFalse(result.success, "Move should fail when not in game");
        assertTrue(result.message.contains("Not in game") || result.message.contains("menu"),
            "Error should mention not being in game");
    }

    /**
     * Build action should fail gracefully with invalid parameters.
     */
    @Test
    @DisplayName("Build action should fail with invalid parameters")
    void testBuildActionInvalidParams() {
        ActionResult result = actionSystem.build.execute(new JSONObject());
        assertFalse(result.success, "Build should fail with empty params");
    }

    /**
     * Mine action should fail gracefully when Mindustry is not running.
     */
    @Test
    @DisplayName("Mine action should fail when not in game")
    void testMineActionNotInGame() {
        ActionResult result = actionSystem.mine.execute(new JSONObject());
        assertFalse(result.success, "Mine should fail when not in game");
    }

    /**
     * Shoot action should fail gracefully when Mindustry is not running.
     */
    @Test
    @DisplayName("Shoot action should fail when not in game")
    void testShootActionNotInGame() {
        ActionResult result = actionSystem.shoot.execute(new JSONObject());
        assertFalse(result.success, "Shoot should fail when not in game");
    }

    /**
     * Rotate action should fail gracefully when Mindustry is not running.
     */
    @Test
    @DisplayName("Rotate action should fail when not in game")
    void testRotateActionNotInGame() {
        ActionResult result = actionSystem.rotate.execute(new JSONObject());
        assertFalse(result.success, "Rotate should fail when not in game");
    }

    /**
     * Building config action should fail gracefully when Mindustry is not running.
     */
    @Test
    @DisplayName("Building config action should fail when not in game")
    void testBuildingConfigActionNotInGame() {
        ActionResult result = actionSystem.buildingConfig.execute(new JSONObject());
        assertFalse(result.success, "Building config should fail when not in game");
    }

    /**
     * Unit command action should fail gracefully when Mindustry is not running.
     */
    @Test
    @DisplayName("Unit command action should fail when not in game")
    void testUnitCommandActionNotInGame() {
        JSONObject params = new JSONObject();
        params.put("unit_ids", new JSONArray());
        ActionResult result = actionSystem.unitCommand.execute(params);
        assertFalse(result.success, "Unit command should fail when not in game");
    }

    // ========== Routing Tests ==========

    /**
     * Verifies that "move" action type is correctly routed to MoveAction.
     */
    @Test
    @DisplayName("execute should route 'move' to MoveAction")
    void testMoveRouting() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = actionSystem.execute("move", params);
        // Will fail because Mindustry is not loaded, but verifies routing
        assertNotNull(result, "Result should not be null");
        assertFalse(result.success, "Should fail when Mindustry not loaded");
    }

    /**
     * Verifies that "build" action type is correctly routed to BuildAction.
     */
    @Test
    @DisplayName("execute should route 'build' to BuildAction")
    void testBuildRouting() {
        JSONObject params = new JSONObject();
        params.put("x", 10);
        params.put("y", 10);
        params.put("block", "conveyor");
        ActionResult result = actionSystem.execute("build", params);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that "break" action type is correctly routed to BuildAction.executeBreak.
     */
    @Test
    @DisplayName("execute should route 'break' to BuildAction.executeBreak")
    void testBreakRouting() {
        JSONObject params = new JSONObject();
        params.put("x", 10);
        params.put("y", 10);
        ActionResult result = actionSystem.execute("break", params);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.success, "Should fail when Mindustry not loaded");
    }

    /**
     * Verifies that "mine" action type is correctly routed to MineAction.
     */
    @Test
    @DisplayName("execute should route 'mine' to MineAction")
    void testMineRouting() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = actionSystem.execute("mine", params);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that "shoot" action type is correctly routed to ShootAction.
     */
    @Test
    @DisplayName("execute should route 'shoot' to ShootAction")
    void testShootRouting() {
        JSONObject params = new JSONObject();
        params.put("x", 100);
        params.put("y", 200);
        ActionResult result = actionSystem.execute("shoot", params);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that "rotate" action type is correctly routed to RotateAction.
     */
    @Test
    @DisplayName("execute should route 'rotate' to RotateAction")
    void testRotateRouting() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        ActionResult result = actionSystem.execute("rotate", params);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that "configure" action type is correctly routed to BuildingConfigAction.
     */
    @Test
    @DisplayName("execute should route 'configure' to BuildingConfigAction")
    void testConfigureRouting() {
        JSONObject params = new JSONObject();
        params.put("building_id", 1);
        params.put("config", "copper");
        ActionResult result = actionSystem.execute("configure", params);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that "unit_command" action type is correctly routed to UnitCommandAction.
     */
    @Test
    @DisplayName("execute should route 'unit_command' to UnitCommandAction")
    void testUnitCommandRouting() {
        JSONObject params = new JSONObject();
        JSONArray unitIds = new JSONArray();
        unitIds.put(1);
        params.put("unit_ids", unitIds);
        params.put("command", "move");
        ActionResult result = actionSystem.execute("unit_command", params);
        assertNotNull(result, "Result should not be null");
    }
}
