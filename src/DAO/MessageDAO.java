package DAO;

import Database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    // Lưu tin nhắn
    public static boolean luuTinNhan(int senderId, Integer receiverId, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            if (receiverId != null) {
                ps.setInt(2, receiverId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER); // Chat chung (Public)
            }
            ps.setString(3, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi luuTinNhan: " + e.getMessage());
            return false;
        }
    }

    public static List<String[]> layLichSuChatChung() {
        List<String[]> messages = new ArrayList<>();
        String sql = "SELECT u.username, m.content "
                + "FROM messages m "
                + "JOIN users u ON m.sender_id = u.id "
                + "WHERE m.receiver_id IS NULL "
                + "ORDER BY m.created_at ASC, m.id ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                messages.add(new String[] { rs.getString("username"), rs.getString("content") });
            }
        } catch (SQLException e) {
            System.err.println("Loi layLichSuChatChung: " + e.getMessage());
        }
        return messages;
    }

    public static List<String[]> layLichSuChatRieng(int userId1, int userId2) {
        List<String[]> messages = new ArrayList<>();
        String sql = "SELECT u.username, m.content "
                + "FROM messages m "
                + "JOIN users u ON m.sender_id = u.id "
                + "WHERE (m.sender_id = ? AND m.receiver_id = ?) "
                + "   OR (m.sender_id = ? AND m.receiver_id = ?) "
                + "ORDER BY m.created_at ASC, m.id ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new String[] { rs.getString("username"), rs.getString("content") });
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi layLichSuChatRieng: " + e.getMessage());
        }
        return messages;
    }
}
