package Client;

import java.io.DataInputStream;
import java.io.IOException;

public class ReceiveThread extends Thread {
    private DataInputStream dis;

    public ReceiveThread(DataInputStream dis) {
        this.dis = dis;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = dis.readUTF();
                System.out.println(message);
            }
        } catch (IOException e) {
            System.out.println("Mất kết nối tới Server hoặc Server đã đóng.");
        }
    }
}