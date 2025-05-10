package com.saraci.websiteanalyzer.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Hilfsklasse für die Verarbeitung von JSON.
 * Diese verbesserte Version verwendet die Gson-Bibliothek für eine korrekte JSON-Serialisierung und -Deserialisierung.
 */
public class JsonUtil {
    private static final Logger logger = Logger.getLogger(JsonUtil.class.getName());

    // Erstelle einen benutzerdefinierten Gson-Builder mit einem TypeAdapter für LocalDateTime
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Benutzerdefinierter TypeAdapter für LocalDateTime.
     */
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(src));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }

    /**
     * Wandelt ein Objekt in JSON um.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }

        return gson.toJson(obj);
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
     * Extrahiert einen String-Wert aus einem JSON-String.
     */
    public static String getStringValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        try {
            // Versuche, den JSON-String zu parsen
            Map<?, ?> map = gson.fromJson(json, Map.class);
            Object value = map.get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            // Fallback auf Regex, wenn JSON-Parsing fehlschlägt
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
    }

    /**
     * Extrahiert einen Long-Wert aus einem JSON-String.
     */
    public static Long getLongValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        try {
            // Versuche, den JSON-String zu parsen
            Map<?, ?> map = gson.fromJson(json, Map.class);
            Object value = map.get(key);

            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.parseLong((String) value);
            }
            return null;
        } catch (Exception e) {
            // Fallback auf Regex, wenn JSON-Parsing fehlschlägt
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                try {
                    return Long.parseLong(matcher.group(1));
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Extrahiert einen Boolean-Wert aus einem JSON-String.
     */
    public static Boolean getBooleanValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        try {
            // Versuche, den JSON-String zu parsen
            Map<?, ?> map = gson.fromJson(json, Map.class);
            Object value = map.get(key);

            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
            return null;
        } catch (Exception e) {
            // Fallback auf Regex, wenn JSON-Parsing fehlschlägt
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                return Boolean.parseBoolean(matcher.group(1));
            }
            return null;
        }
    }

    /**
     * Parst einen JSON-String zu einem Map-Objekt.
     */
    public static Map<String, Object> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(json, Map.class);
            return map != null ? map : new HashMap<>();
        } catch (Exception e) {
            logger.warning("Fehler beim Parsen des JSON-Strings: " + e.getMessage());
            return new HashMap<>();
        }
    }
}