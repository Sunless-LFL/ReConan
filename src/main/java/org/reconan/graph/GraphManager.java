package org.reconan.graph;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import javafx.application.Platform;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.reconan.model.Entity;
import org.reconan.model.Relationship;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the investigation graph logic and its visual representation via
 * SmartGraph.
 */
public class GraphManager {

    private final Digraph<Entity, Relationship> graph;
    private final Map<Integer, Entity> entityMap;
    private final Map<Integer, Vertex<Entity>> vertexMap;
    private SmartGraphPanel<Entity, Relationship> graphView;
    private boolean isAutomaticLayout = true;

    public GraphManager() {
        this.graph = new DigraphEdgeList<>();
        this.entityMap = new HashMap<>();
        this.vertexMap = new HashMap<>();
    }

    /**
     * Initializes the visual graph panel.
     *
     * @param container The JavaFX StackPane where the graph will be displayed.
     */
    public void initializeView(StackPane container) {
        SmartPlacementStrategy strategy = new SmartRandomPlacementStrategy();

        SmartGraphProperties properties = null;
        URI styleSheet = null;

        try {
            properties = new SmartGraphProperties(getClass().getResourceAsStream("/smartgraph.properties"));
            styleSheet = getClass().getResource("/css/smartgraph.css").toURI();
        } catch (Exception e) {
            System.err.println("Terminal: Could not load SmartGraph assets: " + e.getMessage());
        }

        if (properties != null && styleSheet != null) {
            this.graphView = new SmartGraphPanel<>(graph, properties, strategy, styleSheet);
        } else {
            this.graphView = new SmartGraphPanel<>(graph, strategy);
        }

        // Force automatic layout to be ON by default
        this.graphView.setAutomaticLayout(true);

        // Bind view size to container size
        graphView.prefWidthProperty().bind(container.widthProperty());
        graphView.prefHeightProperty().bind(container.heightProperty());

        container.getChildren().add(0, graphView);

        // Enable Zoom
        setupZoom();

        // Enable Panning
        setupPanning();

        // Defer initialization until the scene is attached
        Platform.runLater(() -> {
            try {
                graphView.init();
            } catch (Exception e) {
                System.err.println("Terminal: SmartGraph initialization failed: " + e.getMessage());
            }
        });
    }

    private void setupZoom() {
        if (graphView != null) {
            graphView.addEventHandler(ScrollEvent.SCROLL, event -> {
                double delta = event.getDeltaY();
                if (delta == 0)
                    return;

                double factor = (delta > 0) ? 1.1 : 0.9;
                graphView.setScaleX(graphView.getScaleX() * factor);
                graphView.setScaleY(graphView.getScaleY() * factor);
                event.consume();
            });
        }
    }

    private double lastMouseX, lastMouseY;

    private void setupPanning() {
        if (graphView != null) {
            graphView.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown()) {
                    lastMouseX = event.getSceneX();
                    lastMouseY = event.getSceneY();
                }
            });

            graphView.setOnMouseDragged(event -> {
                if (event.isPrimaryButtonDown()) {
                    // Only pan if we're clicking the background, not a node
                    if (event.getTarget() == graphView || event.getTarget() instanceof Pane) {
                        double deltaX = event.getSceneX() - lastMouseX;
                        double deltaY = event.getSceneY() - lastMouseY;

                        graphView.setTranslateX(graphView.getTranslateX() + deltaX);
                        graphView.setTranslateY(graphView.getTranslateY() + deltaY);

                        lastMouseX = event.getSceneX();
                        lastMouseY = event.getSceneY();
                    }
                }
            });
        }
    }



    /**
     * Adds an entity to the graph at specific coordinates.
     *
     * @param entity The entity to add.
     * @param x      The x coordinate.
     * @param y      The y coordinate.
     */
    public void addEntity(Entity entity, double x, double y) {
        if (!entityMap.containsKey(entity.getId())) {
            Vertex<Entity> vertex = graph.insertVertex(entity);
            entityMap.put(entity.getId(), entity);
            vertexMap.put(entity.getId(), vertex);

            if (graphView != null) {
                graphView.update();
                // Set initial visual position for the drop event
                graphView.setVertexPosition(vertex, x, y);

                // Add CSS class based on entity type
                Platform.runLater(() -> {
                    try {
                        var stylable = graphView.getStylableVertex(vertex);
                        if (stylable != null) {
                            String cssClass = entity.getType().name().toLowerCase().replace("_", "-");
                            stylable.addStyleClass(cssClass);
                        }
                    } catch (Exception e) {
                        // Silent fail if view is not ready
                    }
                });
            }
        }
    }

    /**
     * Removes an entity from the graph.
     *
     * @param entity The entity to remove.
     */
    public void removeEntity(Entity entity) {
        Vertex<Entity> vertex = vertexMap.get(entity.getId());
        if (vertex != null) {
            graph.removeVertex(vertex);
            entityMap.remove(entity.getId());
            vertexMap.remove(entity.getId());
            updateView();
        }
    }

    /**
     * Updates an entity's visual state on the graph.
     *
     * @param entity The updated entity.
     */
    public void updateEntity(Entity entity) {
        // SmartGraph labels are usually bound to the toString() or a provider.
        // We just need to trigger an update.
        updateView();
    }

    /**
     * Sets a listener for node selection events.
     *
     * @param onSelect Callback when an entity is selected.
     */
    public void setSelectionListener(java.util.function.Consumer<Entity> onSelect) {
        if (graphView != null) {
            graphView.setVertexDoubleClickAction(graphVertex -> {
                onSelect.accept(graphVertex.getUnderlyingVertex().element());
            });
        }
    }

    /**
     * Sets a listener for edge (relationship) double-click events.
     *
     * @param onSelect Callback when a relationship is selected.
     */
    public void setEdgeSelectionListener(java.util.function.Consumer<Relationship> onSelect) {
        if (graphView != null) {
            graphView.setEdgeDoubleClickAction(graphEdge -> {
                onSelect.accept(graphEdge.getUnderlyingEdge().element());
            });
        }
    }

    /**
     * Adds an entity to the graph.
     *
     * @param entity The entity to add.
     */
    public void addEntity(Entity entity) {
        addEntity(entity, 0, 0);
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

    public void toggleAutomaticLayout() {
        if (graphView != null) {
            isAutomaticLayout = !isAutomaticLayout;
            graphView.setAutomaticLayout(isAutomaticLayout);
            System.out.println("Terminal: Automatic Layout " + (isAutomaticLayout ? "Enabled" : "Disabled"));
        }
    }

    public void setAutomaticLayout(boolean enabled) {
        if (graphView != null) {
            this.isAutomaticLayout = enabled;
            graphView.setAutomaticLayout(enabled);
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
        vertexMap.clear();
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

    public Collection<Entity> getEntities() {
        return entityMap.values();
    }

    public Collection<Relationship> getRelationships() {
        Collection<Relationship> relationships = new ArrayList<>();
        graph.edges().forEach(edge -> relationships.add(edge.element()));
        return relationships;
    }
}