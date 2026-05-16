package aiagentmod.action;

import org.json.JSONObject;

/**
 * Represents the result of an action execution.
 * Contains success status, message, and optional data.
 */
public class ActionResult {
    
    /** Whether the action succeeded */
    public final boolean success;
    
    /** Human-readable message */
    public final String message;
    
    /** Additional data returned by the action */
    public final JSONObject data;
    
    public ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = new JSONObject();
    }
    
    public ActionResult(boolean success, String message, JSONObject data) {
        this.success = success;
        this.message = message;
        this.data = data != null ? data : new JSONObject();
    }
    
    /**
     * Converts the result to JSON for API response.
     */
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * Creates a successful result.
     */
    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }
    
    /**
     * Creates a successful result with data.
     */
    public static ActionResult ok(String message, JSONObject data) {
        return new ActionResult(true, message, data);
    }
    
    /**
     * Creates an error result.
     */
    public static ActionResult error(String message) {
        return new ActionResult(false, message);
    }
    
    /**
     * Creates an error result with additional data.
     */
    public static ActionResult error(String message, JSONObject data) {
        return new ActionResult(false, message, data);
    }
}
