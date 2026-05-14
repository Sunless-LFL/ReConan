package org.reconan.transform;

import org.reconan.model.EntityType;
import org.reconan.transform.impl.*;

import java.util.*;

public class TransformRegistry {

    private final Map<EntityType, List<Transform>> registry = new HashMap<>();

    public TransformRegistry() {
        register(EntityType.IP_ADDRESS,     new ShodanTransform());
        register(EntityType.DOMAIN, new HunterTransform());
        register(EntityType.EMAIL,  new HunterEmailVerifyTransform());
        register(EntityType.PERSON, new HunterEmailFinderTransform());
    }

    public void register(EntityType type, Transform transform) {
        registry.computeIfAbsent(type, k -> new ArrayList<>()).add(transform);
    }

    public List<Transform> getTransforms(EntityType type) {
        return registry.getOrDefault(type, Collections.emptyList());
    }
}
