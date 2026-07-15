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

    // Lưu lịch sử file đã gửi
    // receiverId = null -> gửi chung (Public), có giá trị -> gửi riêng (Private)
    public static boolean luuLichSuFile(int senderId, Integer receiverId, String fileName, long fileSize) {
        String sql = "INSERT INTO file_transfers (sender_id, receiver_id, file_name, file_size) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            if (receiverId != null) {
                ps.setInt(2, receiverId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, fileName);
            ps.setLong(4, fileSize);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi luuLichSuFile: " + e.getMessage());
            return false;
        }
    }

    // Lấy lịch sử file chung (Public)
    // Trả về mảng: [senderUsername, fileName, fileSize, sentAt]
    public static List<String[]> layLichSuFileChung() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT u.username, ft.file_name, ft.file_size, ft.sent_at "
                + "FROM file_transfers ft "
                + "JOIN users u ON ft.sender_id = u.id "
                + "WHERE ft.receiver_id IS NULL "
                + "ORDER BY ft.sent_at ASC, ft.id ASC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] {
                    rs.getString("username"),
                    rs.getString("file_name"),
                    String.valueOf(rs.getLong("file_size")),
                    rs.getString("sent_at")
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layLichSuFileChung: " + e.getMessage());
        }
        return list;
    }

    // Lấy lịch sử file riêng giữa 2 người dùng
    // Trả về mảng: [senderUsername, fileName, fileSize, sentAt]
    public static List<String[]> layLichSuFileRieng(int userId1, int userId2) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT u.username, ft.file_name, ft.file_size, ft.sent_at "
                + "FROM file_transfers ft "
                + "JOIN users u ON ft.sender_id = u.id "
                + "WHERE (ft.sender_id = ? AND ft.receiver_id = ?) "
                + "   OR (ft.sender_id = ? AND ft.receiver_id = ?) "
                + "ORDER BY ft.sent_at ASC, ft.id ASC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] {
                        rs.getString("username"),
                        rs.getString("file_name"),
                        String.valueOf(rs.getLong("file_size")),
                        rs.getString("sent_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layLichSuFileRieng: " + e.getMessage());
        }
        return list;
    }
}
