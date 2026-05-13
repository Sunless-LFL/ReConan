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

    public void saveAll(int investigationId, List<Relationship> relationships) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete existing relationships for this investigation
                String deleteSql = "DELETE FROM relationships WHERE investigation_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setInt(1, investigationId);
                    pstmt.executeUpdate();
                }

                // 2. Insert new ones
                String insertSql = "INSERT INTO relationships (investigation_id, source_id, target_id, label, created_at) VALUES (?, ?, ?, ?, GETDATE())";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (Relationship rel : relationships) {
                        pstmt.setInt(1, investigationId);
                        pstmt.setInt(2, rel.getSourceId());
                        pstmt.setInt(3, rel.getTargetId());
                        pstmt.setString(4, rel.getLabel());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
                System.out.println("SQL Server: Successfully saved " + relationships.size() + " relationships.");
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
        String sql = "SELECT id, source_id, target_id, label FROM relationships WHERE investigation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, investigationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Relationship rel = new Relationship(
                            rs.getInt("source_id"),
                            rs.getInt("target_id"),
                            rs.getString("label")
                    );
                    rel.setId(rs.getInt("id"));
                    rel.setInvestigationId(investigationId);
                    relationships.add(rel);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Server: Error loading relationships: " + e.getMessage());
        }
        return relationships;
    }
}