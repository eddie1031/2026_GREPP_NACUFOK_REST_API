package io.eddie.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class JsonUtil {

    public static final ObjectMapper om = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static ObjectNode obj(Object... keyValues) {
        ObjectNode node = om.createObjectNode();
        for (int i = 0; i < keyValues.length; i += 2) {
            node.set((String) keyValues[i], toNode(keyValues[i + 1]));
        }
        return node;
    }

    public static ArrayNode arr(Object... items) {
        ArrayNode node = om.createArrayNode();
        for (Object item : items) {
            node.add(toNode(item));
        }
        return node;
    }

    public static JsonNode parse(String json) {
        try {
            return om.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 파싱 실패: " + json, e);
        }
    }

    public static <T> T parse(String json, Class<T> type) {
        try {
            return om.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 파싱 실패: " + json, e);
        }
    }

    public static String toJson(Object value) {
        try {
            return om.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String pretty(Object value) {
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static JsonNode toNode(Object value) {
        if (value instanceof JsonNode) return (JsonNode) value;
        return om.valueToTree(value);
    }

}
