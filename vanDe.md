# 1. 
    String reply = dis.readUTF();
    System.out.println(reply);

- Chỉ nhận tin nhắn 1 lần 

* Solution:
- Thêm vòng lặp

while (true) {
    String message = dis.readUTF();
    System.out.println("Client: " + message);
}
- Tương tự cho accept

# 1. 1 Thread (Client)
while (true) {
    String message = dis.readUTF();
    System.out.println(message);
}

- readUTF() sẽ đứng chờ đến khi có dữ liệu.
- Trong lúc đó, Server không thể làm việc khác.

Ví dụ: 
Client gửi:
Hello

↓

Server đọc

↓

Server đứng chờ

↓

Client gửi tiếp...

- Nếu Server còn phải gửi dữ liệu hoặc phục vụ client khác thì sẽ bị ảnh hưởng.

* Solution:
- Hai luồng chạy đồng thời.

Client

Main Thread
↓

Gửi dữ liệu

-------------------

Receive Thread
↓

Nhận dữ liệu

Ví dụ:
Lớp mới chỉ đọc
public class DemoThread extends Thread {

    @Override
    public void run() {

        while (true) {
            System.out.println("Đang chạy...");
        }

    }

}
Lớp chính chỉ gửi:
Scanner scanner = new Scanner(System.in);

while (true) {

    String message = scanner.nextLine();

    dos.writeUTF(message);

    dos.flush();

}

# 3. 1 Thread (Server)
while (true) {
    Socket socket = serverSocket.accept();

    DataInputStream dis = new DataInputStream(socket.getInputStream());

    while (true) {
        String msg = dis.readUTF();
        System.out.println(msg);
    }
}

- Điều gì xảy ra?
Server

accept()

↓

Client A kết nối

↓

Đọc tin nhắn của A mãi mãi

- Lúc này nếu Client B kết nối thì sao?
+ Server không accept() nữa
+ Vì Server đang mắc kẹt trong: 
while (true) {
    String msg = dis.readUTF();
}

* Solution:
                Server

          accept()

             │
   ┌─────────┴─────────┐
   │                   │
Client A           Client B

Thread A           Thread B
# 4. Broadcast

