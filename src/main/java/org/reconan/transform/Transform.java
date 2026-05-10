package org.reconan.transform;

import org.reconan.model.Entity;
import org.reconan.model.EntityType;
import java.util.List;

public interface Transform {
    String getName();
    EntityType getInputType();
    EntityType getOutputType();
    List<Entity> execute(Entity input) throws TransformException;
}
