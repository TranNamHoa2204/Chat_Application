package GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory tạo các loại bubble hiển thị trong khung chat.
 * Nhận chatPanel từ ngoài và add component trực tiếp vào đó.
 */
public class BubbleFactory {

    // ── Màu sắc ──────────────────────────────────────────────────────────────────
    static final Color COLOR_BUBBLE_MINE  = new Color(0, 132, 255);
    static final Color COLOR_BUBBLE_OTHER = new Color(230, 230, 230);
    static final Color COLOR_TEXT_MINE    = Color.WHITE;
    static final Color COLOR_TEXT_OTHER   = new Color(30, 30, 30);
    static final Color COLOR_BG_CHAT      = new Color(245, 245, 245);
    static final Color COLOR_SYSTEM       = new Color(150, 150, 150);

    // ── Đuôi file ảnh ────────────────────────────────────────────────────────────
    private static final Set<String> IMAGE_EXTS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp"));

    private final JPanel      chatPanel;
    private final JScrollPane scrollChat;
    private final JFrame      owner;       // dùng cho JOptionPane khi lỗi mở file

    public BubbleFactory(JPanel chatPanel, JScrollPane scrollChat, JFrame owner) {
        this.chatPanel  = chatPanel;
        this.scrollChat = scrollChat;
        this.owner      = owner;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  Public: thêm các loại bubble
    // ════════════════════════════════════════════════════════════════════════════

    /** Nhãn hệ thống — căn giữa, chữ xám nghiêng */
    public void addSystemLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl.setForeground(COLOR_SYSTEM);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
        chatPanel.add(lbl);
    }

    /** Bubble tin nhắn văn bản */
    public void addTextBubble(String sender, String content, boolean mine) {
        Color bgColor   = mine ? COLOR_BUBBLE_MINE  : COLOR_BUBBLE_OTHER;
        Color textColor = mine ? COLOR_TEXT_MINE     : COLOR_TEXT_OTHER;

        JLabel lblSender = senderLabel(sender, mine);

        JLabel lblContent = new JLabel("<html><body style='width:260px'>"
                + escapeHtml(content) + "</body></html>");
        lblContent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblContent.setForeground(textColor);

        JPanel bubble = createBubblePanel(bgColor);
        bubble.add(lblSender);
        bubble.add(Box.createVerticalStrut(2));
        bubble.add(lblContent);

        append(bubble, mine);
    }

    /** Bubble file: icon 📎 + tên + size + nút "Mở file" */
    public void addFileBubble(String sender, String fileName, long fileSize,
                               Path savePath, boolean mine) {
        Color bgColor   = mine ? COLOR_BUBBLE_MINE  : COLOR_BUBBLE_OTHER;
        Color textColor = mine ? COLOR_TEXT_MINE     : COLOR_TEXT_OTHER;

        JLabel lblIcon = new JLabel("📎 " + fileName);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIcon.setForeground(textColor);

        JLabel lblSize = new JLabel(fileSize >= 0 ? formatSize(fileSize) : "");
        lblSize.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSize.setForeground(mine ? new Color(200, 230, 255) : new Color(120, 120, 120));

        JPanel bubble = createBubblePanel(bgColor);
        bubble.add(senderLabel(sender, mine));
        bubble.add(Box.createVerticalStrut(3));
        bubble.add(lblIcon);
        if (fileSize >= 0) bubble.add(lblSize);

        if (savePath != null && savePath.toFile().exists()) {
            JButton btnOpen = createActionButton("📂 Mở file", mine);
            btnOpen.addActionListener(e -> openFile(savePath.toFile()));
            bubble.add(Box.createVerticalStrut(4));
            bubble.add(btnOpen);
        }

        append(bubble, mine);
    }

    /** Bubble ảnh: thumbnail + nút "Xem ảnh" */
    public void addImageBubble(String sender, String fileName, Path savePath, boolean mine) {
        Color bgColor   = mine ? COLOR_BUBBLE_MINE  : COLOR_BUBBLE_OTHER;
        Color textColor = mine ? COLOR_TEXT_MINE     : COLOR_TEXT_OTHER;

        JPanel bubble = createBubblePanel(bgColor);
        bubble.add(senderLabel(sender, mine));
        bubble.add(Box.createVerticalStrut(4));

        JLabel thumbnail = buildThumbnail(savePath, 220, 160);
        if (thumbnail != null) {
            bubble.add(thumbnail);
        } else {
            JLabel fallback = new JLabel("🖼 " + fileName);
            fallback.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fallback.setForeground(textColor);
            bubble.add(fallback);
        }

        JButton btnOpen = createActionButton("🔍 Xem ảnh", mine);
        btnOpen.addActionListener(e -> openFile(savePath.toFile()));
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(btnOpen);

        append(bubble, mine);
    }

    // ── Public utilities ─────────────────────────────────────────────────────────

    public boolean isImageFile(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return false;
        return IMAGE_EXTS.contains(fileName.substring(dot + 1).toLowerCase());
    }

    public static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br>");
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024)        return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    /** Cuộn xuống cuối và repaint */
    public void refresh() {
        chatPanel.revalidate();
        chatPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollChat.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  Private helpers
    // ════════════════════════════════════════════════════════════════════════════

    /** Thêm bubble + khoảng cách vào chatPanel */
    private void append(JPanel bubble, boolean mine) {
        chatPanel.add(wrapBubble(bubble, mine));
        chatPanel.add(Box.createVerticalStrut(4));
    }

    /** JPanel bo góc làm nền bubble */
    private JPanel createBubblePanel(Color bg) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bg);
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));
        panel.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));
        return panel;
    }

    /** Row wrapper căn trái (other) hoặc phải (mine) */
    private JPanel wrapBubble(JPanel bubble, boolean mine) {
        JPanel row = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        row.add(bubble);
        return row;
    }

    /** Label tên người gửi nhỏ bên trên nội dung */
    private JLabel senderLabel(String sender, boolean mine) {
        JLabel lbl = new JLabel(sender);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(mine ? new Color(180, 220, 255) : new Color(100, 100, 100));
        return lbl;
    }

    /** Nút hành động nhỏ bên trong bubble */
    private JButton createActionButton(String text, boolean mine) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(mine ? new Color(0, 100, 210) : new Color(210, 210, 210));
        btn.setForeground(mine ? Color.WHITE : new Color(30, 30, 30));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    /** Scale ảnh thành thumbnail, trả null nếu không đọc được */
    private JLabel buildThumbnail(Path path, int maxW, int maxH) {
        try {
            BufferedImage img = ImageIO.read(path.toFile());
            if (img == null) return null;
            double scale = Math.min((double) maxW / img.getWidth(),
                                    (double) maxH / img.getHeight());
            int w = (int) (img.getWidth()  * scale);
            int h = (int) (img.getHeight() * scale);
            JLabel lbl = new JLabel(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            return lbl;
        } catch (IOException e) {
            return null;
        }
    }

    /** Mở file bằng ứng dụng mặc định của OS */
    private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException | UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(owner,
                    "Không thể mở file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
