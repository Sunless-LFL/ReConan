package org.reconan.model;

/**
 * Enumeration of supported entity types in ReConan.
 */
public enum EntityType {
    PERSON("Person"),
    EMAIL("Email Address"),
    PHONE("Phone Number"),
    ORGANIZATION("Organization"),
    IP_ADDRESS("IP Address"),
    DOMAIN("Domain"),
    IMAGE("Image"),
    URL("URL");

    private final String label;

    EntityType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
