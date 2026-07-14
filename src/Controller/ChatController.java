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
    private String currentReceiver;
    private ChatFrame view; // Giữ liên kết tới giao diện để đổ dữ liệu lên

    public ChatController(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
    }

    public boolean login(String username, String password) throws IOException {
        this.username = username;
        dos.writeUTF("LOGIN|" + username + "|" + password);
        dos.flush();

        String response = dis.readUTF();
        if ("LOGIN_SUCCESS".equals(response)) {
            return true;
        } else if (response.startsWith("LOGIN_FAIL|")) {
            String reason = response.split("\\|")[1];
            throw new IOException(reason);
        }
        return false;
    }

    public boolean register(String username, String password) throws IOException {
        dos.writeUTF("REGISTER|" + username + "|" + password);
        dos.flush();

        String response = dis.readUTF();
        if ("REGISTER_SUCCESS".equals(response)) {
            return true;
        } else if (response.startsWith("REGISTER_FAIL|")) {
            String reason = response.split("\\|")[1];
            throw new IOException(reason);
        }
        return false;
    }

    public void setCurrentReceiver(String currentReceiver) {
        this.currentReceiver = currentReceiver;
    }

    public String getCurrentReceiver() {
        return currentReceiver;
    }

    public void setView(ChatFrame view) {
        this.view = view;
    }

    public String getUsername() {
        return username;
    }

    // Logic gửi tin nhắn được chuyển về đây
    // public void sendTextMessage(String msg) {
    //     try {
    //         dos.writeUTF("PUB_MSG|" + msg);
    //         dos.flush();
    //     } catch (IOException e) {
    //         if (view != null)
    //             view.appendMessage(">> Lỗi: Không thể gửi tin nhắn.");
    //     }
    // }

    public void sendTextMessage(String msg) {
        try {
            if (currentReceiver == null || currentReceiver.isEmpty()) {
                dos.writeUTF("PUB_MSG|" + msg);
            } else {
                dos.writeUTF("PRI_MSG|" + currentReceiver + "|" + msg);
            }
            dos.flush();
        } catch (IOException e) {
            if (view != null) {
                view.appendMessage(">> Lỗi: Không thể gửi tin nhắn.");
            }
        }
    }

    // Luồng nhận tin nhắn được quản lý tại đây
    public void startListening() {
        Thread receiveThread = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    String rawMsg = dis.readUTF();
                    String[] parts = rawMsg.split("\\|", 3);
                    if (parts.length >= 2) {
                        String cmd = parts[0];
                        if (cmd.equals("MSG")) {
                            String sender = parts[1];
                            String content = parts[2];
                            if (view != null) {
                                view.appendMessage(sender + ": " + content);
                            }
                            
                        } else if (cmd.equals("PRIVATE_MSG")) {
                            String sender = parts[1];
                            String content = parts[2];
                            if (view != null) {
                                view.appendMessage("[Riêng] " + sender + ": " + content);
                            }

                        } else if (cmd.equals("ONLINE_LIST")) {
                            String listStr = parts[1];
                            if (view != null) {
                                view.updateOnlineList(listStr.split(","));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (view != null)
                    view.appendMessage(">> Mất kết nối tới máy chủ.");
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void closeConnection() {
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