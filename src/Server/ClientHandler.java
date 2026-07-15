package Server;

import DAO.MessageDAO;
import DAO.UserDAO;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    public DataOutputStream dos;
    private DataInputStream dis;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            boolean authenticated = false;

            // Vòng lặp xác thực (Đăng nhập / Đăng ký) trước khi được chat
            while (!authenticated) {
                String rawMsg = dis.readUTF();
                String[] parts = rawMsg.split("\\|", 3);
                if (parts.length < 2) {
                    continue;
                }
                String cmd = parts[0];

                if (cmd.equals("LOGIN")) {
                    if (parts.length < 3) {
                        continue;
                    }
                    String user = parts[1];
                    String pass = parts[2];
                    boolean success = UserDAO.kiemTraDangNhap(user, pass);
                    if (success) {
                        this.username = user;
                        dos.writeUTF("LOGIN_SUCCESS");
                        dos.flush();
                        authenticated = true;
                        System.out.println("Client [" + this.username + "] đăng nhập hệ thống thành công.");
                    } else {
                        dos.writeUTF("LOGIN_FAIL|Sai tên đăng nhập hoặc mật khẩu.");
                        dos.flush();
                    }
                } else if (cmd.equals("REGISTER")) {
                    if (parts.length < 3) {
                        continue;
                    }
                    String user = parts[1];
                    String pass = parts[2];
                    boolean success = UserDAO.dangKy(user, pass);
                    if (success) {
                        dos.writeUTF("REGISTER_SUCCESS");
                        dos.flush();
                    } else {
                        dos.writeUTF("REGISTER_FAIL|Tên đăng nhập đã tồn tại hoặc xảy ra lỗi.");
                        dos.flush();
                    }
                }
            }

            // Gửi thông báo toàn phòng khi đăng nhập thành công
            broadcastMessage("MSG|Hệ thống|" + this.username + " đã tham gia phòng chat.");

            // Cập nhật danh sách online tới tất cả mọi người
            broadcastOnlineList();

            // Vòng lặp xử lý tin nhắn chat từ client
            while (true) {
                String rawMsg = dis.readUTF();
                String[] parts = rawMsg.split("\\|", 3);
                if (parts.length < 2)
                    continue;
                String cmd = parts[0];

                if (cmd.equals("PUB_MSG")) {
                    if (parts.length < 2)
                        continue;

                    String content = parts[1];

                    int senderId = UserDAO.getUserId(this.username);
                    if (senderId != -1) {
                        MessageDAO.luuTinNhan(senderId, null, content);
                    }

                    String formattedMsg = "MSG|" + this.username + "|" + content;
                    System.out.println(this.username + " (Public): " + content);
                    broadcastMessage(formattedMsg);
                }

                else if (cmd.equals("PRI_MSG")) {
                    if (parts.length < 3)
                        continue;

                    String receiverUsername = parts[1];
                    String content = parts[2];

                    sendPrivateMessage(receiverUsername, content);
                } else if (cmd.equals("LOAD_PUBLIC_HISTORY")) {
                    sendPublicHistory();
                } else if (cmd.equals("LOAD_PRIVATE_HISTORY")) {
                    if (parts.length < 2)
                        continue;

                    sendPrivateHistory(parts[1]);
                } else if (cmd.equals("CHANGE_PASSWORD")) {
                    if (parts.length < 3) continue;
                    String oldPass = parts[1];
                    String newPass = parts[2];
                    boolean ok = UserDAO.doiMatKhau(this.username, oldPass, newPass);
                    if (ok) {
                        sendMessage("CHANGE_SUCCESS|Đổi mật khẩu thành công.");
                    } else {
                        sendMessage("CHANGE_FAIL|Mật khẩu cũ không đúng hoặc xảy ra lỗi.");
                    }
                }

                else if (cmd.equals("CHANGE_USERNAME")) {
                    if (parts.length < 3) continue;
                    String newUsername = parts[1];
                    String password = parts[2];
                    boolean ok = UserDAO.doiUsername(this.username, newUsername, password);
                    if (ok) {
                        String oldUsername = this.username;
                        this.username = newUsername; // Cập nhật username trong session
                        broadcastMessage("MSG|Hệ thống|" + oldUsername + " đã đổi tên thành " + newUsername);
                        broadcastOnlineList(); // Cập nhật danh sách
                        sendMessage("CHANGE_SUCCESS|Đổi username thành công. Username mới: " + newUsername);
                    } else {
                        sendMessage("CHANGE_FAIL|Mật khẩu không đúng hoặc username đã tồn tại.");
                    }
                }
                else if (cmd.equals("DELETE_ACCOUNT")) {
                    if (parts.length < 2) continue;
                    String password = parts[1];
                    
                    // Gọi sang DAO để kiểm tra và xóa
                    boolean ok = UserDAO.xoaTaiKhoan(this.username, password);
                    if (ok) {
                        sendMessage("DELETE_SUCCESS|Tài khoản của bạn đã bị xóa hệ thống thành công.");
                        broadcastMessage("MSG|Hệ thống|" + this.username + " đã bị xóa tài khoản.");
                        closeEverything(); // Đóng kết nối của user này
                    } else {
                        sendMessage("CHANGE_FAIL|Mật khẩu xác nhận không đúng hoặc lỗi CSDL.");
                    }
                } 
                // else if (cmd.equals("SEND_FILE")) {
                //     if (parts.length < 3) continue;
                //     String fileName = parts[1];
                //     String base64   = parts[2];
                //     // Broadcast tới người khác (hoặc gửi riêng nếu cần)
                //     broadcastMessage("FILE_MSG|" + this.username 
                //                     + "|" + fileName + ":" + base64);
                // }
                else if (cmd.equals("FILE_START")) {
                    // parts[1]=fileName, parts[2]=fileSize, parts[3]=receiver
                    if (parts.length < 3) continue;
                    String[] subParts = rawMsg.split("\\|", 4);
                    String fileName = subParts[1];
                    long fileSize   = Long.parseLong(subParts[2]);
                    String receiver = subParts.length > 3 ? subParts[3] : "PUBLIC";

                    // Đọc toàn bộ bytes từ client gửi lên
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int chunkLen;
                    while ((chunkLen = dis.readInt()) != -1) {
                        byte[] chunk = new byte[chunkLen];
                        dis.readFully(chunk);
                        baos.write(chunk);
                    }
                    dis.readUTF(); // bỏ FILE_END

                    byte[] fileData = baos.toByteArray();

                    // Lưu lịch sử file vào DB
                    int senderId = UserDAO.getUserId(this.username);
                    if ("PUBLIC".equals(receiver)) {
                        if (senderId != -1) {
                            MessageDAO.luuLichSuFile(senderId, null, fileName, fileSize);
                        }
                        // Relay tới tất cả
                        broadcastFile(this.username, fileName, fileData);
                    } else {
                        int receiverId = UserDAO.getUserId(receiver);
                        if (senderId != -1 && receiverId != -1) {
                            MessageDAO.luuLichSuFile(senderId, receiverId, fileName, fileSize);
                        }
                        // Gửi riêng
                        ClientHandler target = findClientByUsername(receiver);
                        if (target != null) {
                            sendFileTo(target, this.username, fileName, fileData);
                        } else {
                            sendMessage("MSG|Hệ thống|" + receiver + " không online, không thể gửi file.");
                        }
                    }
                }




            }
        } catch (IOException e) {
            System.out.println("Client [" + (this.username != null ? this.username : socket.getInetAddress())
                    + "] đã ngắt kết nối.");
        } finally {
            closeEverything();
        }
    }

    public void sendMessage(String message) {
        try {
            if (dos != null) {
                dos.writeUTF(message);
                dos.flush();
            }
        } catch (IOException e) {
            System.out.println("Lỗi gửi tin nhắn tới: " + this.username);
        }
    }

    public void closeEverything() {
        synchronized (ChatServer.clients) {
            ChatServer.clients.remove(this);
        }
        if (this.username != null) {
            System.out.println("User: " + this.username + " đã thoát phòng chat.");
            broadcastMessage("MSG|Hệ thống|" + this.username + " đã rời phòng chat.");
            broadcastOnlineList(); // Cập nhật danh sách online mới sau khi thoát
        }
        try {
            if (dis != null)
                dis.close();
            if (dos != null)
                dos.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        synchronized (ChatServer.clients) {
            for (ClientHandler client : ChatServer.clients) {
                if (client != this && client.username != null) {
                    client.sendMessage(message);
                }
            }
        }
    }

    private void sendPrivateMessage(String receiverUsername, String content) {
        ClientHandler receiver = findClientByUsername(receiverUsername);

        if (receiver == null) {
            sendMessage("MSG|Hệ thống|Người dùng " + receiverUsername + " hiện không online.");
            return;
        }

        int senderId = UserDAO.getUserId(this.username);
        int receiverId = UserDAO.getUserId(receiverUsername);

        if (senderId != -1 && receiverId != -1) {
            MessageDAO.luuTinNhan(senderId, receiverId, content);
        }

        String messageToReceiver = "PRIVATE_MSG|" + this.username + "|" + content;
        receiver.sendMessage(messageToReceiver);

        System.out.println(this.username + " -> " + receiverUsername + " (Private): " + content);
    }

    private ClientHandler findClientByUsername(String username) {
        synchronized (ChatServer.clients) {
            for (ClientHandler client : ChatServer.clients) {
                if (client.username != null && client.username.equals(username)) {
                    return client;
                }
            }
        }
        return null;
    }

    private void sendPublicHistory() {
        sendMessage("HISTORY_CLEAR|Chat chung");
        for (String[] msg : MessageDAO.layLichSuChatChung()) {
            sendMessage("HISTORY_MSG|" + msg[0] + "|" + msg[1]);
        }
    }

    private void sendPrivateHistory(String otherUsername) {
        sendMessage("HISTORY_CLEAR|Chat rieng voi " + otherUsername);
        int currentUserId = UserDAO.getUserId(this.username);
        int otherUserId = UserDAO.getUserId(otherUsername);

        if (currentUserId == -1 || otherUserId == -1) {
            return;
        }

        for (String[] msg : MessageDAO.layLichSuChatRieng(currentUserId, otherUserId)) {
            sendMessage("HISTORY_MSG|" + msg[0] + "|" + msg[1]);
        }
    }

    public static void broadcastOnlineList() {
        StringBuilder sb = new StringBuilder("USER_STATUS_LIST|");
        java.util.List<String> allUsers = UserDAO.getAllUsernames();

        synchronized (ChatServer.clients) {
            boolean first = true;
            for (String user : allUsers) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(user).append(":").append(isUserOnline(user) ? "online" : "offline");
                first = false;
            }
        }

        String listMsg = sb.toString();
        synchronized (ChatServer.clients) {
            for (ClientHandler client : ChatServer.clients) {
                if (client.username != null) {
                    client.sendMessage(listMsg);
                }
            }
        }
    }

    private static boolean isUserOnline(String username) {
        for (ClientHandler client : ChatServer.clients) {
            if (client.username != null && client.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void broadcastFile(String sender, String fileName, byte[] data) throws IOException {
        synchronized (ChatServer.clients) {
            for (ClientHandler client : ChatServer.clients) {
                if (client != this && client.username != null) {
                    sendFileTo(client, sender, fileName, data);
                }
            }
        }
    }

    private void sendFileTo(ClientHandler target, String sender,
                            String fileName, byte[] data) throws IOException {
        DataOutputStream targetDos = target.dos;
        synchronized (targetDos) {
            targetDos.writeUTF("FILE_META|" + sender + "|" + fileName + ":" + data.length);
            targetDos.flush();

            int chunkSize = 64 * 1024;
            int offset = 0;
            while (offset < data.length) {
                int len = Math.min(chunkSize, data.length - offset);
                targetDos.writeInt(len);
                targetDos.write(data, offset, len);
                offset += len;
            }
            targetDos.writeInt(-1); // sentinel
            targetDos.writeUTF("FILE_DONE|" + fileName);
            targetDos.flush();
        }
    }

}
