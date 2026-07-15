package Controller;

import GUI.ChatFrame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

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
                    // else if(cmd.equals("FILE_MSG") && parts.length >=3){
                    //     String sender = parts[1];

                    //     String[] payload = parts[2].split(":",2);
                    //     String fileName = payload[0];
                    //     String base64 = payload[1];

                    //     // giải mã và lưu file
                    //     byte[] data = Base64.getDecoder().decode(base64);
                    //     Path savePath = Path.of(System.getProperty("user.home"), "Downloads", fileName);
                    //     Files.write(savePath, data);

                    //     if(view != null)
                    //         view.appendMessage("[FILE] " + sender + " gửi: " + fileName + " -> Đã lưu tại" + savePath);
                    // }

                    else if (cmd.equals("FILE_META") && parts.length >= 3) {
                        // parts[1] = sender, parts[2] = fileName:fileSize
                        String sender = parts[1];
                        String[] meta = parts[2].split(":", 2);
                        String fileName = meta[0];
                        long fileSize = Long.parseLong(meta[1]);

                        // Nhận raw bytes và lưu vào Downloads
                        Path savePath = Path.of(System.getProperty("user.home"), "Downloads", fileName);
                        try (FileOutputStream fos = new FileOutputStream(savePath.toFile())) {
                            int chunkLen;
                            while ((chunkLen = dis.readInt()) != -1) {
                                byte[] chunk = new byte[chunkLen];
                                dis.readFully(chunk);
                                fos.write(chunk);
                            }
                        }
                        // Đọc FILE_DONE
                        dis.readUTF(); // bỏ qua "FILE_DONE|..."

                        // Định dạng: "[FILE] sender gửi: filename -> /absolute/path"
                        // ChatFrame.parseAndAddBubble() sẽ nhận ra định dạng này
                        if (view != null)
                            view.appendMessage("[FILE] " + sender + " gửi: " + fileName
                                    + " -> " + savePath.toAbsolutePath());
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

    private static final int CHUNK_SIZE = 64 * 1024; // 64 KB mỗi chunk

    public void sendFile(File file) {
        new Thread(() -> {
            try {
                long fileSize = file.length();
                String receiver = (currentReceiver == null || currentReceiver.isEmpty())
                        ? "PUBLIC" : currentReceiver;

                // 1. Báo hiệu bắt đầu
                dos.writeUTF("FILE_START|" + file.getName() + "|" + fileSize + "|" + receiver);
                dos.flush();

                // 2. Gửi raw bytes theo từng chunk
                byte[] buffer = new byte[CHUNK_SIZE];
                try (FileInputStream fis = new FileInputStream(file)) {
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.writeInt(bytesRead);        // độ dài chunk này
                        dos.write(buffer, 0, bytesRead); // dữ liệu thực
                        dos.flush();
                    }
                }

                // 3. Báo hiệu kết thúc
                dos.writeInt(-1); // sentinel = hết file
                dos.writeUTF("FILE_END|" + file.getName());
                dos.flush();

                if (view != null)
                    view.appendMessage("[FILE] Gửi xong: " + file.getName());

            } catch (IOException e) {
                if (view != null)
                    view.appendMessage(">> Lỗi gửi file: " + e.getMessage());
            }
        }).start(); // chạy trên thread riêng để không đơ UI
    }


}
