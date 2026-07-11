# Lộ trình 
✅ ServerSocket
✅ Socket
✅ Gửi/nhận dữ liệu
✅ Thread
✅ ClientHandler
✅ Broadcast

⬇

1. Username
2. Thông báo Online/Offline
3. Xử lý ngắt kết nối
4. Giao diện Swing
5. Database
6. Chat riêng
7. Gửi file

# Từ ngữ:
readUTF(): Chờ nhận tin nhắn
scanner.nextLine(): chờ nhập tin nhắn
=> Nếu bỏ chung vòng lặp thì sẽ chặn nhau và bị treo. Do đó sẽ bỏ đọc tin nhắn sang lớp riêng
# accept() hoạt động như thế nào?
Server

ServerSocket(5000)

↓

accept()

↓

Đợi...

↓

Client kết nối

↓

Trả về Socket

# Luồng gửi tin nhắn 1 chiều
Client

writeUTF()

      │
      ▼

OutputStream

      │

Socket TCP

      │

InputStream

      ▼

readUTF()

Server

# Luồng gửi 2 chiều
Client

writeUTF("Hello")

        │

        ▼

Server

readUTF()

↓

writeUTF("Xin chào")

        │

        ▼

Client

readUTF()

# Ý tưởng của Thread
## Thay vì chỉ có một luồng chạy:
Main Thread

↓

Đọc dữ liệu

↓

Đọc dữ liệu

↓

Đọc dữ liệu

## Ta tạo thêm một luồng:
Main Thread
↓

Gửi dữ liệu

-------------------

Receive Thread
↓

Nhận dữ liệu

### Hai luồng chạy đồng thời.

# 
ChatApplication
│
├── server
│   ├── ChatServer.java
│   └── ClientHandler.java   ← Mỗi client là một Thread
│
├── client
│   ├── ChatClient.java
│   ├── ReceiveThread.java   ← Luôn lắng nghe tin nhắn
│   └── SendThread.java       ← (Có thể thêm sau)

# Thread đa luồng (cách hoạt động)
Server

↓

accept()

↓

Thread An

↓

accept()

↓

Thread Bình

↓

accept()

↓

Thread Cường

# Kiến trúc đa luồng
ChatServer

↓

accept()

↓

ClientHandler

↓

Đọc dữ liệu

↓

Xử lý

↓

Gửi dữ liệu

# Broadcast
Client A  <---> Handler A
Client B  <---> Handler B
Client C  <---> Handler C

- Khi A gửi:
+ Server sẽ làm:
Handler A nhận

↓

Gửi cho Handler B

↓

Gửi cho Handler C

- Tại sao phải lưu?
Nếu không lưu, khi Client A gửi tin nhắn, Server sẽ không biết còn những Client nào đang online.

## Additon
while (true) {

    String message = dis.readUTF();

    for (ClientHandler client : ChatServer.clients) {

        client.sendMessage(message);

    }

}

- Ý nghĩa:
A gửi

↓

Server

↓

for()

↓

B

↓

C

↓

D

=> Mỗi ClientHandler trong danh sách sẽ được gọi sendMessage().

### 
Client A (đang gửi tin nhắn "Hello")
    ↓
Server nhận được tin nhắn từ Client A
    ↓
Vòng lặp duyệt danh sách:
    - Client A? → BỎ QUA (vì client != this là false)
    - Client B? → GỬI TIN "Hello" đến Client B ✓
    - Client C? → GỬI TIN "Hello" đến Client C ✓
    - Client D? → GỬI TIN "Hello" đến Client D ✓

### 
Client A gửi: "Xin chào mọi người!"
         ↓
    [SERVER] ← Nhận tin nhắn
         ↓
    Server duyệt danh sách client:
    - Client A? → BỎ QUA
    - Client B? → GỬI "Xin chào mọi người!" ✓
    - Client C? → GỬI "Xin chào mọi người!" ✓
         ↓
    Client B nhận được: "Xin chào mọi người!" (từ Client A)
    Client C nhận được: "Xin chào mọi người!" (từ Client A)
    
    Server: Im lặng, không hiển thị gì cả 🤐


###
# Client
Khởi động

↓

Kết nối tới Server

↓

Tạo InputStream và OutputStream

↓

Thread 1:
Đọc bàn phím → Gửi Server

↓

Thread 2:
Nhận từ Server → Hiển thị

* Hay nói ngắn gọn:
      ✅ Kết nối Server
      ✅ Nhập tin nhắn
      ✅ Gửi tin nhắn
      ✅ Nhận tin nhắn từ Server
      ✅ Hiển thị tin nhắn
# Server
Khởi động

↓

Mở ServerSocket

↓

Chờ kết nối

↓

Có Client mới

↓

Tạo ClientHandler

↓

Lưu ClientHandler vào List

# Sau đó mỗi ClientHandler sẽ:
Nhận tin nhắn

↓

Xử lý

↓

Broadcast

↓

Gửi cho các Client cần nhận

* Hay nói ngắn gọn:
      ✅ Mở cổng và chấp nhận kết nối
      ✅ Quản lý danh sách client đang online
      ✅ Nhận tin nhắn từ client
      ✅ Quyết định gửi cho ai (broadcast, chat riêng...)
      ✅ Gửi tin nhắn đến client

# Luồng dữ liệu hiện tại:
Client A
   │
   │ writeUTF("Hello")
   ▼
Server
   │
   │ ClientHandler A đọc được
   ▼
Broadcast
   │
   ├────► Client B
   │
   └────► Client C

###
+--------------------+
|      Client A      |
|--------------------|
| - Gửi              |
| - Nhận             |
| - Hiển thị         |
+--------------------+
          |
          |
          v
+--------------------+
|       Server       |
|--------------------|
| - Accept client    |
| - Lưu danh sách    |
| - Broadcast        |
| - Quản lý kết nối  |
+--------------------+
        /      \
       /        \
      v          v
+----------+  +----------+
| Client B |  | Client C |
+----------+  +----------+

# Username
Client

↓

Kết nối

↓

Gửi Username

↓

Server lưu Username

↓

Bắt đầu chat
- Tức là chỉ gửi Username một lần duy nhất, sau đó mới gửi các tin nhắn.

# Synchorized
Luồng A (Client A đang ngắt kết nối):
    ChatServer.clients.remove(A);  // Đang xóa A

Luồng B (Client B đang ngắt kết nối):
    ChatServer.clients.remove(B);  // Đang xóa B

Luồng C (Client C đang gửi tin nhắn):
    for (ClientHandler client : ChatServer.clients) {  // Đang duyệt danh sách
        client.sendMessage(msg);
    }

* Có thể bị
- ConcurrentModificationException: Khi một luồng đang duyệt danh sách (for loop) mà luồng khác lại thay đổi danh sách (xóa phần tử)

- Dữ liệu không nhất quán: Hai luồng cùng sửa danh sách có thể gây lỗi logic

- NullPointerException: Có thể lấy được client đã bị xóa từ luồng khác