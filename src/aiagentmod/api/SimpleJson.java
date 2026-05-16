package aiagentmod.api;

import java.util.*;

/**
 * Minimal JSON serializer without external dependencies.
 * Compatible with Java 25 modular system.
 */
public class SimpleJson {
    
    private final StringBuilder sb = new StringBuilder();
    private boolean first = true;
    
    public static String obj(String... pairs) {
        StringBuilder b = new StringBuilder("{");
        for (int i = 0; i < pairs.length; i += 2) {
            if (i > 0) b.append(",");
            b.append("\"").append(escape(pairs[i])).append("\":").append(pairs[i + 1]);
        }
        b.append("}");
        return b.toString();
    }
    
    public static String arr(String... items) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < items.length; i++) {
            if (i > 0) b.append(",");
            b.append(items[i]);
        }
        b.append("]");
        return b.toString();
    }
    
    public static String str(String s) {
        if (s == null) return "null";
        return "\"" + escape(s) + "\"";
    }
    
    public static String num(Number n) {
        return n != null ? n.toString() : "0";
    }
    
    public static String bool(boolean b) {
        return b ? "true" : "false";
    }
    
    public static String nullv() {
        return "null";
    }
    
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    // ===== Object builder =====
    
    public static SimpleJson object() {
        return new SimpleJson();
    }
    
    public SimpleJson put(String key, String value) {
        comma();
        sb.append("\"").append(escape(key)).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append("\"").append(escape(value)).append("\"");
        }
        return this;
    }
    
    public SimpleJson put(String key, Number value) {
        comma();
        sb.append("\"").append(escape(key)).append("\":");
        sb.append(value != null ? value.toString() : "0");
        return this;
    }
    
    public SimpleJson put(String key, boolean value) {
        comma();
        sb.append("\"").append(escape(key)).append("\":").append(value);
        return this;
    }
    
    public SimpleJson putRaw(String key, String rawValue) {
        comma();
        sb.append("\"").append(escape(key)).append("\":");
        sb.append(rawValue != null ? rawValue : "null");
        return this;
    }
    
    public SimpleJson putArray(String key, String arrayContent) {
        comma();
        sb.append("\"").append(escape(key)).append("\":");
        sb.append(arrayContent != null ? arrayContent : "[]");
        return this;
    }
    
    public SimpleJson putObject(String key, String objectContent) {
        comma();
        sb.append("\"").append(escape(key)).append("\":");
        sb.append(objectContent != null ? objectContent : "{}");
        return this;
    }
    
    private void comma() {
        if (!first) sb.append(",");
        first = false;
    }
    
    @Override
    public String toString() {
        return "{" + sb.toString() + "}";
    }
    
    public String toArray() {
        return "[" + sb.toString() + "]";
    }
    
    // ===== Response helpers =====
    
    public static String okResponse(String message, String data) {
        return "{\"success\":true,\"message\":" + str(message) + ",\"data\":" + (data != null ? data : "{}") + ",\"timestamp\":" + System.currentTimeMillis() + "}";
    }
    
    public static String errorResponse(String message) {
        return "{\"success\":false,\"message\":" + str(message) + ",\"data\":{},\"timestamp\":" + System.currentTimeMillis() + "}";
    }
    
    public static String notFound() {
        return "{\"error\":\"Not found\",\"available\":[\"/api/state\",\"/api/map\",\"/api/player\",\"/api/units\",\"/api/buildings\",\"/api/resources\",\"/api/action/*\"]}";
    }
}
