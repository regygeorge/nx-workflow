package com.miniflow.util;

import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for Avro serialization and deserialization
 */
public class AvroSerializationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AvroSerializationUtil.class);
    
    /**
     * Serialize an Avro record to a byte buffer
     * 
     * @param record The Avro record to serialize
     * @return ByteBuffer containing the serialized data
     */
    public static ByteBuffer serializeToByteBuffer(SpecificRecord record) {
        try {
            // Use reflection to call toByteBuffer() method on any SpecificRecord
            return (ByteBuffer) record.getClass().getMethod("toByteBuffer").invoke(record);
        } catch (Exception e) {
            logger.error("Error serializing Avro record", e);
            throw new RuntimeException("Failed to serialize Avro record", e);
        }
    }
    
    /**
     * Deserialize a byte buffer to a specific Avro record type
     * 
     * @param buffer ByteBuffer containing serialized data
     * @param recordClass The class of the Avro record
     * @return Deserialized Avro record
     */
    @SuppressWarnings("unchecked")
    public static <T extends SpecificRecord> T deserializeRecord(ByteBuffer buffer, Class<T> recordClass) {
        try {
            // Use reflection to call fromByteBuffer() static method
            return (T) recordClass.getMethod("fromByteBuffer", ByteBuffer.class)
                    .invoke(null, buffer);
        } catch (Exception e) {
            logger.error("Error deserializing Avro record", e);
            throw new RuntimeException("Failed to deserialize Avro record", e);
        }
    }
    
    /**
     * Convert a Map<String, Object> to a format compatible with Avro
     */
    public static Map<CharSequence, Object> convertVariables(Map<String, Object> variables) {
        if (variables == null) {
            return null;
        }
        
        Map<CharSequence, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();
            // Only include primitive types and strings
            if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
                result.put(entry.getKey(), value);
            } else {
                // Convert complex objects to string representation
                result.put(entry.getKey(), value.toString());
            }
        }
        return result;
    }
}

// Made with Bob
