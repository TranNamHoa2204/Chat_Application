package GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import Controller.ChatController;
import java.io.IOException;

public class RegisterFrame extends JFrame {
    private JTextField txtHo, txtTen, txtUsername;
    private JPasswordField txtPassword;
    private JButton btnRegister, btnCancel;

    public RegisterFrame() {
        this.setTitle("Đăng ký tài khoản mới");
        this.setSize(400, 300);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng form này, không đóng cả app
        this.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtHo = new JTextField(20);
        txtTen = new JTextField(20);
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);

        btnRegister = new JButton("Đăng ký");
        btnCancel = new JButton("Hủy");

        // Các Label
        JLabel lblHo = new JLabel("Họ:");
        JLabel lblTen = new JLabel("Tên:");
        JLabel lblUsername = new JLabel("Username:");
        JLabel lblPassword = new JLabel("Password:");

        // Add layout
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; mainPanel.add(lblHo, gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.7; mainPanel.add(txtHo, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; mainPanel.add(lblTen, gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.7; mainPanel.add(txtTen, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; mainPanel.add(lblUsername, gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.7; mainPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; mainPanel.add(lblPassword, gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.7; mainPanel.add(txtPassword, gbc);

        // Buttons
        JPanel pnButton = new JPanel();
        pnButton.add(btnRegister);
        pnButton.add(Box.createHorizontalStrut(15));
        pnButton.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(pnButton, gbc);

        add(mainPanel);

        // Sự kiện nút bấm
        btnCancel.addActionListener(e -> dispose());
        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Username và Password!", "Lỗi",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ChatController tempController = new ChatController("localhost", 5000);
                if (tempController.register(username, password)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Đăng ký thành công!", "Thông báo",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    tempController.closeConnection();
                    dispose();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Đăng ký thất bại. Tài khoản có thể đã tồn tại!", "Lỗi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    tempController.closeConnection();
                }
            } catch (IOException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối hoặc đăng ký tới Server: " + ex.getMessage(),
                        "Lỗi Kết Nối", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        this.setVisible(true);
    }
}
