package org.reconan.model;

import java.util.HashMap;
import java.util.Map;

public class Entity {
    private final EntityType type;
    private final String value;
    private final Map<String, String> metadata = new HashMap<>();

    public Entity(EntityType type, String value) {
        this.type  = type;
        this.value = value;
    }

    public EntityType getType()  { return type; }
    public String     getValue() { return value; }

    public void addMetadata(String key, String val) {
        metadata.put(key, val);
    }
    public Map<String, String> getMetadata() { return metadata; }
}
