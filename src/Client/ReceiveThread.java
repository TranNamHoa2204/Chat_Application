package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ReceiveThread extends Thread {
    private DataInputStream dis;
    private Socket socket;

    public ReceiveThread(DataInputStream dis, Socket socket) {
        this.dis = dis;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                String rawMsg = dis.readUTF();
                String[] parts = rawMsg.split("\\|", 3);
                if (parts.length >= 3 && "MSG".equals(parts[0])) {
                    String sender = parts[1];
                    String content = parts[2];
                    // In tin nhắn có định dạng rõ ràng
                    System.out.println("\n" + sender + ": " + content);
                    System.out.print("> Gửi: "); // Nhắc lại dòng lệnh để User biết mình vẫn đang gõ ở đâu
                }
            }
        } catch (IOException e) {
            if(!socket.isClosed()){
                System.out.println("\n>> Mất kết nối tới Server hoặc Server đã đóng.");
            }
        }
    }
}