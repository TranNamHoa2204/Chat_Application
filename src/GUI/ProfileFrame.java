package GUI;

import Controller.ChatController;
import javax.swing.*;
import java.awt.*;

public class ProfileFrame extends JFrame {
    public ProfileFrame(ChatController controller) {
        setTitle("Thông tin cá nhân - " + controller.getUsername());
        setSize(380, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        // --- Tab đổi mật khẩu ---
        JPanel tabPass = new JPanel(new GridBagLayout());
        tabPass.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPasswordField txtOldPass = new JPasswordField(20);
        JPasswordField txtNewPass = new JPasswordField(20);
        JPasswordField txtConfirmPass = new JPasswordField(20);
        JButton btnChangePass = new JButton("Đổi mật khẩu");

        gbc.gridx = 0; gbc.gridy = 0; tabPass.add(new JLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1; tabPass.add(txtOldPass, gbc);
        gbc.gridx = 0; gbc.gridy = 1; tabPass.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; tabPass.add(txtNewPass, gbc);
        gbc.gridx = 0; gbc.gridy = 2; tabPass.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; tabPass.add(txtConfirmPass, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        tabPass.add(btnChangePass, gbc);

        btnChangePass.addActionListener(e -> {
            String oldPass = new String(txtOldPass.getPassword());
            String newPass = new String(txtNewPass.getPassword());
            String confirmPass = new String(txtConfirmPass.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            controller.changePassword(oldPass, newPass);
            JOptionPane.showMessageDialog(this, "Yêu cầu đã được gửi. Kiểm tra cửa sổ chat để xem kết quả.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        // --- Tab đổi username ---
        JPanel tabUser = new JPanel(new GridBagLayout());
        tabUser.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCurrentUser = new JLabel("Hiện tại: " + controller.getUsername());
        JTextField txtNewUsername = new JTextField(20);
        JPasswordField txtPassConfirm = new JPasswordField(20);
        JButton btnChangeUser = new JButton("Đổi username");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; tabUser.add(lblCurrentUser, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; tabUser.add(new JLabel("Username mới:"), gbc);
        gbc.gridx = 1; tabUser.add(txtNewUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 2; tabUser.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; tabUser.add(txtPassConfirm, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        tabUser.add(btnChangeUser, gbc);

        btnChangeUser.addActionListener(e -> {
            String newUsername = txtNewUsername.getText().trim();
            String pass = new String(txtPassConfirm.getPassword());

            if (newUsername.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            controller.changeUsername(newUsername, pass);
            JOptionPane.showMessageDialog(this, "Yêu cầu đã được gửi. Kiểm tra cửa sổ chat để xem kết quả.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        tabs.addTab("Đổi mật khẩu", tabPass);
        tabs.addTab("Đổi username", tabUser);
        add(tabs);
        setVisible(true);
    }
}
