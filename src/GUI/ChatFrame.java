package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatFrame extends JFrame implements ActionListener {
    // Khai báo các thành phần giao diện
    private JList<String> listOnline; // Danh sách người dùng online (bên trái)
    private JTextArea txtHienThiTinNhan; // Khung hiển thị nội dung chat (ở giữa)
    private JTextField txtTinNhanNhap; // Ô nhập tin nhắn (phía dưới)
    private JButton btnGui; // Nút gửi

    public ChatFrame() {
        // 1. Cấu hình thuộc tính cơ bản cho Frame
        setTitle("Messenger");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Đóng chương trình khi bấm X
        setLocationRelativeTo(null); // Hiển thị cửa sổ ở chính giữa màn hình
        setLayout(new BorderLayout(5, 5)); // Khoảng cách giữa các vùng là 5px

        // 2. VÙNG BÊN TRÁI (WEST): Danh sách Online
        JPanel pnLeft = new JPanel(new BorderLayout());
        pnLeft.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Online"));
        pnLeft.setPreferredSize(new Dimension(150, 0)); // Cố định chiều rộng vùng bên trái là 150px

        // Demo danh sách người dùng online
        DefaultListModel<String> model = new DefaultListModel<>();
        listOnline = new JList<>(model);
        pnLeft.add(new JScrollPane(listOnline), BorderLayout.CENTER);

        // 3. VÙNG Ở GIỮA (CENTER): Khung hiển thị tin nhắn
        JPanel pnCenter = new JPanel(new BorderLayout());
        pnCenter.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Tin nhắn"));

        txtHienThiTinNhan = new JTextArea();
        txtHienThiTinNhan.setEditable(false); // Không cho người dùng gõ đè vào khung hiển thị
        txtHienThiTinNhan.setLineWrap(true); // Tự động xuống dòng khi text quá dài
        // txtHienThiTinNhan.setWrapStyleWord(true);

        // Đặt JTextArea vào JScrollPane để tự động có thanh cuộn khi tin nhắn nhiều
        JScrollPane scrollChat = new JScrollPane(txtHienThiTinNhan);
        pnCenter.add(scrollChat, BorderLayout.CENTER);

        // 4. VÙNG PHÍA DƯỚI (SOUTH): Thanh nhập liệu và nút Gửi
        JPanel pnSouth = new JPanel(new BorderLayout(5, 0));
        pnSouth.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Tạo khoảng cách viền xung quanh

        txtTinNhanNhap = new JTextField();
        btnGui = new JButton("Gửi");
        btnGui.setBackground(new Color(0, 132, 255)); // Đổi nút gửi thành màu xanh giống Messenger
        btnGui.setForeground(Color.WHITE); // Chữ màu trắng

        pnSouth.add(txtTinNhanNhap, BorderLayout.CENTER); // Ô nhập liệu chiếm phần lớn không gian
        pnSouth.add(btnGui, BorderLayout.EAST); // Nút gửi nằm gọn bên phải

        // 5. Thêm các Panel chính vào Frame theo BorderLayout
        add(pnLeft, BorderLayout.WEST);
        add(pnCenter, BorderLayout.CENTER);
        add(pnSouth, BorderLayout.SOUTH);

        btnGui.addActionListener(this);

        // Hiển thị giao diện
        setVisible(true);
    }

    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread để đảm bảo an toàn luồng trong
        // Swing
        javax.swing.SwingUtilities.invokeLater(() -> new ChatFrame());
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}