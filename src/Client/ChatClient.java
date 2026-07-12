package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            
            // 1. Nhập username trước
            System.out.print("Nhập username của bạn: ");
            String username = scanner.nextLine();

            Socket socket = new Socket("localhost", 5000);
            System.out.println("Đã kết nối tới Server.");

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // 2. Gửi username lên Server ngay lập tức (đây sẽ là tin nhắn đầu tiên Server nhận được)
            dos.writeUTF(username);
            dos.flush();

            ReceiveThread receiveThread = new ReceiveThread(dis);
            receiveThread.start();

            SendThread sendThread = new SendThread(dos);
            sendThread.start();

        } catch (IOException e) {
            System.out.println("Kết nối thất bại hoặc bị ngắt.");
        }
    }
}