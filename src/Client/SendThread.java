package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class SendThread extends Thread {
    private String msg;
    private Scanner scanner;
    private DataOutputStream dos;

    public SendThread(DataOutputStream dos) {
        this.dos = dos;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            while (true) {

                System.out.println("Nhập tin nhắn: ");
                msg = scanner.nextLine();

                dos.writeUTF(msg);
                dos.flush();
            }
        } catch (IOException e) {
            // TODO: handle exception
            System.out.println("Lỗi gửi tin nhắn");
        }

    }
}
