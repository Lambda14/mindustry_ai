package aiagentmod.observation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import org.json.JSONArray;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ObservationSystem} class.
 * <p>
 * Tests the observation system coordinator which delegates to
 * individual scanners. Tests verify proper delegation and error
 * handling when Mindustry is not loaded.
 */
class ObservationSystemTest {

    private ObservationSystem observationSystem;

    /**
     * Creates a fresh ObservationSystem instance before each test.
     */
    @BeforeEach
    void setUp() {
        observationSystem = new ObservationSystem();
    }

    /**
     * Verifies that the observation system is created successfully.
     */
    @Test
    @DisplayName("ObservationSystem should be constructible")
    void testConstruction() {
        assertNotNull(observationSystem, "ObservationSystem should not be null");
    }

    /**
     * Verifies that scanFullState returns a result even when not in game.
     */
    @Test
    @DisplayName("scanFullState should return result when not in game")
    void testScanFullStateNotInGame() {
        JSONObject result = observationSystem.scanFullState();
        assertNotNull(result, "Result should not be null");
        assertTrue(result.has("status"), "Result should have status");
        assertTrue(result.has("in_game"), "Result should have in_game");
    }

    /**
     * Verifies that scanMap returns a result even when not in game.
     */
    @Test
    @DisplayName("scanMap should return result when not in game")
    void testScanMapNotInGame() {
        JSONObject result = observationSystem.scanMap();
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that scanPlayer returns a result even when not in game.
     */
    @Test
    @DisplayName("scanPlayer should return result when not in game")
    void testScanPlayerNotInGame() {
        JSONObject result = observationSystem.scanPlayer();
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that scanUnits returns a result even when not in game.
     */
    @Test
    @DisplayName("scanUnits should return result when not in game")
    void testScanUnitsNotInGame() {
        JSONObject result = observationSystem.scanUnits(null, 0, 0, 0);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.has("units"), "Result should have units");
        assertTrue(result.has("count"), "Result should have count");
    }

    /**
     * Verifies that scanAllUnits returns a result.
     */
    @Test
    @DisplayName("scanAllUnits should return result")
    void testScanAllUnits() {
        JSONObject result = observationSystem.scanAllUnits();
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that scanBuildings returns a result even when not in game.
     */
    @Test
    @DisplayName("scanBuildings should return result when not in game")
    void testScanBuildingsNotInGame() {
        JSONObject result = observationSystem.scanBuildings(null, 0, 0, 0);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.has("buildings"), "Result should have buildings");
        assertTrue(result.has("count"), "Result should have count");
    }

    /**
     * Verifies that scanAllBuildings returns a result.
     */
    @Test
    @DisplayName("scanAllBuildings should return result")
    void testScanAllBuildings() {
        JSONObject result = observationSystem.scanAllBuildings();
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that scanResources returns a result even when not in game.
     */
    @Test
    @DisplayName("scanResources should return result when not in game")
    void testScanResourcesNotInGame() {
        JSONObject result = observationSystem.scanResources(null);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that scanCores returns a result even when not in game.
     */
    @Test
    @DisplayName("scanCores should return result when not in game")
    void testScanCoresNotInGame() {
        JSONObject result = observationSystem.scanCores();
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that getSummary returns a result.
     */
    @Test
    @DisplayName("getSummary should return result")
    void testGetSummary() {
        JSONObject result = observationSystem.getSummary();
        assertNotNull(result, "Result should not be null");
        assertTrue(result.has("status"), "Result should have status");
    }

    /**
     * Verifies that getStatus returns a result.
     */
    @Test
    @DisplayName("getStatus should return result")
    void testGetStatus() {
        JSONObject result = observationSystem.getStatus();
        assertNotNull(result, "Result should not be null");
        assertTrue(result.has("in_game"), "Result should have in_game");
        assertTrue(result.has("timestamp"), "Result should have timestamp");
    }

    /**
     * Verifies that scanTile returns a result.
     */
    @Test
    @DisplayName("scanTile should return result")
    void testScanTile() {
        JSONObject result = observationSystem.scanTile(10, 10);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that scanRegion returns a result.
     */
    @Test
    @DisplayName("scanRegion should return result")
    void testScanRegion() {
        JSONObject result = observationSystem.scanRegion(50, 50, 10);
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Verifies that scanUnit returns error for non-existent unit.
     */
    @Test
    @DisplayName("scanUnit should return error for non-existent unit")
    void testScanUnitNotFound() {
        JSONObject result = observationSystem.scanUnit(-1);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.has("error") || result.has("id"), 
            "Should have error or id field");
    }

    /**
     * Verifies that scanBuilding returns error for non-existent building.
     */
    @Test
    @DisplayName("scanBuilding should return error for non-existent building")
    void testScanBuildingNotFound() {
        JSONObject result = observationSystem.scanBuilding(-1);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.has("error"), "Should have error field");
        assertEquals(-1, result.getInt("id"), "Should have the requested ID");
    }
}
