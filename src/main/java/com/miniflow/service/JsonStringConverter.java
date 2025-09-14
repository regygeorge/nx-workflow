package com.miniflow.service;

// Converter: Object <-> JSON text
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class JsonStringConverter implements AttributeConverter<Object, String> {
    private static final ObjectMapper M = new ObjectMapper();
    @Override public String convertToDatabaseColumn(Object attribute) {
        try { return attribute == null ? null : M.writeValueAsString(attribute); }
        catch (Exception e) { throw new IllegalStateException("JSON serialize failed", e); }
    }
    @Override public Object convertToEntityAttribute(String dbData) {
        try { return dbData == null ? null : M.readValue(dbData, Object.class); }
        catch (Exception e) { throw new IllegalStateException("JSON parse failed", e); }
    }
}
