package com.miniflow.avro;

import org.apache.avro.Schema;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
@Component
public class AvroSchemas {
    public static Schema loadSchema(String resourcePath) {
        try (InputStream in = AvroSchemas.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Schema file not found: " + resourcePath);
            }
            return new Schema.Parser().parse(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Avro schema: " + resourcePath, e);
        }
    }
}
