package GUI;

import Controller.ChatController;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatFrame extends JFrame {

    // ── Widgets ──────────────────────────────────────────────────────────────────
    private JList<String> listUsers;
    private JPanel        chatPanel;
    private JScrollPane   scrollChat;
    private JTextField    txtTinNhanNhap;
    private JButton       btnGui;
    private JLabel        lblDangChatVoi;
    private JButton       btnChatChung;

    private final ChatController controller;
    private BubbleFactory        bubbles;   // tạo và add bubble vào chatPanel

    // ── Constructor ──────────────────────────────────────────────────────────────
    public ChatFrame(ChatController controller) {
        this.controller = controller;
        this.controller.setView(this);

        setTitle("Messenger - " + controller.getUsername());
        setSize(820, 580);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        buildUI();
        wireEvents();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { controller.closeConnection(); }
        });

        setVisible(true);
    }

    // ── Xây dựng UI ─────────────────────────────────────────────────────────────
    private void buildUI() {
        // Chat area
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BubbleFactory.COLOR_BG_CHAT);
        chatPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollChat = new JScrollPane(chatPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);
        scrollChat.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        bubbles = new BubbleFactory(chatPanel, scrollChat, this);

        // Panel phải – danh sách user
        listUsers = new JList<>();
        listUsers.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollUsers = new JScrollPane(listUsers);
        scrollUsers.setPreferredSize(new Dimension(190, 0));
        scrollUsers.setBorder(BorderFactory.createTitledBorder("Người dùng"));

        btnChatChung = new JButton("Chat chung");

        JPanel pnRight = new JPanel(new BorderLayout(5, 5));
        pnRight.add(scrollUsers,  BorderLayout.CENTER);
        pnRight.add(btnChatChung, BorderLayout.SOUTH);

        // Panel trên – tiêu đề + hồ sơ
        lblDangChatVoi = new JLabel("Đang chat: Tất cả");
        lblDangChatVoi.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnProfile = new JButton("Hồ sơ");
        btnProfile.addActionListener(e -> new ProfileFrame(controller));

        JPanel pnTop = new JPanel(new BorderLayout());
        pnTop.setBorder(new EmptyBorder(6, 8, 6, 8));
        pnTop.setBackground(new Color(250, 250, 250));
        pnTop.add(lblDangChatVoi, BorderLayout.WEST);
        pnTop.add(btnProfile,     BorderLayout.EAST);

        // Panel dưới – input
        txtTinNhanNhap = new JTextField();
        txtTinNhanNhap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnGui = new JButton("Gửi");

        JButton btnFile = new JButton("📎");
        btnFile.setToolTipText("Gửi file");
        btnFile.addActionListener(e -> chonVaGuiFile());

        JPanel pnBottom = new JPanel(new BorderLayout(5, 5));
        pnBottom.setBorder(new EmptyBorder(5, 8, 8, 8));
        pnBottom.add(btnFile,        BorderLayout.WEST);
        pnBottom.add(txtTinNhanNhap, BorderLayout.CENTER);
        pnBottom.add(btnGui,         BorderLayout.EAST);

        add(pnTop,      BorderLayout.NORTH);
        add(scrollChat, BorderLayout.CENTER);
        add(pnRight,    BorderLayout.EAST);
        add(pnBottom,   BorderLayout.SOUTH);
    }

    // ── Kết nối sự kiện ─────────────────────────────────────────────────────────
    private void wireEvents() {
        btnGui.addActionListener(e -> kiemTraVaGuiTin());
        txtTinNhanNhap.addActionListener(e -> kiemTraVaGuiTin());

        listUsers.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String user = usernameFromItem(listUsers.getSelectedValue());
            if (user == null || user.equals(controller.getUsername())) {
                controller.setCurrentReceiver(null);
                lblDangChatVoi.setText("Đang chat: Tất cả");
                controller.requestPublicHistory();
            } else {
                controller.setCurrentReceiver(user);
                lblDangChatVoi.setText("Đang chat riêng với: " + user);
                controller.requestPrivateHistory(user);
            }
        });

        btnChatChung.addActionListener(e -> {
            listUsers.clearSelection();
            controller.setCurrentReceiver(null);
            lblDangChatVoi.setText("Đang chat: Tất cả");
            controller.requestPublicHistory();
        });
    }

    // ── Gửi tin nhắn text ───────────────────────────────────────────────────────
    private void kiemTraVaGuiTin() {
        String msg = txtTinNhanNhap.getText().trim();
        if (msg.isEmpty()) return;

        controller.sendTextMessage(msg);

        String receiver = controller.getCurrentReceiver();
        String label    = (receiver == null || receiver.isEmpty()) ? "Bạn" : "Bạn → " + receiver;
        bubbles.addTextBubble(label, msg, true);
        bubbles.refresh();

        txtTinNhanNhap.setText("");
        txtTinNhanNhap.requestFocus();
    }

    // ── Chọn và gửi file ────────────────────────────────────────────────────────
    private void chonVaGuiFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (file.length() > 5 * 1024 * 1024) {
            JOptionPane.showMessageDialog(this,
                    "File không được vượt quá 5MB!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        controller.sendFile(file);
        bubbles.addFileBubble("Bạn", file.getName(), file.length(), file.toPath(), true);
        bubbles.refresh();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  PUBLIC API — gọi từ ChatController (trên EDT qua SwingUtilities.invokeLater)
    // ════════════════════════════════════════════════════════════════════════════

    /** Xóa chat và hiện tiêu đề phòng/đoạn mới */
    public void clearMessages(String title) {
        SwingUtilities.invokeLater(() -> {
            chatPanel.removeAll();
            bubbles.addSystemLabel("═══  " + title + "  ═══");
            bubbles.refresh();
        });
    }

    /** Tin lịch sử — luôn căn trái */
    public void appendHistoryMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            parseAndRender(message, false);
            bubbles.refresh();
        });
    }

    /** Tin real-time */
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("[FILE] Gửi xong:")) return; // bubble đã thêm khi chọn file
            boolean mine = message.startsWith("Bạn") || message.startsWith("Ban:")
                    || message.startsWith("[Rieng toi") || message.startsWith("[Riêng tới");
            parseAndRender(message, mine);
            bubbles.refresh();
        });
    }

    public void updateOnlineList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (String u : users) if (!u.isEmpty()) model.addElement(u + " (online)");
            listUsers.setModel(model);
        });
    }

    public void updateUserStatusList(String[] userStatuses) {
        SwingUtilities.invokeLater(() -> {
            String current = usernameFromItem(listUsers.getSelectedValue());
            DefaultListModel<String> model = new DefaultListModel<>();
            for (String item : userStatuses) {
                if (item == null || item.isBlank()) continue;
                String[] p = item.split(":", 2);
                model.addElement(p[0] + " (" + (p.length > 1 ? p[1] : "offline") + ")");
            }
            listUsers.setModel(model);
            if (current != null) selectUser(current);
        });
    }

    public void logoutToLogin(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message);
            controller.closeConnection();
            dispose();
            new LoginFrame();
        });
    }

    public void updateTitle(String title) {
        SwingUtilities.invokeLater(() -> setTitle(title));
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  Parse message string → bubble
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Phân tích chuỗi message và uỷ quyền vẽ bubble cho BubbleFactory.
     *
     * Định dạng nhận:
     *   "[FILE] sender gửi: filename -> /abs/path"  → file hoặc image bubble
     *   "[File] sender gửi: filename"               → file bubble (không path)
     *   "[Rieng] / [Riêng] sender: content"         → text bubble private
     *   "=== … ===" / ">> …"                        → system label
     *   "sender: content"                           → text bubble
     */
    private void parseAndRender(String message, boolean defaultMine) {
        if (message == null || message.isBlank()) return;

        // System label
        if (message.startsWith("===") || message.startsWith(">>")
                || message.startsWith("Hệ thống:") || message.startsWith("He thong:")) {
            bubbles.addSystemLabel(message);
            return;
        }

        // File nhận: "[FILE] sender gửi: filename -> /path"
        if (message.startsWith("[FILE] ") && message.contains(" gửi: ") && message.contains(" -> ")) {
            String body     = message.substring("[FILE] ".length());
            String sender   = body.substring(0, body.indexOf(" gửi: "));
            String rest     = body.substring(body.indexOf(" gửi: ") + " gửi: ".length());
            String fileName = rest.contains(" -> ") ? rest.substring(0, rest.indexOf(" -> ")) : rest;
            String pathStr  = rest.contains(" -> ") ? rest.substring(rest.indexOf(" -> ") + " -> ".length()) : null;
            Path   savePath = pathStr != null ? Path.of(pathStr) : null;
            boolean mine    = sender.equals("Bạn") || sender.equals(controller.getUsername());

            if (savePath != null && bubbles.isImageFile(fileName)) {
                bubbles.addImageBubble(sender, fileName, savePath, mine);
            } else {
                long size = (savePath != null && savePath.toFile().exists()) ? savePath.toFile().length() : -1;
                bubbles.addFileBubble(sender, fileName, size, savePath, mine);
            }
            return;
        }

        // File gửi không có path: "[File] Bạn gửi: filename"
        if (message.startsWith("[File] ") && message.contains(" gửi: ")) {
            String rest     = message.substring("[File] ".length());
            String sender   = rest.substring(0, rest.indexOf(" gửi: "));
            String fileName = rest.substring(rest.indexOf(" gửi: ") + " gửi: ".length()).trim();
            boolean mine    = sender.equals("Bạn") || sender.equals(controller.getUsername());
            bubbles.addFileBubble(sender, fileName, -1, null, mine);
            return;
        }

        // Tin riêng: "[Rieng] sender: content"
        if (message.startsWith("[Rieng] ") || message.startsWith("[Riêng] ")) {
            int colon = message.indexOf(": ");
            bubbles.addTextBubble(
                    "🔒 " + message.substring(message.indexOf("] ") + 2, colon),
                    message.substring(colon + 2), false);
            return;
        }

        // Tin thường: "sender: content"
        int colon = message.indexOf(": ");
        if (colon > 0) {
            String sender  = message.substring(0, colon);
            String content = message.substring(colon + 2);
            boolean mine   = defaultMine
                    || sender.equals("Bạn") || sender.equals("Ban")
                    || sender.equals(controller.getUsername());
            bubbles.addTextBubble(sender, content, mine);
        } else {
            bubbles.addSystemLabel(message);
        }
    }

    // ── Helpers danh sách user ───────────────────────────────────────────────────

    private String usernameFromItem(String item) {
        if (item == null) return null;
        int idx = item.lastIndexOf(" (");
        return idx >= 0 ? item.substring(0, idx) : item;
    }

    private void selectUser(String username) {
        for (int i = 0; i < listUsers.getModel().getSize(); i++) {
            if (username.equals(usernameFromItem(listUsers.getModel().getElementAt(i)))) {
                listUsers.setSelectedIndex(i);
                return;
            }
        }
    }
}
