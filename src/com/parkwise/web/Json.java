package com.parkwise.web;

import java.util.*;

/**
 * Lightweight JSON builder — no external library needed.
 */
public class Json {

    public static String obj(Object... keyValues) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(keyValues[i]).append("\":");
            Object val = keyValues[i + 1];
            sb.append(toJson(val));
        }
        sb.append("}");
        return sb.toString();
    }

    public static String arr(List<?> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            Object item = items.get(i);
            // If item is a raw JSON string (object or array), don't re-quote it
            if (item instanceof String) {
                String s = ((String) item).trim();
                if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
                    sb.append(s);
                    continue;
                }
            }
            sb.append(toJson(item));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String toJson(Object val) {
        if (val == null) return "null";
        if (val instanceof Number || val instanceof Boolean) return val.toString();
        String s = val.toString();
        // If it's already a raw JSON object or array, don't wrap in quotes
        String trimmed = s.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return s;
        }
        return "\"" + escape(s) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Parse a simple flat JSON body (single level key:value pairs) */
    public static Map<String, String> parseBody(String body) {
        Map<String, String> map = new LinkedHashMap<>();
        if (body == null || body.isBlank()) return map;
        body = body.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        // Split by comma but not inside strings
        List<String> pairs = splitPairs(body);
        for (String pair : pairs) {
            int colon = pair.indexOf(':');
            if (colon < 0) continue;
            String key = pair.substring(0, colon).trim().replaceAll("^\"|\"$", "");
            String value = pair.substring(colon + 1).trim().replaceAll("^\"|\"$", "");
            map.put(key, value);
        }
        return map;
    }

    private static List<String> splitPairs(String s) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inString = !inString;
            else if (!inString && (c == '{' || c == '[')) depth++;
            else if (!inString && (c == '}' || c == ']')) depth--;
            else if (!inString && depth == 0 && c == ',') {
                result.add(s.substring(start, i));
                start = i + 1;
            }
        }
        result.add(s.substring(start));
        return result;
    }
}
