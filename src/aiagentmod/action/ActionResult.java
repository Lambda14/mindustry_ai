package aiagentmod.action;

/**
 * Represents the result of an action execution.
 * Uses String-based JSON (no external dependencies).
 */
public class ActionResult {
    
    public final boolean success;
    public final String message;
    public final String data;
    
    public ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = "{}";
    }
    
    public ActionResult(boolean success, String message, String data) {
        this.success = success;
        this.message = message;
        this.data = data != null ? data : "{}";
    }
    
    /**
     * Converts the result to JSON string.
     */
    public String toJson() {
        return "{\"success\":" + success
            + ",\"message\":\"" + escape(message) + "\""
            + ",\"data\":" + data
            + ",\"timestamp\":" + System.currentTimeMillis()
            + "}";
    }
    
    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }
    
    public static ActionResult ok(String message, String data) {
        return new ActionResult(true, message, data);
    }
    
    public static ActionResult error(String message) {
        return new ActionResult(false, message);
    }
    
    public static ActionResult error(String message, String data) {
        return new ActionResult(false, message, data);
    }
    
    private static String escape(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
