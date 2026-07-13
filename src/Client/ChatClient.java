package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {

            // 1. Nhập username và password trước
            System.out.print("Nhập username của bạn: ");
            String username = scanner.nextLine();
            System.out.print("Nhập password của bạn: ");
            String password = scanner.nextLine();

            Socket socket = new Socket("localhost", 5000);
            System.out.println("Đã kết nối tới Server. Đang xác thực...");

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // 2. Thực hiện đăng nhập theo giao thức
            dos.writeUTF("LOGIN|" + username + "|" + password);
            dos.flush();

            String response = dis.readUTF();
            if ("LOGIN_SUCCESS".equals(response)) {
                System.out.println("Đăng nhập thành công!");
                ReceiveThread receiveThread = new ReceiveThread(dis, socket);
                receiveThread.start();

                SendThread sendThread = new SendThread(dos, socket, scanner);
                sendThread.start();
            } else {
                String reason = response.startsWith("LOGIN_FAIL|") ? response.split("\\|")[1] : "Không xác định";
                System.out.println("Đăng nhập thất bại: " + reason);
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("Kết nối thất bại hoặc bị ngắt: " + e.getMessage());
        }
    }
}