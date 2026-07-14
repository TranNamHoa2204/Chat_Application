package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import Controller.ChatController;

public class ChatFrame extends JFrame {
    private JList<String> listUsers;
    private JTextArea txtHienThiTinNhan;
    private JTextField txtTinNhanNhap;
    private JButton btnGui;
    private JLabel lblDangChatVoi;
    private JButton btnChatChung;

    private ChatController controller;

    public ChatFrame(ChatController controller) {
        this.controller = controller;
        this.controller.setView(this);

        setTitle("Messenger - " + controller.getUsername());
        setSize(760, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        txtHienThiTinNhan = new JTextArea();
        txtHienThiTinNhan.setEditable(false);
        txtHienThiTinNhan.setLineWrap(true);
        txtHienThiTinNhan.setWrapStyleWord(true);
        JScrollPane scrollChat = new JScrollPane(txtHienThiTinNhan);

        listUsers = new JList<>();
        JScrollPane scrollUsers = new JScrollPane(listUsers);
        scrollUsers.setPreferredSize(new Dimension(190, 0));
        scrollUsers.setBorder(BorderFactory.createTitledBorder("Nguoi dung"));

        btnChatChung = new JButton("Chat chung");
        lblDangChatVoi = new JLabel("Dang chat: Tat ca");

        JPanel pnBottom = new JPanel(new BorderLayout(5, 5));
        txtTinNhanNhap = new JTextField();
        btnGui = new JButton("Gui");
        pnBottom.add(txtTinNhanNhap, BorderLayout.CENTER);
        pnBottom.add(btnGui, BorderLayout.EAST);
        pnBottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel pnRight = new JPanel(new BorderLayout(5, 5));
        pnRight.add(scrollUsers, BorderLayout.CENTER);
        pnRight.add(btnChatChung, BorderLayout.SOUTH);

        JPanel pnTop = new JPanel(new BorderLayout());
        pnTop.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnTop.add(lblDangChatVoi, BorderLayout.WEST);

        add(pnTop, BorderLayout.NORTH);
        add(scrollChat, BorderLayout.CENTER);
        add(pnRight, BorderLayout.EAST);
        add(pnBottom, BorderLayout.SOUTH);

        btnGui.addActionListener(e -> thucHienHanhDongGui());
        txtTinNhanNhap.addActionListener(e -> thucHienHanhDongGui());

        listUsers.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = listUsers.getSelectedValue();
                String selectedUser = getUsernameFromListValue(selectedValue);

                if (selectedUser == null || selectedUser.equals(controller.getUsername())) {
                    controller.setCurrentReceiver(null);
                    lblDangChatVoi.setText("Dang chat: Tat ca");
                    controller.requestPublicHistory();
                    return;
                }

                controller.setCurrentReceiver(selectedUser);
                lblDangChatVoi.setText("Dang chat rieng voi: " + selectedUser);
                controller.requestPrivateHistory(selectedUser);
            }
        });

        btnChatChung.addActionListener(e -> {
            listUsers.clearSelection();
            controller.setCurrentReceiver(null);
            lblDangChatVoi.setText("Dang chat: Tat ca");
            controller.requestPublicHistory();
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.closeConnection();
            }
        });

        this.setVisible(true);
    }

    private void thucHienHanhDongGui() {
        String msg = txtTinNhanNhap.getText().trim();
        if (msg.isEmpty())
            return;

        controller.sendTextMessage(msg);

        String receiver = controller.getCurrentReceiver();
        if (receiver == null || receiver.isEmpty()) {
            txtHienThiTinNhan.append("Ban: " + msg + "\n");
        } else {
            txtHienThiTinNhan.append("[Rieng toi " + receiver + "] Ban: " + msg + "\n");
        }

        txtTinNhanNhap.setText("");
        txtTinNhanNhap.requestFocus();
    }

    public void clearMessages(String title) {
        SwingUtilities.invokeLater(() -> {
            txtHienThiTinNhan.setText("");
            txtHienThiTinNhan.append("=== " + title + " ===\n");
        });
    }

    public void appendHistoryMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            txtHienThiTinNhan.append(message + "\n");
            txtHienThiTinNhan.setCaretPosition(txtHienThiTinNhan.getDocument().getLength());
        });
    }

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            txtHienThiTinNhan.append(message + "\n");
            txtHienThiTinNhan.setCaretPosition(txtHienThiTinNhan.getDocument().getLength());
        });
    }

    public void updateOnlineList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (String user : users) {
                if (!user.isEmpty()) {
                    model.addElement(user + " (online)");
                }
            }
            listUsers.setModel(model);
        });
    }

    public void updateUserStatusList(String[] userStatuses) {
        SwingUtilities.invokeLater(() -> {
            String selectedUser = getUsernameFromListValue(listUsers.getSelectedValue());
            DefaultListModel<String> model = new DefaultListModel<>();

            for (String item : userStatuses) {
                if (item == null || item.trim().isEmpty()) {
                    continue;
                }

                String[] parts = item.split(":", 2);
                String user = parts[0];
                String status = parts.length > 1 ? parts[1] : "offline";
                model.addElement(user + " (" + status + ")");
            }

            listUsers.setModel(model);
            if (selectedUser != null) {
                selectUserInList(selectedUser);
            }
        });
    }

    private String getUsernameFromListValue(String value) {
        if (value == null) {
            return null;
        }

        int statusIndex = value.lastIndexOf(" (");
        if (statusIndex >= 0) {
            return value.substring(0, statusIndex);
        }
        return value;
    }

    private void selectUserInList(String username) {
        for (int i = 0; i < listUsers.getModel().getSize(); i++) {
            String value = listUsers.getModel().getElementAt(i);
            if (username.equals(getUsernameFromListValue(value))) {
                listUsers.setSelectedIndex(i);
                return;
            }
        }
    }
}
