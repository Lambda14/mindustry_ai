package aiagentmod.api;

import java.util.*;

/**
 * Minimal key-value map for parsing JSON-like parameters.
 * Compatible with Java 25 modular system.
 * No external dependencies.
 */
public class SimpleMap {
    
    private final HashMap<String, String> map = new HashMap<>();
    
    public static SimpleMap parse(String json) {
        SimpleMap m = new SimpleMap();
        if (json == null || json.trim().isEmpty()) return m;
        
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        
        String key = null;
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escapeNext) {
                current.append(c);
                escapeNext = false;
                continue;
            }
            
            if (c == '\\') {
                escapeNext = true;
                current.append(c);
                continue;
            }
            
            if (c == '"') {
                inQuotes = !inQuotes;
                if (inQuotes && key != null) {
                    // We're reading a string value
                    current.append(c);
                } else if (!inQuotes && key != null) {
                    // End of string value
                    current.append(c);
                } else {
                    current.append(c);
                }
                continue;
            }
            
            if (!inQuotes && c == ':' && key == null) {
                // End of key
                String rawKey = current.toString().trim();
                key = unquote(rawKey);
                current = new StringBuilder();
                continue;
            }
            
            if (!inQuotes && c == ',') {
                if (key != null) {
                    m.map.put(key, unquote(current.toString().trim()));
                    key = null;
                }
                current = new StringBuilder();
                continue;
            }
            
            current.append(c);
        }
        
        // Last pair
        if (key != null) {
            m.map.put(key, unquote(current.toString().trim()));
        }
        
        return m;
    }
    
    private static String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        if (s.startsWith("\"") && s.length() > 1) {
            return s.substring(1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return s;
    }
    
    public String optString(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }
    
    public int optInt(String key, int defaultValue) {
        String v = map.get(key);
        if (v == null) return defaultValue;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public float optFloat(String key, float defaultValue) {
        String v = map.get(key);
        if (v == null) return defaultValue;
        try {
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public boolean optBoolean(String key, boolean defaultValue) {
        String v = map.get(key);
        if (v == null) return defaultValue;
        return v.equals("true");
    }
    
    public String get(String key) {
        return map.get(key);
    }
    
    public boolean has(String key) {
        return map.containsKey(key);
    }
    
    public void put(String key, String value) {
        map.put(key, value);
    }
    
    public Set<String> keys() {
        return map.keySet();
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
}
