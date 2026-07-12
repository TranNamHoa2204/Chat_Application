package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class client2 {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Nhập username của bạn: ");
            String username = scanner.nextLine();

            Socket socket = new Socket("localhost", 5000);
            System.out.println("Đã kết nối tới Server.");

            // Khởi tạo luồng vào/ra (Chỉ tạo 1 lần duy nhất)
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF(username);
            dos.flush();

            // Khởi động thread nhận dữ liệu từ Server (Chỉ chạy 1 lần duy nhất)
            ReceiveThread receiveThread = new ReceiveThread(dis, socket);
            receiveThread.start();

            SendThread sendThread = new SendThread(dos, socket, scanner);
            sendThread.start();

        } catch (IOException e) {
            System.out.println("Kết nối thất bại hoặc bị ngắt.");
        }
    }

}
