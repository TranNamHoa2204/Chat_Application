package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

import Controller.ChatController;

public class ChatFrame extends JFrame {
    private JList<String> listOnline;
    private JTextArea txtHienThiTinNhan;
    private JTextField txtTinNhanNhap;
    private JButton btnGui;
    private JLabel lblDangChatVoi;
    private JButton btnChatChung;

    private ChatController controller; // Thay thế Socket bằng Controller

    public ChatFrame(ChatController controller) {
        this.controller = controller;
        this.controller.setView(this); // Gắn frame này vào controller để nhận callback

        // [Toàn bộ phần code khởi tạo giao diện giữ nguyên...]
        setTitle("Messenger - " + controller.getUsername());
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // Khởi tạo các thành phần giao diện
        txtHienThiTinNhan = new JTextArea();
        txtHienThiTinNhan.setEditable(false);
        txtHienThiTinNhan.setLineWrap(true);
        txtHienThiTinNhan.setWrapStyleWord(true);
        JScrollPane scrollChat = new JScrollPane(txtHienThiTinNhan);

        listOnline = new JList<>();
        JScrollPane scrollOnline = new JScrollPane(listOnline);
        scrollOnline.setPreferredSize(new Dimension(150, 0));
        scrollOnline.setBorder(BorderFactory.createTitledBorder("Online"));

        btnChatChung = new JButton("Chat chung");
        lblDangChatVoi = new JLabel("Đang chat: Tất cả");

        JPanel pnBottom = new JPanel(new BorderLayout(5, 5));
        txtTinNhanNhap = new JTextField();
        btnGui = new JButton("Gửi");
        pnBottom.add(txtTinNhanNhap, BorderLayout.CENTER);
        pnBottom.add(btnGui, BorderLayout.EAST);
        pnBottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Thêm vào JFrame
        // add(scrollChat, BorderLayout.CENTER);
        // add(scrollOnline, BorderLayout.EAST);
        // add(pnBottom, BorderLayout.SOUTH);

        JPanel pnRight = new JPanel(new BorderLayout(5, 5));
        pnRight.add(scrollOnline, BorderLayout.CENTER);
        pnRight.add(btnChatChung, BorderLayout.SOUTH);

        JPanel pnTop = new JPanel(new BorderLayout());
        pnTop.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnTop.add(lblDangChatVoi, BorderLayout.WEST);

        add(pnTop, BorderLayout.NORTH);
        add(scrollChat, BorderLayout.CENTER);
        add(pnRight, BorderLayout.EAST);
        add(pnBottom, BorderLayout.SOUTH);

        // Đăng ký sự kiện
        btnGui.addActionListener(e -> thucHienHanhDongGui());
        txtTinNhanNhap.addActionListener(e -> thucHienHanhDongGui());

        listOnline.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = listOnline.getSelectedValue();

                if (selectedUser == null || selectedUser.equals(controller.getUsername())) {
                    controller.setCurrentReceiver(null);
                    lblDangChatVoi.setText("Đang chat: Tất cả");
                    return;
                }

                controller.setCurrentReceiver(selectedUser);
                lblDangChatVoi.setText("Đang chat riêng với: " + selectedUser);
            }
        });

        btnChatChung.addActionListener(e -> {
            listOnline.clearSelection();
            controller.setCurrentReceiver(null);
            lblDangChatVoi.setText("Đang chat: Tất cả");
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.closeConnection(); // Đóng kết nối thông qua bộ điều khiển
            }
        });

        this.setVisible(true);
    }

    // Hàm phục vụ tương tác từ giao diện
    private void thucHienHanhDongGui() {
        String msg = txtTinNhanNhap.getText().trim();
        if (msg.isEmpty())
            return;

        // Báo cho bộ điều khiển gửi đi thay vì tự gửi
        controller.sendTextMessage(msg);

        //txtHienThiTinNhan.append("Bạn: " + msg + "\n");

        String receiver = controller.getCurrentReceiver();
        if (receiver == null || receiver.isEmpty()) {
            txtHienThiTinNhan.append("Bạn: " + msg + "\n");
        } else {
            txtHienThiTinNhan.append("[Riêng tới " + receiver + "] Bạn: " + msg + "\n");
        }
        
        txtTinNhanNhap.setText("");
        txtTinNhanNhap.requestFocus();
    }

    // Hàm CHỈ làm nhiệm vụ hiển thị - sẽ được Controller gọi từ luồng nhận tin ngầm
    public void appendMessage(String message) {
        // Đảm bảo cập nhật giao diện an toàn trong Swing thread
        SwingUtilities.invokeLater(() -> {
            txtHienThiTinNhan.append(message + "\n");
            txtHienThiTinNhan.setCaretPosition(txtHienThiTinNhan.getDocument().getLength());
        });
    }

    public void updateOnlineList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            listOnline.setListData(users);
        });
    }
}