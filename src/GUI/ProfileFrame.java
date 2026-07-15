package GUI;

import Controller.ChatController;
import javax.swing.*;
import java.awt.*;

public class ProfileFrame extends JFrame {
    public ProfileFrame(ChatController controller) {
        setTitle("Thông tin cá nhân - " + controller.getUsername());
        setSize(390, 320);
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        tabPass.add(new JLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1;
        tabPass.add(txtOldPass, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        tabPass.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1;
        tabPass.add(txtNewPass, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        tabPass.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1;
        tabPass.add(txtConfirmPass, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        tabPass.add(btnChangePass, gbc);

        btnChangePass.addActionListener(e -> {
            String oldPass = new String(txtOldPass.getPassword());
            String newPass = new String(txtNewPass.getPassword());
            String confirmPass = new String(txtConfirmPass.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(oldPass.equals(newPass)){
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải khác mật khẩu cũ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

            controller.changePassword(oldPass, newPass);
            JOptionPane.showMessageDialog(this, "Yêu cầu đã được gửi. Kiểm tra cửa sổ chat để xem kết quả.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionPanel.add(btnChangeUser);

        // Username hiện tại
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        tabUser.add(lblCurrentUser, gbc);

        // Username mới
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        tabUser.add(new JLabel("Username mới:"), gbc);

        gbc.gridx = 1;
        tabUser.add(txtNewUsername, gbc);

        // Mật khẩu xác nhận
        gbc.gridx = 0;
        gbc.gridy = 2;
        tabUser.add(new JLabel("Xác nhận mật khẩu:"), gbc);

        gbc.gridx = 1;
        tabUser.add(txtPassConfirm, gbc);

        // Hai nút
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        tabUser.add(actionPanel, gbc);

        btnChangeUser.addActionListener(e -> {
            String newUsername = txtNewUsername.getText().trim();
            String pass = new String(txtPassConfirm.getPassword());

            if (newUsername.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            controller.changeUsername(newUsername, pass);
            JOptionPane.showMessageDialog(this, "Yêu cầu đã được gửi. Kiểm tra cửa sổ chat để xem kết quả.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        JButton btnDeleteAccount = new JButton("Xóa tài khoản");
        btnDeleteAccount.setForeground(Color.WHITE); 
        btnDeleteAccount.setBackground(Color.red);// Đổi màu chữ sang đỏ để cảnh báo
        actionPanel.add(btnDeleteAccount);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        tabUser.add(actionPanel, gbc); 

        // --- Sự kiện cho nút xóa tài khoản ---
        btnDeleteAccount.addActionListener(e -> {
            showDeleteDialog(controller);
        });

        tabs.addTab("Đổi mật khẩu", tabPass);
        tabs.addTab("Đổi username", tabUser);
        add(tabs);
        setVisible(true);
    }

    private void showDeleteDialog(ChatController controller) {
        JDialog dialog = new JDialog(this, "Xóa tài khoản", true);
        dialog.setSize(350, 170);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbl = new JLabel("Nhập mật khẩu để xác nhận:");
        JPasswordField txtPassword = new JPasswordField(20);

        JButton btnDelete = new JButton("Xóa");
        JButton btnCancel = new JButton("Hủy");

        JPanel panelBtn = new JPanel();
        panelBtn.add(btnDelete);
        panelBtn.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(lbl, gbc);

        gbc.gridy = 1;
        dialog.add(txtPassword, gbc);

        gbc.gridy = 2;
        dialog.add(panelBtn, gbc);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnDelete.addActionListener(e -> {
            String pass = new String(txtPassword.getPassword());

            if (pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng nhập mật khẩu!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "Bạn có chắc chắn muốn xóa tài khoản?\nThao tác này không thể hoàn tác.",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                controller.deleteAccount(pass);
                dialog.dispose();
                dispose(); // đóng ProfileFrame nếu muốn
            }
        });

        dialog.setVisible(true);
    }
}
