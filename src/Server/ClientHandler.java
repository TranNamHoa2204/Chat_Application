package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String username; // Thêm thuộc tính lưu username

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            // 1. Đọc tin nhắn đầu tiên từ Client, đó chính là username
            this.username = dis.readUTF();
            System.out.println("Client [" + this.username + "] đã tham gia phòng chat.");

            // Vòng lặp xử lý tin nhắn chat
            while (true) {
                // Đọc tin nhắn gốc của user
                String msg = dis.readUTF();

                // 2. Ghép username vào trước tin nhắn
                String formattedMsg = this.username + ": " + msg;

                for (ClientHandler client : ChatServer.clients) {
                    if (client != this) { 
                        // Gửi tin nhắn đã có kèm tên cho những người khác
                        client.sendMessage(formattedMsg);
                        //sendMessage(formattedMsg); không thì chỉ gửi chính mình
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client [" + (this.username != null ? this.username : socket.getInetAddress()) + "] đã ngắt kết nối.");
        } finally {
            closeEverything();
        }
    }

    public void sendMessage(String message) {
        try {
            // dos: luồng dữ liệu của CLIENT HIỆN TẠI
            if (dos != null) { 
                dos.writeUTF(message);
                dos.flush();
            }
        } catch (IOException e) {
            System.out.println("Lỗi gửi tin nhắn tới: " + this.username);
        }
    }

    public void closeEverything() {
        // CẬP NHẬT: Xóa khỏi danh sách trước rồi mới gửi thông báo thoát
        synchronized (ChatServer.clients){
            ChatServer.clients.remove(this);
        }
        if(this.username != null){
            System.out.println("User: "+ this.username + " đã thoát phòng chat.");
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

    public void broadcastMessage(String message){
        synchronized (ChatServer.clients){
            for(ClientHandler client : ChatServer.clients){
                if(client != this){
                    client.sendMessage(message);
                }
            }
        }
    }
}

