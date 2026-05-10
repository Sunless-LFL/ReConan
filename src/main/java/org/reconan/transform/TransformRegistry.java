package org.reconan.transform;

import org.reconan.model.EntityType;
import org.reconan.transform.impl.*;

import java.util.*;

public class TransformRegistry {

    private final Map<EntityType, List<Transform>> registry = new HashMap<>();

    public TransformRegistry() {
        register(EntityType.IP,       new IpInfoTransform());
        register(EntityType.IP,       new ShodanTransform());
        register(EntityType.IP,       new AbuseIpDbTransform());
        register(EntityType.IP,       new CensysTransform());
        register(EntityType.DOMAIN,   new VirusTotalDomainTransform());
        register(EntityType.DOMAIN,   new SecurityTrailsTransform());
        register(EntityType.DOMAIN,   new HunterTransform());
        register(EntityType.EMAIL,    new HaveIBeenPwnedTransform());
        register(EntityType.USERNAME, new GitHubTransform());
        register(EntityType.URL,      new UrlScanTransform());
    }

    public void register(EntityType type, Transform transform) {
        registry.computeIfAbsent(type, k -> new ArrayList<>()).add(transform);
    }

    public List<Transform> getTransforms(EntityType type) {
        return registry.getOrDefault(type, Collections.emptyList());
    }
}
