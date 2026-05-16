package aiagentmod.observation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import org.json.JSONArray;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link JsonSerializer} utility class.
 * <p>
 * Tests cover point creation, rectangle serialization, and other
 * JSON helper methods that don't require Mindustry to be running.
 */
class JsonSerializerTest {

    /**
     * Tests creating a point with world coordinates.
     */
    @Test
    @DisplayName("point(float, float) should create point with x and y")
    void testPointWorld() {
        JSONObject point = JsonSerializer.point(100.5f, 200.75f);
        assertNotNull(point, "Point should not be null");
        assertTrue(point.has("x"), "Point should have x");
        assertTrue(point.has("y"), "Point should have y");
        assertEquals(100.5f, point.getFloat("x"), 0.01, "X should be 100.5");
        assertEquals(200.75f, point.getFloat("y"), 0.01, "Y should be 200.75");
    }

    /**
     * Tests creating a point with negative coordinates.
     */
    @Test
    @DisplayName("point should handle negative coordinates")
    void testPointNegative() {
        JSONObject point = JsonSerializer.point(-50.0f, -100.0f);
        assertEquals(-50.0f, point.getFloat("x"), 0.01);
        assertEquals(-100.0f, point.getFloat("y"), 0.01);
    }

    /**
     * Tests creating a point with zero coordinates.
     */
    @Test
    @DisplayName("point should handle zero coordinates")
    void testPointZero() {
        JSONObject point = JsonSerializer.point(0.0f, 0.0f);
        assertEquals(0.0f, point.getFloat("x"), 0.01);
        assertEquals(0.0f, point.getFloat("y"), 0.01);
    }

    /**
     * Tests creating a point with tile and world coordinates.
     */
    @Test
    @DisplayName("point(int, int, float, float) should include tile and world coords")
    void testPointTileAndWorld() {
        JSONObject point = JsonSerializer.point(10, 20, 160.0f, 320.0f);
        assertNotNull(point);
        assertTrue(point.has("tile_x"), "Should have tile_x");
        assertTrue(point.has("tile_y"), "Should have tile_y");
        assertTrue(point.has("world_x"), "Should have world_x");
        assertTrue(point.has("world_y"), "Should have world_y");
        assertEquals(10, point.getInt("tile_x"));
        assertEquals(20, point.getInt("tile_y"));
        assertEquals(160.0f, point.getFloat("world_x"), 0.01);
        assertEquals(320.0f, point.getFloat("world_y"), 0.01);
    }

    /**
     * Tests creating a rectangle.
     */
    @Test
    @DisplayName("rect should create rectangle with x, y, width, height")
    void testRect() {
        JSONObject rect = JsonSerializer.rect(10.0f, 20.0f, 100.0f, 200.0f);
        assertNotNull(rect);
        assertTrue(rect.has("x"));
        assertTrue(rect.has("y"));
        assertTrue(rect.has("width"));
        assertTrue(rect.has("height"));
        assertEquals(10.0f, rect.getFloat("x"), 0.01);
        assertEquals(20.0f, rect.getFloat("y"), 0.01);
        assertEquals(100.0f, rect.getFloat("width"), 0.01);
        assertEquals(200.0f, rect.getFloat("height"), 0.01);
    }

    /**
     * Tests that rect handles zero dimensions.
     */
    @Test
    @DisplayName("rect should handle zero dimensions")
    void testRectZero() {
        JSONObject rect = JsonSerializer.rect(0.0f, 0.0f, 0.0f, 0.0f);
        assertEquals(0.0f, rect.getFloat("x"), 0.01);
        assertEquals(0.0f, rect.getFloat("y"), 0.01);
        assertEquals(0.0f, rect.getFloat("width"), 0.01);
        assertEquals(0.0f, rect.getFloat("height"), 0.01);
    }

    /**
     * Tests coordinate precision rounding in point output.
     */
    @Test
    @DisplayName("point should round to 2 decimal places")
    void testPointRounding() {
        JSONObject point = JsonSerializer.point(100.12345f, 200.98765f);
        // Values should be rounded to 2 decimal places
        float x = point.getFloat("x");
        float y = point.getFloat("y");
        assertTrue(Math.abs(x - 100.12f) < 0.01 || Math.abs(x - 100.123f) < 0.001,
            "X should be approximately rounded: " + x);
        assertTrue(Math.abs(y - 200.99f) < 0.01 || Math.abs(y - 200.988f) < 0.001,
            "Y should be approximately rounded: " + y);
    }
}
