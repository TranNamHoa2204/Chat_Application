package DAO;

import Database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // 1. Kiểm tra đăng nhập
    public static boolean kiemTraDangNhap(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // Trong thực tế nên dùng BCrypt hash nhưng bài tập thì kiểm tra chuỗi thường
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu tìm thấy dòng tương ứng
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiemTraDangNhap: " + e.getMessage());
            return false;
        }
    }

    // 2. Đăng ký tài khoản
    public static boolean dangKy(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi dangKy: " + e.getMessage());
            return false;
        }
    }

    // 3. Lấy ID từ username
    public static int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getUserId: " + e.getMessage());
        }
        return -1;
    }

    public static List<String> getAllUsernames() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Loi getAllUsernames: " + e.getMessage());
        }
        return users;
    }

    // Đổi password (sau khi đã xác minh password cũ)
    public static boolean doiMatKhau(String username, String oldPassword, String newPassword) {
        // Xác minh password cũ trước
        if (!kiemTraDangNhap(username, oldPassword)) {
            return false;
        }
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi doiMatKhau: " + e.getMessage());
            return false;
        }
    }

    // Đổi username
    public static boolean doiUsername(String oldUsername, String newUsername, String password) {
        // Xác minh password trước khi cho đổi username
        if (!kiemTraDangNhap(oldUsername, password)) {
            return false;
        }
        String sql = "UPDATE users SET username = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setString(2, oldUsername);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Lỗi unique constraint = username đã tồn tại
            System.err.println("Lỗi doiUsername: " + e.getMessage());
            return false;
        }
    }

}
