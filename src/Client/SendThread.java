package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class SendThread extends Thread {
    private Scanner scanner;
    private DataOutputStream dos;
    private Socket socket;

    public SendThread(DataOutputStream dos, Socket socket, Scanner scanner) {
        this.dos = dos;
        this.socket = socket;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                System.out.print("> Gửi: ");
                String msg = scanner.nextLine();

                // Kiểm tra nếu người dùng muốn thoát
                if (msg.equalsIgnoreCase("exit") || msg.equalsIgnoreCase("quit")) {
                    System.out.println(">> Đang ngắt kết nối và thoát phòng chat...");
                    break;
                }

                // Nếu tin nhắn trống thì bỏ qua không gửi
                if (msg.trim().isEmpty()) {
                    continue;
                }

                dos.writeUTF(msg);
                dos.flush();
            }
        } catch (IOException e) {
            // TODO: handle exception
            System.out.println(">> Lỗi xảy ra trong quá trình gửi tin nhắn.");
        } finally {
            // Đóng socket giải phóng tài nguyên khi luồng kết thúc (nhấn exit)
            try {
                if (dos != null)
                    dos.close();
                if (socket != null && !socket.isClosed())
                    socket.close();
                System.out.println(">> Đã ngắt kết nối an toàn. Tạm biệt!");
                System.exit(0); // Kết thúc chương trình
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
