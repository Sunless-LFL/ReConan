package org.reconan.repository;

import org.reconan.database.DatabaseConnection;
import org.reconan.model.Entity;
import org.reconan.model.EntityType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing Entity persistence.
 */
public class EntityRepository {

    /**
     * Saves all entities for an investigation and returns a map of session IDs to new database IDs.
     * This uses an 'Upsert' logic to preserve existing IDs and creation timestamps.
     */
    public java.util.Map<Integer, Integer> saveAll(int investigationId, List<Entity> entities) {
        java.util.Map<Integer, Integer> idMap = new java.util.HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Get existing entity IDs in DB for this investigation
                List<Integer> dbIds = new ArrayList<>();
                String selectIdsSql = "SELECT id FROM entities WHERE investigation_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(selectIdsSql)) {
                    pstmt.setInt(1, investigationId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            dbIds.add(rs.getInt(1));
                        }
                    }
                }

                // 2. Prepare statements
                String insertEntitySql = "INSERT INTO entities (investigation_id, type, value, created_at, updated_at) VALUES (?, ?, ?, GETDATE(), GETDATE())";
                String updateEntitySql = "UPDATE entities SET type = ?, value = ?, updated_at = GETDATE() WHERE id = ?";
                String deletePropsSql = "DELETE FROM entity_properties WHERE entity_id = ?";
                String insertPropSql = "INSERT INTO entity_properties (entity_id, property_key, property_value) VALUES (?, ?, ?)";

                List<Integer> currentEntityIds = new ArrayList<>();

                for (Entity entity : entities) {
                    int oldId = entity.getId();
                    boolean isExisting = dbIds.contains(oldId);

                    if (isExisting) {
                        // UPDATE existing entity
                        try (PreparedStatement pstmt = conn.prepareStatement(updateEntitySql)) {
                            pstmt.setString(1, entity.getType().name());
                            pstmt.setString(2, entity.getValue());
                            pstmt.setInt(3, oldId);
                            pstmt.executeUpdate();
                        }
                        int newId = oldId;
                        idMap.put(oldId, newId);
                        currentEntityIds.add(newId);

                        // Refresh properties (Delete and Re-insert is safe for properties)
                        try (PreparedStatement delPropPstmt = conn.prepareStatement(deletePropsSql)) {
                            delPropPstmt.setInt(1, newId);
                            delPropPstmt.executeUpdate();
                        }
                        saveProperties(conn, insertPropSql, newId, entity.getProperties());
                    } else {
                        // INSERT new entity
                        try (PreparedStatement pstmt = conn.prepareStatement(insertEntitySql, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setInt(1, investigationId);
                            pstmt.setString(2, entity.getType().name());
                            pstmt.setString(3, entity.getValue());
                            pstmt.executeUpdate();

                            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    int newId = rs.getInt(1);
                                    entity.setId(newId);
                                    idMap.put(oldId, newId);
                                    currentEntityIds.add(newId);

                                    saveProperties(conn, insertPropSql, newId, entity.getProperties());
                                }
                            }
                        }
                    }
                }

                // 3. Delete orphaned entities (those in DB but not in the graph)
                for (Integer dbId : dbIds) {
                    if (!currentEntityIds.contains(dbId)) {
                        // Properties will be deleted by FK cascade if configured, but let's be explicit
                        try (PreparedStatement pstmt = conn.prepareStatement(deletePropsSql)) {
                            pstmt.setInt(1, dbId);
                            pstmt.executeUpdate();
                        }
                        String deleteEntitySql = "DELETE FROM entities WHERE id = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(deleteEntitySql)) {
                            pstmt.setInt(1, dbId);
                            pstmt.executeUpdate();
                        }
                    }
                }

                conn.commit();
                System.out.println("SQL Server: Successfully synchronized " + entities.size() + " entities.");
                return idMap;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("SQL Server: Error saving entities: " + e.getMessage());
            return idMap;
        }
    }

    private void saveProperties(Connection conn, String insertPropSql, int entityId, Map<String, String> properties) throws SQLException {
        if (properties.isEmpty()) return;

        try (PreparedStatement propPstmt = conn.prepareStatement(insertPropSql)) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                propPstmt.setInt(1, entityId);
                propPstmt.setString(2, entry.getKey());
                propPstmt.setString(3, entry.getValue());
                propPstmt.addBatch();
            }
            propPstmt.executeBatch();
        }
    }


    /**
     * Finds all entities for a given investigation.
     */
    public List<Entity> findByInvestigationId(int investigationId) {
        List<Entity> entities = new ArrayList<>();
        String sql = "SELECT id, type, value, created_at, updated_at FROM entities WHERE investigation_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, investigationId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Entity entity = new Entity(
                        EntityType.valueOf(rs.getString("type")),
                        rs.getString("value")
                );
                entity.setId(rs.getInt("id"));
                entity.setInvestigationId(investigationId);

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    entity.setCreatedAt(createdAt.toLocalDateTime());
                }

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    entity.setUpdatedAt(updatedAt.toLocalDateTime());
                }

                // Load properties
                loadProperties(entity, conn);

                entities.add(entity);
            }
        } catch (SQLException e) {
            System.err.println("SQL Server: Error loading entities: " + e.getMessage());
        } finally {
            DatabaseConnection.close(rs, pstmt, conn);
        }
        return entities;
    }

    private void loadProperties(Entity entity, Connection conn) throws SQLException {
        String sql = "SELECT property_key, property_value FROM entity_properties WHERE entity_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, entity.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                entity.addProperty(rs.getString("property_key"), rs.getString("property_value"));
            }
        } finally {
            DatabaseConnection.close(rs, pstmt);
        }
    }
}