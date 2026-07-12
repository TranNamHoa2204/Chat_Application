package GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import Controller.ChatController;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnCancel;

    public LoginFrame() {
        this.setTitle("Đăng nhập Hệ thống");
        this.setSize(400, 250);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Tăng khoảng cách một chút cho thoáng
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsername = new JLabel("Username:");
        txtUsername = new JTextField(20);

        JLabel lblPassword = new JLabel("Password:");
        txtPassword = new JPasswordField(20);

        btnLogin = new JButton("Login");
        btnCancel = new JButton("Cancel");

        // Thêm username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        mainPanel.add(lblUsername, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        mainPanel.add(txtUsername, gbc);

        // Thêm Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        mainPanel.add(lblPassword, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        mainPanel.add(txtPassword, gbc);

        // Cụm Button
        JPanel pnButton = new JPanel();
        pnButton.add(btnLogin);
        pnButton.add(Box.createHorizontalStrut(15));
        pnButton.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(pnButton, gbc);

        add(mainPanel);

        // Đăng ký sự kiện TRƯỚC KHI hiển thị giao diện
        btnLogin.addActionListener(e -> login());
        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLogin);

        this.setVisible(true);
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
        	// Tạo bộ não điều khiển trước
        	ChatController controller = new ChatController("localhost", 5000, username);

        	// Tạo giao diện và truyền bộ não vào
        	ChatFrame chatFrame = new ChatFrame(controller);

        	// Kích hoạt luồng nghe tin nhắn từ controller
        	controller.startListening();
        	
        	this.dispose();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối tới Server. Hãy chắc chắn Server đang chạy!",
                    "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}