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

* giải pháp:
- synchronized tạo ra khóa (lock) trên đối tượng ChatServer.clients:
synchronized (ChatServer.clients) {
    // Chỉ có 1 luồng được vào đây tại 1 thời điểm
    ChatServer.clients.remove(this);
}

Nguyên lý hoạt động:

Luồng A vào khối synchronized → giành lock

Luồng B muốn vào → phải chờ đến khi A giải phóng lock

Luồng A thực hiện xong → giải phóng lock

Luồng B được vào → tiếp tục thực hiện



===========================================
🖥️ Server/ — Phía máy chủ
- ChatServer.java	Điểm khởi động server. Mở cổng lắng nghe (port), chấp nhận kết nối từ client, tạo một ClientHandler mới cho mỗi client kết nối vào.

- ClientHandler.java	Xử lý từng client riêng biệt. Chạy trên một Thread riêng, đọc lệnh từ client (LOGIN, PUB_MSG, DELETE_ACCOUNT...), xử lý và phản hồi. Đây là file logic chính của server.

🎮 Controller/ — Tầng điều phối phía client
- ChatController.java	Cầu nối giữa GUI và socket. Quản lý kết nối TCP tới server, gửi các lệnh (sendTextMessage, deleteAccount, changePassword...), lắng nghe phản hồi từ server trong một thread nền và cập nhật lại giao diện (ChatFrame).

🗄️ DAO/ — Truy cập cơ sở dữ liệu (Data Access Object)
- UserDAO.java	Thao tác với bảng Users trong DB. Cung cấp các hàm: đăng nhập (kiemTraDangNhap), đăng ký (dangKy), đổi mật khẩu, đổi username, xóa tài khoản (xoaTaiKhoan), lấy danh sách user.

- MessageDAO.java	Thao tác với bảng Messages trong DB. Lưu tin nhắn, lấy lịch sử chat chung và chat riêng.

🗃️ Database/ — Kết nối cơ sở dữ liệu
- DBConnection.java	Tạo kết nối JDBC tới SQL Server. Chứa thông tin host, port, tên DB, username/password. Trả về đối tượng Connection dùng chung cho các DAO.

🖼️ GUI/ — Giao diện người dùng (Swing)
- LoginFrame.java	Màn hình đăng nhập. Nhập username/password, gọi ChatController.login(), chuyển sang ChatFrame nếu thành công. Có nút mở màn hình đăng ký.

- RegisterFrame.java	Màn hình đăng ký tài khoản. Nhập thông tin, gọi ChatController.register().

- ChatFrame.java	Màn hình chat chính. Hiển thị tin nhắn, danh sách người dùng, cho phép gửi tin nhắn công khai/riêng tư. Có nút "Hồ sơ" mở ProfileFrame.

- ProfileFrame.java	Màn hình quản lý tài khoản. Gồm 2 tab: đổi mật khẩu và đổi username; có nút "Xóa tài khoản" để xóa và thoát về màn hình login.

===========================
gửi file lớn:
- Giao thức mới
Client → Server:
  FILE_START|<fileName>|<fileSize>|<receiverOrPUBLIC>
  [raw bytes, chunk by chunk]
  FILE_END|<fileName>

Server → Clients:
  FILE_META|<sender>|<fileName>|<fileSize>
  [raw bytes]
  FILE_DONE|<fileName>
