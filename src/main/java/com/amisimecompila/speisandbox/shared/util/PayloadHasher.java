package com.amisimecompila.speisandbox.shared.util;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class PayloadHasher {

    private final ObjectMapper canonicalMapper;

    public PayloadHasher(ObjectMapper applicationMapper) {

        this.canonicalMapper = JsonMapper.builder(applicationMapper.getFactory())
                .findAndAddModules()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .build();

    }
    public String calcular(Object value) {
        try {
            byte[] json = canonicalMapper.writeValueAsString(value)
                    .getBytes(StandardCharsets.UTF_8);
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(json);
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No fue posible calcular el hash canónico",
                    exception
            );
        }
    }
}
