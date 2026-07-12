package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ChatFrame extends JFrame {
    private JList<String> listOnline;
    private JTextArea txtHienThiTinNhan;
    private JTextField txtTinNhanNhap;
    private JButton btnGui;

    // Quản lý kết nối Socket bên trong giao diện
    private Socket socket;
    private String username;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ChatFrame(Socket socket, String username) {
        this.socket = socket;
        this.username = username;

        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 1. Cấu hình Cửa sổ
        setTitle("Messenger - " + username);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // 2. Danh sách Online (West)
        JPanel pnLeft = new JPanel(new BorderLayout());
        pnLeft.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Online"));
        pnLeft.setPreferredSize(new Dimension(150, 0));

        DefaultListModel<String> model = new DefaultListModel<>();
        listOnline = new JList<>(model);
        pnLeft.add(new JScrollPane(listOnline), BorderLayout.CENTER);

        // Demo tạm thời
        model.addElement(username + " (Bạn)");

        // 3. Khung hiển thị tin nhắn (Center)
        JPanel pnCenter = new JPanel(new BorderLayout());
        pnCenter.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Tin nhắn"));

        txtHienThiTinNhan = new JTextArea();
        txtHienThiTinNhan.setEditable(false);
        txtHienThiTinNhan.setLineWrap(true);
        txtHienThiTinNhan.setWrapStyleWord(true); // Xuống hàng theo từ, không bị cắt đôi chữ

        JScrollPane scrollChat = new JScrollPane(txtHienThiTinNhan);
        pnCenter.add(scrollChat, BorderLayout.CENTER);

        // 4. Thanh nhập liệu (South)
        JPanel pnSouth = new JPanel(new BorderLayout(5, 0));
        pnSouth.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        txtTinNhanNhap = new JTextField();
        btnGui = new JButton("Gửi");
        btnGui.setBackground(new Color(0, 132, 255));
        btnGui.setForeground(Color.WHITE);

        pnSouth.add(txtTinNhanNhap, BorderLayout.CENTER);
        pnSouth.add(btnGui, BorderLayout.EAST);

        // Gắn các Panel vào Frame
        add(pnLeft, BorderLayout.WEST);
        add(pnCenter, BorderLayout.CENTER);
        add(pnSouth, BorderLayout.SOUTH);

        // 5. Đăng ký các sự kiện hành động
        btnGui.addActionListener(e -> guiTinNhan());
        txtTinNhanNhap.addActionListener(e -> guiTinNhan()); // Nhấn Enter ở ô text cũng gửi được tin

        // Sự kiện khi người dùng click nút [X] để đóng ứng dụng -> Đóng socket sạch sẽ
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                docNgoacKetNoi();
            }
        });

        // 6. Kích hoạt Thread nhận tin nhắn từ Server liên tục mà không gây đơ UI
        startReceiveThread();

        this.setVisible(true);
    }

    private void guiTinNhan() {
        String msg = txtTinNhanNhap.getText().trim();
        if (msg.isEmpty())
            return;

        try {
            dos.writeUTF(msg);
            dos.flush();

            // Hiển thị tin nhắn của chính mình lên ô chat
            txtHienThiTinNhan.append("Bạn: " + msg + "\n");
            txtTinNhanNhap.setText(""); // Xóa trống ô nhập dữ liệu
            txtTinNhanNhap.requestFocus(); // Đặt lại con trỏ chuột vào ô nhập
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể gửi tin nhắn. Kết nối gặp lỗi!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startReceiveThread() {
        Thread receiveThread = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    String msgText = dis.readUTF();
                    SwingUtilities.invokeLater(() -> {
                        // Nhận tin nhắn từ người khác và cập nhật thẳng lên giao diện
                        txtHienThiTinNhan.append(msgText + "\n");
                        // Tự động cuộn thanh cuốn JScrollPane xuống cuối cùng khi có tin nhắn mới
                        txtHienThiTinNhan.setCaretPosition(txtHienThiTinNhan.getDocument().getLength());
                    }); 
                }
            } catch (IOException e) {
                if (socket != null && !socket.isClosed()) {
                    txtHienThiTinNhan.append(">> Mất kết nối tới máy chủ.\n");
                }
            }
        });
        receiveThread.setDaemon(true); // Thread sẽ tự hủy nếu tắt ứng dụng GUI
        receiveThread.start();
    }

    private void docNgoacKetNoi() {
        try {
            if (dos != null)
                dos.close();
            if (dis != null)
                dis.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}