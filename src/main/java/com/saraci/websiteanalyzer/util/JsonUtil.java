package com.saraci.websiteanalyzer.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hilfsklasse für die Verarbeitung von JSON.
 * Diese einfache Implementierung dient nur zu Demonstrationszwecken.
 * In einer realen Anwendung sollte eine umfassendere JSON-Bibliothek wie Jackson oder Gson verwendet werden.
 */
public class JsonUtil {

    /**
     * Wandelt ein Objekt in JSON um.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof String) {
            return "\"" + escapeJsonString((String) obj) + "\"";
        }

        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder json = new StringBuilder("{");
            boolean first = true;

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                first = false;

                json.append("\"").append(entry.getKey()).append("\":");
                json.append(toJson(entry.getValue()));
            }

            json.append("}");
            return json.toString();
        }

        if (obj instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) obj;
            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            for (Object item : iterable) {
                if (!first) {
                    json.append(",");
                }
                first = false;

                json.append(toJson(item));
            }

            json.append("]");
            return json.toString();
        }

        // Für andere Objekte verwenden wir eine einfache toString-Methode
        return "\"" + escapeJsonString(obj.toString()) + "\"";
    }

    /**
     * Erzeugt eine einfache Erfolgsmeldung.
     */
    public static Map<String, Object> success(String message, Object... keyValues) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);

        // Zusätzliche key-value Paare hinzufügen
        if (keyValues != null && keyValues.length % 2 == 0) {
            for (int i = 0; i < keyValues.length; i += 2) {
                if (keyValues[i] instanceof String) {
                    result.put((String) keyValues[i], keyValues[i + 1]);
                }
            }
        }

        return result;
    }

    /**
     * Erzeugt eine einfache Fehlermeldung.
     */
    public static Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return result;
    }

    /**
     * Entfernt Sonderzeichen aus einem JSON-String.
     */
    private static String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }

        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Extrahiert einen String-Wert aus einem JSON-String.
     */
    public static String getStringValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Extrahiert einen Long-Wert aus einem JSON-String.
     */
    public static Long getLongValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Extrahiert einen Boolean-Wert aus einem JSON-String.
     */
    public static Boolean getBooleanValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }

        return null;
    }
}