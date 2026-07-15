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
    private ChatFrame view;

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
            String reason = response.split("\\|", 2)[1];
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
            String reason = response.split("\\|", 2)[1];
            throw new IOException(reason);
        }
        return false;
    }

    public void deleteAccount(String password) {
        try {
            // Gửi lệnh xóa kèm theo mật khẩu xác nhận
            dos.writeUTF("DELETE_ACCOUNT|" + password);
            dos.flush();
        } catch (IOException e) {
            if (view != null)
                view.appendMessage(">> Lỗi: Không thể gửi yêu cầu xóa tài khoản.");
        }
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
                view.appendMessage(">> Loi: Khong the gui tin nhan.");
            }
        }
    }

    public void requestPublicHistory() {
        try {
            dos.writeUTF("LOAD_PUBLIC_HISTORY|");
            dos.flush();
        } catch (IOException e) {
            if (view != null) {
                view.appendMessage(">> Loi: Khong the tai lich su chat chung.");
            }
        }
    }

    public void requestPrivateHistory(String otherUsername) {
        try {
            dos.writeUTF("LOAD_PRIVATE_HISTORY|" + otherUsername);
            dos.flush();
        } catch (IOException e) {
            if (view != null) {
                view.appendMessage(">> Loi: Khong the tai lich su chat rieng.");
            }
        }
    }

    public void startListening() {
        Thread receiveThread = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    String rawMsg = dis.readUTF();
                    String[] parts = rawMsg.split("\\|", 3);
                    if (parts.length < 2) {
                        continue;
                    }

                    String cmd = parts[0];
                    if (cmd.equals("MSG") && parts.length >= 3) {
                        String sender = parts[1];
                        String content = parts[2];
                        if (view != null) {
                            view.appendMessage(sender + ": " + content);
                        }
                    } else if (cmd.equals("PRIVATE_MSG") && parts.length >= 3) {
                        String sender = parts[1];
                        String content = parts[2];
                        if (view != null) {
                            view.appendMessage("[Rieng] " + sender + ": " + content);
                        }
                    } else if (cmd.equals("HISTORY_CLEAR")) {
                        String title = parts[1];
                        if (view != null) {
                            view.clearMessages(title);
                        }
                    } else if (cmd.equals("HISTORY_MSG") && parts.length >= 3) {
                        String sender = parts[1];
                        String content = parts[2];
                        if (view != null) {
                            view.appendHistoryMessage(sender + ": " + content);
                        }
                    } else if (cmd.equals("USER_STATUS_LIST")) {
                        String listStr = parts[1];
                        if (view != null) {
                            view.updateUserStatusList(listStr.split(","));
                        }
                    } else if (cmd.equals("ONLINE_LIST")) {
                        String listStr = parts[1];
                        if (view != null) {
                            view.updateOnlineList(listStr.split(","));
                        }
                    } else if (cmd.equals("CHANGE_SUCCESS")) {
                        String msg = parts[1];
                        if (view != null)
                            view.appendMessage(">> " + msg);
                        // Nếu đổi username thành công, cập nhật lại title
                        if (msg.contains("username")) {
                            String newName = msg.replace("Đổi username thành công. Username mới: ", "").trim();
                            this.username = newName;
                            if (view != null)
                                view.updateTitle("Messenger - " + newName);
                        }
                    } else if (cmd.equals("CHANGE_FAIL")) {
                        String reason = parts[1];
                        if (view != null)
                            view.appendMessage(">> Thất bại: " + reason);
                    } else if (cmd.equals("DELETE_SUCCESS")) {
                        String msg = parts.length > 1
                                ? parts[1]
                                : "Tài khoản đã được xóa.";

                        if (view != null) {
                            view.logoutToLogin(msg);
                        }
                    } else if (cmd.equals("DELETE_FAIL")) {
                        String reason = parts.length > 1 ? parts[1] : "Xóa tài khoản thất bại.";
                        if (view != null)
                            view.appendMessage(">> Lỗi xóa tài khoản: " + reason);
                    }

                }
            } catch (IOException e) {
                if (view != null) {
                    view.appendMessage(">> Mat ket noi toi may chu.");
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
        requestPublicHistory();
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

    public void changePassword(String oldPassword, String newPassword) {
        try {
            dos.writeUTF("CHANGE_PASSWORD|" + oldPassword + "|" + newPassword);
            dos.flush();
        } catch (IOException e) {
            if (view != null)
                view.appendMessage(">> Lỗi: Không thể gửi yêu cầu đổi mật khẩu.");
        }
    }

    public void changeUsername(String newUsername, String password) {
        try {
            dos.writeUTF("CHANGE_USERNAME|" + newUsername + "|" + password);
            dos.flush();
        } catch (IOException e) {
            if (view != null)
                view.appendMessage(">> Lỗi: Không thể gửi yêu cầu đổi username.");
        }
    }

}
