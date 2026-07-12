package Controller;

import GUI.ChatFrame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatController {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;
    private ChatFrame view; // Giữ liên kết tới giao diện để đổ dữ liệu lên

    public ChatController(String host, int port, String username) throws IOException {
        this.username = username;
        this.socket = new Socket(host, port);
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        
        // Gửi username lên server ngay khi kết nối thành công
        this.dos.writeUTF(username);
        this.dos.flush();
    }

    public void setView(ChatFrame view) {
        this.view = view;
    }

    public String getUsername() {
        return username;
    }

    // Logic gửi tin nhắn được chuyển về đây
    public void sendTextMessage(String msg) {
        try {
            dos.writeUTF(msg);
            dos.flush();
        } catch (IOException e) {
            if (view != null) view.appendMessage(">> Lỗi: Không thể gửi tin nhắn.");
        }
    }

    // Luồng nhận tin nhắn được quản lý tại đây
    public void startListening() {
        Thread receiveThread = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    String msgText = dis.readUTF();
                    if (view != null) {
                        view.appendMessage(msgText); // Gọi giao diện hiển thị hộ
                    }
                }
            } catch (IOException e) {
                if (view != null) view.appendMessage(">> Mất kết nối tới máy chủ.");
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void closeConnection() {
        try {
            if (dos != null) dos.close();
            if (dis != null) dis.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}