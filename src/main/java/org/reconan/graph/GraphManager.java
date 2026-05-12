package org.reconan.graph;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;
import javafx.scene.layout.Pane;
import org.reconan.model.Entity;
import org.reconan.model.Relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the investigation graph logic and its visual representation via SmartGraph.
 */
public class GraphManager {

    private final Digraph<Entity, Relationship> graph;
    private final Map<Integer, Entity> entityMap;
    private SmartGraphPanel<Entity, Relationship> graphView;

    public GraphManager() {
        this.graph = new DigraphEdgeList<>();
        this.entityMap = new HashMap<>();
    }

    /**
     * Initializes the visual graph panel.
     *
     * @param container The JavaFX Pane where the graph will be displayed.
     */
    public void initializeView(Pane container) {
        SmartPlacementStrategy strategy = new SmartRandomPlacementStrategy();
        this.graphView = new SmartGraphPanel<>(graph, strategy);

        // Bind view size to container size
        graphView.prefWidthProperty().bind(container.widthProperty());
        graphView.prefHeightProperty().bind(container.heightProperty());

        container.getChildren().add(graphView);
        graphView.init();
    }

    /**
     * Adds an entity to the graph.
     *
     * @param entity The entity to add.
     */
    public void addEntity(Entity entity) {
        if (!entityMap.containsKey(entity.getId())) {
            graph.insertVertex(entity);
            entityMap.put(entity.getId(), entity);
            updateView();
        }
    }

    /**
     * Adds a relationship between two entities.
     *
     * @param relationship The relationship to add.
     */
    public void addRelationship(Relationship relationship) {
        Entity source = entityMap.get(relationship.getSourceId());
        Entity target = entityMap.get(relationship.getTargetId());

        if (source != null && target != null) {
            graph.insertEdge(source, target, relationship);
            updateView();
        }
    }

    /**
     * Clears all nodes and edges from the graph.
     */
    public void clear() {
        Collection<Vertex<Entity>> vertices = new ArrayList<>(graph.vertices());
        for (Vertex<Entity> v : vertices) {
            graph.removeVertex(v);
        }
        entityMap.clear();
        updateView();
    }

    /**
     * Refreshes the visual representation.
     */
    public void updateView() {
        if (graphView != null) {
            graphView.update();
        }
    }

    public SmartGraphPanel<Entity, Relationship> getGraphView() {
        return graphView;
    }

    public Digraph<Entity, Relationship> getGraph() {
        return graph;
    }
}
