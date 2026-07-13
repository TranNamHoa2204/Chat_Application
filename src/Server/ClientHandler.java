package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import DAO.UserDAO;
import DAO.MessageDAO;

public class ClientHandler extends Thread {
    private Socket socket;
    private DataOutputStream dos;
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
                String[] parts = rawMsg.split("\\|", 2);
                if (parts.length < 2) continue;
                String cmd = parts[0];

                if (cmd.equals("PUB_MSG")) {
                    String content = parts[1];
                    
                    // Lưu tin nhắn vào cơ sở dữ liệu
                    int senderId = UserDAO.getUserId(this.username);
                    if (senderId != -1) {
                        MessageDAO.luuTinNhan(senderId, null, content);
                    }

                    // Forward gói tin tới tất cả Client khác
                    String formattedMsg = "MSG|" + this.username + "|" + content;
                    System.out.println(this.username + " (Public): " + content);
                    broadcastMessage(formattedMsg);
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

    public static void broadcastOnlineList() {
        StringBuilder sb = new StringBuilder("ONLINE_LIST|");
        synchronized (ChatServer.clients) {
            boolean first = true;
            for (ClientHandler client : ChatServer.clients) {
                if (client.username != null) {
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append(client.username);
                    first = false;
                }
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
}
