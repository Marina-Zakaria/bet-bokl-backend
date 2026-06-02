package com.bokl.homerental.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return MAPPER.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize JSON", e);
        }
    }
}
