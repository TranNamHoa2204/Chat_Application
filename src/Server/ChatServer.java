package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    public static List<ClientHandler> clients = new ArrayList<>();
    public static void main(String[] args) throws Exception{
        System.out.println("Server đang chạy, đang chờ client kết nối...");
        try {
            ServerSocket serverSocket = new ServerSocket(5000);

            // Vòng lặp liên tục nhận và phản hồi tin nhắn của chính client này
            while (true) {
                Socket socket = serverSocket.accept();

                // Mỗi client là một handler
                ClientHandler handler = new ClientHandler(socket);

                clients.add(handler);

                handler.start();

            }
            
        } catch (IOException e) {
            System.out.println("Client đã ngắt kết nối hoặc có lỗi xảy ra.");
        }
    }
}