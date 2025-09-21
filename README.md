<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   ỨNG DỤNG CHAT CLIENT-SERVER SỬ DỤNG TCP
</h2>
<div align="center">
    <p align="center">
        <!-- Thay link ảnh ở đây -->
        <img src="https://github.com/user-attachments/assets/ee72b1c4-04c7-4e4b-8d7a-8cf16932804a" width="170" />
        <img src="https://github.com/user-attachments/assets/1459f5bf-7fc9-4462-996d-eb1ef7633a97" width="180" />
        <img src="https://github.com/user-attachments/assets/f081d02c-b644-4e87-a40c-fcb8383c2985" width="200" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

---

## 📖 1. Giới thiệu
Đây là đồ án môn học Lập trình mạng, ứng dụng Chat Client-Server được phát triển dựa trên mô hình TCP Socket. Hệ thống cho phép nhiều người dùng có thể đăng ký tài khoản, đăng nhập, gửi và nhận tin nhắn theo thời gian thực, chia sẻ tệp, và tuỳ chỉnh avatar.  
Sinh viên sau khi hoàn thành có khả năng hiểu và triển khai một hệ thống mạng ứng dụng thực tế dựa trên Java TCP Socket.

---

## 🔧 2. Ngôn ngữ lập trình sử dụng
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)

---

## 🚀 3. Các chức năng chính

### 🔑 Đăng ký & Đăng nhập
Người dùng có thể tạo tài khoản mới với **username, mật khẩu và avatar cá nhân**.  
Khi đăng ký, người dùng được phép **chọn avatar** hoặc hệ thống sẽ tự động gán **avatar mặc định**.  
Khi đăng nhập, hệ thống sẽ kiểm tra thông tin tài khoản trong **Database giả lập (file hoặc Map)**.  
Nếu hợp lệ, người dùng được chuyển vào giao diện Chat.

### 💬 Chat cá nhân & nhóm
Sau khi đăng nhập, người dùng có thể gửi tin nhắn đến server.  
Server chịu trách nhiệm **phát lại tin nhắn đến toàn bộ client khác** đang online.  
Tin nhắn hiển thị kèm **thời gian gửi** và **avatar** của người gửi.  

### 😀 Biểu tượng cảm xúc (Emoji)
Hệ thống hỗ trợ gửi emoji thay cho văn bản.  
Khi click vào nút emoji, bảng emoji hiển thị cho phép chọn nhanh.  
Emoji sẽ được hiển thị trong khung chat thay cho văn bản mã hóa.

### 📁 Gửi và nhận tệp
Người dùng có thể chọn một file từ máy tính và gửi qua server.  
Server sẽ trung chuyển file đó đến client nhận.  
File nhận được sẽ được lưu trong thư mục mặc định và hiển thị đường dẫn trong khung chat.

### 🖼️ Quản lý Avatar
Khi đăng ký, người dùng có thể chọn avatar.  
Nếu không chọn, hệ thống sẽ tự động gán **default_avatar.png**.  
Trong quá trình chat, avatar hiển thị bên cạnh tin nhắn giúp dễ phân biệt người gửi.

---

## 🖼️ 4. Minh hoạ giao diện

### 🔑 Giao diện đăng nhập
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/c504f085-b6b0-4456-81dc-8106a46f21ea" />

### 📝 Giao diện đăng ký
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/42b46770-6cdd-4433-a6cf-158b30a14b39" />


### 💬 Giao diện chat
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/99422739-9d80-44cf-b9e2-f13863f5322d" />

### 😀 Gửi Emoji
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/d7073e07-3632-4dfb-9c1b-f04227ad959a" />


### 📁 Gửi file
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/f55e757d-e016-44ce-a689-c6420f889763" />


---

## 📝 5. License
© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.
