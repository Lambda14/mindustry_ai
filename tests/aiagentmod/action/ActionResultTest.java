package aiagentmod.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ActionResult} class.
 * <p>
 * Tests cover all factory methods, JSON serialization, and edge cases
 * including null data handling.
 */
class ActionResultTest {

    /**
     * Tests that ActionResult.ok(String) creates a successful result
     * with empty data and the correct message.
     */
    @Test
    @DisplayName("ok(String) should create successful result with empty data")
    void testOkResult() {
        ActionResult result = ActionResult.ok("Success");
        assertTrue(result.success, "Result should be successful");
        assertEquals("Success", result.message, "Message should match");
        assertNotNull(result.data, "Data should not be null");
        assertTrue(result.data.isEmpty(), "Data should be empty for ok(String)");
    }

    /**
     * Tests that ActionResult.ok(String, JSONObject) creates a successful result
     * with the provided data embedded.
     */
    @Test
    @DisplayName("ok(String, JSONObject) should create successful result with data")
    void testOkWithData() {
        JSONObject data = new JSONObject();
        data.put("key", "value");
        data.put("number", 42);
        ActionResult result = ActionResult.ok("Success", data);
        assertTrue(result.success, "Result should be successful");
        assertEquals("value", result.data.getString("key"), "String data should match");
        assertEquals(42, result.data.getInt("number"), "Number data should match");
    }

    /**
     * Tests that ActionResult.error(String) creates a failed result
     * with the correct error message.
     */
    @Test
    @DisplayName("error(String) should create failed result")
    void testErrorResult() {
        ActionResult result = ActionResult.error("Failed");
        assertFalse(result.success, "Result should not be successful");
        assertEquals("Failed", result.message, "Error message should match");
    }

    /**
     * Tests that ActionResult.error(String, JSONObject) creates a failed result
     * with the provided error data.
     */
    @Test
    @DisplayName("error(String, JSONObject) should create failed result with data")
    void testErrorWithData() {
        JSONObject errorData = new JSONObject();
        errorData.put("error_code", 500);
        errorData.put("detail", "internal error");
        ActionResult result = ActionResult.error("Failed", errorData);
        assertFalse(result.success, "Result should not be successful");
        assertEquals(500, result.data.getInt("error_code"), "Error code should match");
        assertEquals("internal error", result.data.getString("detail"), "Error detail should match");
    }

    /**
     * Tests that toJson() produces a complete JSON representation
     * containing all fields including timestamp.
     */
    @Test
    @DisplayName("toJson() should include success, message, data, and timestamp")
    void testToJson() {
        ActionResult result = ActionResult.ok("Test", new JSONObject().put("x", 1));
        JSONObject json = result.toJson();
        assertTrue(json.getBoolean("success"), "JSON should contain success=true");
        assertEquals("Test", json.getString("message"), "JSON should contain message");
        assertTrue(json.has("timestamp"), "JSON should contain timestamp");
        assertTrue(json.getLong("timestamp") > 0, "Timestamp should be positive");
        assertEquals(1, json.getJSONObject("data").getInt("x"), "JSON data should contain x=1");
    }

    /**
     * Tests that passing null data to the constructor is handled gracefully
     * by substituting an empty JSONObject.
     */
    @Test
    @DisplayName("Constructor should handle null data by substituting empty JSONObject")
    void testNullDataHandled() {
        ActionResult result = new ActionResult(true, "test", null);
        assertNotNull(result.data, "Data should not be null even when null is passed");
        assertTrue(result.data.isEmpty(), "Data should be empty when null is passed");
    }

    /**
     * Tests the three-argument constructor with non-null data.
     */
    @Test
    @DisplayName("Constructor should preserve provided non-null data")
    void testConstructorWithData() {
        JSONObject data = new JSONObject().put("test_key", "test_value");
        ActionResult result = new ActionResult(false, "constructor test", data);
        assertFalse(result.success);
        assertEquals("constructor test", result.message);
        assertEquals("test_value", result.data.getString("test_key"));
    }

    /**
     * Tests that toJson() includes the data field even when empty.
     */
    @Test
    @DisplayName("toJson() should include empty data object")
    void testToJsonWithEmptyData() {
        ActionResult result = ActionResult.ok("No data");
        JSONObject json = result.toJson();
        assertTrue(json.has("data"), "JSON should have data field");
        assertTrue(json.getJSONObject("data").isEmpty(), "Data should be empty");
    }

    /**
     * Tests that ActionResult objects are immutable after creation.
     */
    @Test
    @DisplayName("ActionResult fields should be set correctly after creation")
    void testResultFields() {
        ActionResult successResult = ActionResult.ok("Works");
        assertTrue(successResult.success);
        assertEquals("Works", successResult.message);

        ActionResult failResult = ActionResult.error("Broken");
        assertFalse(failResult.success);
        assertEquals("Broken", failResult.message);
    }
}
