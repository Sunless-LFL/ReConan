package org.reconan.service;

import org.reconan.model.Entity;
import org.reconan.transform.Transform;
import org.reconan.transform.TransformException;
import org.reconan.transform.TransformRegistry;

import java.util.ArrayList;
import java.util.List;

public class InvestigationService {

    private final TransformRegistry registry = new TransformRegistry();

    public List<Entity> enrich(Entity entity) {
        List<Transform> transforms = registry.getTransforms(entity.getType());
        List<Entity>    discovered = new ArrayList<>();

        for (Transform t : transforms) {
            try {
                List<Entity> results = t.execute(entity);
                discovered.addAll(results);
                System.out.printf("[%s] %s -> %d result(s)%n",
                    t.getName(), entity.getValue(), results.size());
            } catch (TransformException e) {
                System.err.printf("[%s] ERROR: %s%n", t.getName(), e.getMessage());
            }
        }
        return discovered;
    }
}
