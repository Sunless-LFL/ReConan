package org.reconan.repository;

import org.reconan.database.DatabaseConnection;
import org.reconan.model.Relationship;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing Relationship persistence.
 */
public class RelationshipRepository {

    public void deleteAll(int investigationId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM relationships WHERE investigation_id = ?")) {
            pstmt.setInt(1, investigationId);
            pstmt.executeUpdate();
            System.out.println("SQL Server: Cleared relationships for investigation " + investigationId);
        } catch (SQLException e) {
            System.err.println("SQL Server: Error clearing relationships: " + e.getMessage());
        }
    }

    public void saveAll(int investigationId, List<Relationship> relationships) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Get existing relationship IDs
                List<Integer> dbIds = new ArrayList<>();
                String selectIdsSql = "SELECT id FROM relationships WHERE investigation_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(selectIdsSql)) {
                    pstmt.setInt(1, investigationId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            dbIds.add(rs.getInt(1));
                        }
                    }
                }

                // 2. Prepare statements
                String insertSql = "INSERT INTO relationships (investigation_id, source_id, target_id, label, created_at) VALUES (?, ?, ?, ?, GETDATE())";
                String updateSql = "UPDATE relationships SET source_id = ?, target_id = ?, label = ? WHERE id = ?";

                List<Integer> currentRelIds = new ArrayList<>();

                for (Relationship rel : relationships) {
                    if (dbIds.contains(rel.getId())) {
                        // UPDATE
                        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                            pstmt.setInt(1, rel.getSourceId());
                            pstmt.setInt(2, rel.getTargetId());
                            pstmt.setString(3, rel.getLabel());
                            pstmt.setInt(4, rel.getId());
                            pstmt.executeUpdate();
                        }
                        currentRelIds.add(rel.getId());
                    } else {
                        // INSERT
                        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setInt(1, investigationId);
                            pstmt.setInt(2, rel.getSourceId());
                            pstmt.setInt(3, rel.getTargetId());
                            pstmt.setString(4, rel.getLabel());
                            pstmt.executeUpdate();

                            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    rel.setId(rs.getInt(1));
                                    currentRelIds.add(rel.getId());
                                }
                            }
                        }
                    }
                }

                // 3. Delete orphans
                for (Integer dbId : dbIds) {
                    if (!currentRelIds.contains(dbId)) {
                        String deleteSql = "DELETE FROM relationships WHERE id = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                            pstmt.setInt(1, dbId);
                            pstmt.executeUpdate();
                        }
                    }
                }

                conn.commit();
                System.out.println("SQL Server: Successfully synchronized " + relationships.size() + " relationships.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("SQL Server: Error saving relationships: " + e.getMessage());
        }
    }

    public List<Relationship> findByInvestigationId(int investigationId) {
        List<Relationship> relationships = new ArrayList<>();
        String sql = "SELECT id, source_id, target_id, label, created_at FROM relationships WHERE investigation_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, investigationId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Relationship rel = new Relationship(
                        rs.getInt("source_id"),
                        rs.getInt("target_id"),
                        rs.getString("label")
                );
                rel.setId(rs.getInt("id"));
                rel.setInvestigationId(investigationId);

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    rel.setCreatedAt(createdAt.toLocalDateTime());
                }

                relationships.add(rel);
            }
        } catch (SQLException e) {
            System.err.println("SQL Server: Error loading relationships: " + e.getMessage());
        } finally {
            DatabaseConnection.close(rs, pstmt, conn);
        }
        return relationships;
    }

}