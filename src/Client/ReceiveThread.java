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
                String message = dis.readUTF();
                // In thẳng tin nhắn nhận được ra màn hình
                System.out.println("\n" + message);
                System.out.print("> Gửi: "); // Nhắc lại dòng lệnh để User biết mình vẫn đang gõ ở đâu
            }
        } catch (IOException e) {
            if(!socket.isClosed()){
                System.out.println("\n>> Mất kết nối tới Server hoặc Server đã đóng.");
            }
        }
    }
}