package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class SendThread extends Thread{
    private String msg;
    private String username;
    private Scanner scanner;
    private DataOutputStream dos;
    

    public void SendThread(DataOutputStream dos){
        this.dos =  dos;
    }

    @Override
    public void run() {
        // Sửa chỗ này

        System.out.println("Vui lòng nhập tên: ");
        username = scanner.nextLine();

        dos.writeUTF(username);
        dos.flush();

        while (true) {
            msg = scanner.nextLine();
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                // TODO: handle exception
                System.out.println("Lỗi gửi tin nhắn");
            }
            
        }

    }
}
