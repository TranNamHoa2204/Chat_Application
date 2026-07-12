package DAO;

import Database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
