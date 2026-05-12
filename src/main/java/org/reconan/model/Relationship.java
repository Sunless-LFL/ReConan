package org.reconan.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a relationship (edge) between two entities in the investigation graph.
 */
public class Relationship {
    private int id;
    private int investigationId;
    private int sourceId;
    private int targetId;
    private String label;
    private Map<String, String> properties;
    private LocalDateTime createdAt;

    public Relationship() {
        this.properties = new HashMap<>();
    }

    public Relationship(int sourceId, int targetId, String label) {
        this();
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(int investigationId) {
        this.investigationId = investigationId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return label;
    }
}
