package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thay đổi thông tin kết nối phù hợp với máy của bạn
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=JavaChatApp;encrypt=false;trustServerCertificate=true;";
    private static final String USERNAME = "sa"; // Hoặc tài khoản SQL Server của bạn
    private static final String PASSWORD = "sapassword"; // Mật khẩu sa SQL Server của bạn

    public static Connection getConnection() throws SQLException {
        try {
            // Tải driver SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy JDBC Driver cho SQL Server. Hãy add file .jar vào thư viện.");
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
