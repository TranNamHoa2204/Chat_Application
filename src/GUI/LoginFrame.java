package GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    private JButton btnLogin, btnCancel;

    public LoginFrame() {
        this.setTitle("Login");
        this.setSize(400, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        ;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tạo các component
        JLabel lblUsername = new JLabel("Username:");
        txtUsername = new JTextField(20);

        JLabel lblPassword = new JLabel("Password:");
        txtPassword = new JPasswordField(20);

        btnLogin = new JButton("Login");
        btnCancel = new JButton("Cancel");

        // Thiết lập kích thước tối thiểu cho các component
        txtUsername.setPreferredSize(txtUsername.getPreferredSize());
        txtPassword.setPreferredSize(txtUsername.getPreferredSize());

        // Thêm username label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        mainPanel.add(lblUsername, gbc);

        // Thêm username txtField
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        mainPanel.add(txtUsername, gbc);

        // Thêm Password label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        mainPanel.add(lblPassword, gbc);

        // Thêm Password txtField
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        mainPanel.add(txtPassword, gbc);

        JPanel pnButton = new JPanel();
        pnButton.add(btnLogin);
        pnButton.add(Box.createHorizontalStrut(10));
        pnButton.add(btnCancel);

        // Thêm Các button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(pnButton, gbc);

        add(mainPanel);

        this.setVisible(true);

        btnLogin.addActionListener(e -> login());
        btnCancel.addActionListener(e -> dispose());

        // Cho phép nhấn Enter để login
        getRootPane().setDefaultButton(btnLogin);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame());
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (!username.isEmpty() && !password.isEmpty()) {
            // Xử lý đăng nhập ở đây
            System.out.println("Login attempt with: " + username);
            // Sau khi login thành công, có thể chuyển sang frame khác
        } else {
            // Hiển thị thông báo nếu username hoặc password trống
            System.out.println("Please enter both username and password");
        }
    }

}
