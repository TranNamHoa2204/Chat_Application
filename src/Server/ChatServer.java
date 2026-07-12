package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatServer {
    // public static List<ClientHandler> clients = new ArrayList<>();

    // Sử dụng Collections.synchronizedList để đảm bảo an toàn khi nhiều Thread cùng ghi/đọc danh sách
    public static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    public static void main(String[] args) throws Exception{
        System.out.println("Server đang chạy, đang chờ client kết nối...");
        try (ServerSocket serverSocket = new ServerSocket(5000)) { 

            // Vòng lặp liên tục nhận và phản hồi tin nhắn của chính client này
            while (true) {
                Socket socket = serverSocket.accept();

                // Mỗi client là một handler
                ClientHandler handler = new ClientHandler(socket);

                // Thêm vào danh sách (đã được bọc synchronized tự động nhờ mảng Wrapper ở trên)
                clients.add(handler);
                handler.start();

            }
            
        } catch (IOException e) {
            System.out.println("Server xảy ra sự cố: " + e.getMessage());
        }
    
    }
}