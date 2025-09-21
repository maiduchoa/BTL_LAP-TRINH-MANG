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
        <img src="https://github.com/user-attachments/assets/ee72b1c4-04c7-4e4b-8d7a-8cf16932804a"width="170" />
        <img src="https://github.com/user-attachments/assets/1459f5bf-7fc9-4462-996d-eb1ef7633a97"width="180" />
        <img src="https://github.com/user-attachments/assets/f081d02c-b644-4e87-a40c-fcb8383c2985"width="200" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)


📌 Giới thiệu

Ứng dụng Chat Client-Server sử dụng TCP được xây dựng nhằm mục đích mô phỏng một hệ thống nhắn tin cơ bản giữa nhiều người dùng. Hệ thống bao gồm một Server trung tâm chịu trách nhiệm quản lý kết nối và phân phối tin nhắn, cùng các Client có thể kết nối để trao đổi dữ liệu theo thời gian thực.

Ứng dụng được phát triển bằng ngôn ngữ Java, sử dụng Socket TCP để đảm bảo truyền tải tin nhắn ổn định và tin cậy. Với kiến trúc đa luồng, server có thể phục vụ đồng thời nhiều client, cho phép người dùng trò chuyện trong cùng một phòng chat.

Ngoài chức năng chat văn bản cơ bản, ứng dụng có thể mở rộng để hỗ trợ:

Gửi ảnh và file đính kèm.

Đăng nhập/đăng ký tài khoản người dùng.

Hiển thị danh sách người dùng trực tuyến.

Lưu trữ lịch sử tin nhắn.

Ứng dụng phù hợp cho việc học tập và nghiên cứu về lập trình mạng, socket programming, và xây dựng ứng dụng chat thời gian thực.
⚙️ Chức năng của Server

Server trong ứng dụng Chat TCP có các chức năng chính sau:

Quản lý kết nối Client

Tạo ServerSocket lắng nghe tại host:port.

Chấp nhận nhiều kết nối đồng thời nhờ cơ chế đa luồng (ClientHandler).

Theo dõi và quản lý danh sách client đang online.

Xác thực tài khoản

Cho phép Đăng ký (REGISTER): nếu tài khoản chưa tồn tại thì lưu username và password.

Cho phép Đăng nhập (LOGIN): kiểm tra thông tin tài khoản và xác thực người dùng.

Thông báo kết quả đăng nhập/đăng ký về cho client (REGISTER_SUCCESS, REGISTER_FAIL, LOGIN_SUCCESS, LOGIN_FAIL).

Gửi & Nhận tin nhắn

Nhận tin nhắn văn bản từ client (MSG:).

Hiển thị tin nhắn trong log server.

Broadcast tin nhắn đến tất cả client đang online.

Gửi & Nhận hình ảnh

Hỗ trợ nhận dữ liệu ảnh dưới dạng Base64 (IMG:).

Thông báo khi có người gửi ảnh.

Phát lại (broadcast) ảnh đến toàn bộ client.

Quản lý người dùng trực tuyến

Cập nhật danh sách nickname của client khi đăng nhập/tham gia.

Hiển thị số lượng người dùng online (👥 Online).

Thông báo khi có client tham gia hoặc rời khỏi phòng.

Quản lý giao diện server

Hiển thị log sự kiện với màu sắc khác nhau (thông báo, tin nhắn, lỗi, kết nối).

Có khung hiển thị danh sách user online.

Cung cấp ô nhập để server có thể gửi tin nhắn broadcast đến tất cả client.

Các nút chức năng:

🧹 Clear History: xóa log tin nhắn.

🗑 Clear All: xóa toàn bộ log, danh sách user và reset số online.

Xử lý sự cố & ngắt kết nối

Tự động loại bỏ client khỏi danh sách khi mất kết nối hoặc thoát.

Gửi thông báo rời phòng đến các client khác.

Báo lỗi trên log khi server gặp sự cố.
📌 Chức năng của Client

Đăng nhập / Đăng ký:

Người dùng có thể đăng nhập bằng tài khoản đã tạo.

Cho phép đăng ký tài khoản mới nếu chưa tồn tại.

Xử lý phản hồi từ server (LOGIN_SUCCESS, LOGIN_FAIL, REGISTER_SUCCESS, REGISTER_FAIL).

Giao diện chat (UI giống Zalo):

Hiển thị tin nhắn theo dạng bubble (bên phải cho chính mình, bên trái cho người khác).

Hỗ trợ emoji với menu chọn nhanh.

Hỗ trợ gửi ảnh:

Ảnh được gửi đi dưới dạng Base64.

Ảnh trong cửa sổ chat được thu nhỏ, có thể click vào để phóng to trong cửa sổ riêng.

Gửi & nhận tin nhắn:

Gửi tin nhắn văn bản (MSG:user:content) tới server.

Nhận và hiển thị tin nhắn từ người khác theo thời gian thực.

Gửi & nhận ảnh:

Gửi ảnh từ máy người dùng lên server (IMG:user:base64).

Nhận ảnh từ người khác và hiển thị trong cửa sổ chat.

Tích hợp emoji và file ảnh:

Nút 😊 để chọn emoji.

Nút 🖼 để chọn và gửi ảnh từ thư mục máy tính.

Giao diện người dùng trực quan:

Sử dụng Swing + Nimbus LookAndFeel.

Tin nhắn được căn lề rõ ràng (người gửi bên trái, mình bên phải).

Màu sắc khác nhau cho tin nhắn của mình và tin nhắn của người khác.

Hướng dẫn cài đặt & sử dụng
🛠️ Công nghệ sử dụng

Ứng dụng Chat Client-Server sử dụng các công nghệ và thư viện sau:

Ngôn ngữ lập trình:

Java SE – ngôn ngữ chính để xây dựng server và client.

Lập trình mạng (Networking):

java.net.ServerSocket: tạo server lắng nghe kết nối từ client.

java.net.Socket: kết nối client với server qua TCP.

Xử lý dữ liệu I/O:

BufferedReader / InputStreamReader: đọc dữ liệu từ client.

PrintWriter: gửi dữ liệu văn bản đến client.

Base64 Encoding/Decoding: truyền tải dữ liệu ảnh dưới dạng chuỗi.

Đa luồng (Multithreading):

Mỗi kết nối client được xử lý bởi một Thread riêng (ClientHandler).

Sử dụng Collections.synchronizedSet để quản lý danh sách client an toàn khi nhiều luồng truy cập.

Giao diện người dùng (GUI):

Swing (javax.swing, java.awt): xây dựng giao diện Server (log, danh sách user, số lượng online, input gửi tin).

JTextPane + StyledDocument: hiển thị log nhiều màu sắc.

JList + DefaultListModel: hiển thị danh sách người dùng trực tuyến.

Quản lý tài khoản:

HashMap (synchronizedMap): lưu trữ thông tin tài khoản (username → password).

Tiện ích khác:

Color API (java.awt.Color): hiển thị log với nhiều màu sắc.

Event Listener (ActionListener): xử lý sự kiện nút bấm (gửi tin, clear log…)
📸 Hình ảnh chức năng
1. Giao diện Server

Hiển thị log với màu sắc khác nhau (sự kiện, tin nhắn, lỗi).

Danh sách người dùng trực tuyến ở khung bên phải.

Có các nút điều khiển: Clear History, Clear All, ô nhập để gửi tin nhắn từ server.

2. Giao diện Client

Mỗi client đăng nhập bằng tài khoản riêng.

Hiển thị tin nhắn văn bản và ảnh từ các người dùng khác.

Hiển thị nickname của từng người gửi.

3. Demo hoạt động (Server và 3 Client cùng kết nối)
 Hình 1: Ảnh giao diện chat giữa Client-Server
   <img width="1919" height="1025" alt="image" src="https://github.com/user-attachments/assets/be65496a-afd2-40ed-8b77-a457213c28f8" />

 Hình 2: Client chat với Server
   <img width="1916" height="1024" alt="image" src="https://github.com/user-attachments/assets/87536c9e-fe49-40a8-9055-d8e66ae46993" />

 Hình 3: Hai Client chat với nhau.
   <img width="952" height="918" alt="image" src="https://github.com/user-attachments/assets/7c2781e9-8a4f-4d16-a660-25e7dcf26ef7" />

 Hình 4: Đăng ký thành công
   <img width="377" height="201" alt="image" src="https://github.com/user-attachments/assets/81ae4260-6bee-4e4d-bd45-b13e876404aa" />

 Hình 5: Đăng nhập thành công
   <img width="376" height="203" alt="image" src="https://github.com/user-attachments/assets/4fd84a7c-262e-4212-9c8b-e72d6043c3b1" />

Hình 6: Ảnh Server xóa dữ liệu
   <img width="958" height="1023" alt="image" src="https://github.com/user-attachments/assets/47a0b68f-17bc-4204-bc4a-bcd260b6aa0c" />
📥 Hướng dẫn cài đặt và sử dụng
1. Yêu cầu môi trường

Java JDK 8+ (khuyến nghị JDK 11 trở lên).

Máy tính chạy Windows / Linux / macOS.

2. Cài đặt

Clone hoặc tải project về máy:

git clone https://github.com/your-username/tcp-chat-app.git
cd tcp-chat-app


Biên dịch code Java:

javac Chat/Server.java Chat/Client.java


Đảm bảo ảnh minh họa được đặt trong thư mục images/ (nếu muốn hiển thị trong README).

3. Chạy chương trình
🔹 Chạy Server
java Chat.Server


Nhập host (mặc định: 127.0.0.1).

Nhập port (mặc định: 5000).

Server sẽ bắt đầu lắng nghe kết nối từ client.

🔹 Chạy Client
java Chat.Client


Nhập địa chỉ IP và port của server.

Đăng ký tài khoản mới hoặc đăng nhập bằng tài khoản đã có.

Sau khi đăng nhập thành công, bạn có thể chat với những người dùng khác.

🖥 Code minh họa
#.Server.java

package Chat;

import javax.swing.*;
import javax.swing.text.StyleConstants;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private JFrame frame;
    private JTextPane logArea;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel onlineCount;
    private JTextField inputField;

    private ServerSocket serverSocket;
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    // 🔑 Lưu tài khoản (username -> password)
    private static Map<String, String> accounts = Collections.synchronizedMap(new HashMap<>());

    private String host;
    private int port;

    public Server(String host, int port) throws IOException {
        this.setHost(host);
        this.setPort(port);

        // ============= GUI =============
        frame = new JFrame("🌈 TCP Chat Server");
        frame.setSize(750, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Log sự kiện (dùng JTextPane để đổi màu chữ)
        logArea = new JTextPane();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        // Danh sách user
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(new Color(240, 248, 255));
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(180, 0));
        userList.setBorder(BorderFactory.createTitledBorder("👥 Người dùng"));

        // Label số online
        onlineCount = new JLabel("👥 Online: 0");
        onlineCount.setFont(new Font("Arial", Font.BOLD, 14));

        // Panel nhập tin nhắn + nút gửi
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendBtn = new JButton("📢 Gửi");
        sendBtn.setBackground(new Color(255, 182, 193));
        sendBtn.setFont(new Font("Arial", Font.BOLD, 13));
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        // Panel control Clear
        JPanel controlPanel = new JPanel();
        JButton clearHistoryBtn = new JButton("🧹 Clear History");
        JButton clearAllBtn = new JButton("🗑 Clear All");
        clearHistoryBtn.setBackground(new Color(173, 216, 230));
        clearAllBtn.setBackground(new Color(255, 160, 122));
        controlPanel.add(clearHistoryBtn);
        controlPanel.add(clearAllBtn);
        bottomPanel.add(controlPanel, BorderLayout.SOUTH);

        // Thêm vào frame
        frame.add(logScroll, BorderLayout.CENTER);
        frame.add(userScroll, BorderLayout.EAST);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(onlineCount, BorderLayout.NORTH);
        southPanel.add(bottomPanel, BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // ============= EVENT =============
        sendBtn.addActionListener(e -> sendServerMessage());
        inputField.addActionListener(e -> sendServerMessage());

        clearHistoryBtn.addActionListener(e -> logArea.setText(""));
        clearAllBtn.addActionListener(e -> {
            logArea.setText("");
            userListModel.clear();
            onlineCount.setText("👥 Online: 0");
        });

        // ============= SERVER SOCKET =============
        InetAddress bindAddr = InetAddress.getByName(host);
        serverSocket = new ServerSocket(port, 50, bindAddr);

        log("🚀 Server đang chạy tại " + host + ":" + port, Color.BLUE);

        // Thread lắng nghe client
        new Thread(this::acceptClients).start();
    }

    private void acceptClients() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            log("❌ Lỗi server: " + e.getMessage(), Color.RED);
        }
    }

    private void log(String msg, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                var doc = logArea.getStyledDocument();
                var style = logArea.addStyle("Style", null);
                StyleConstants.setForeground(style, color);
                doc.insertString(doc.getLength(), msg + "\n", style);
                logArea.setCaretPosition(doc.getLength());
            } catch (Exception ignored) {}
        });
    }

    private void updateUserList() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            synchronized (clients) {
                for (ClientHandler c : clients) {
                    userListModel.addElement(c.getNickname());
                }
            }
            onlineCount.setText("👥 Online: " + clients.size());
        });
    }

    private void sendServerMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            log("📢 Server: " + msg, new Color(128, 0, 128));
            broadcast("📢 Server: " + msg);
            inputField.setText("");
        }
    }

    // 🔄 Broadcast chung cho tất cả client
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.out.println(message);
            }
        }
    }

    // ================= ClientHandler =================
    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname = "Người dùng";
        private boolean loggedIn = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public String getNickname() {
            return nickname;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Xử lý LOGIN / REGISTER
                while (!loggedIn) {
                    String msg = in.readLine();
                    if (msg == null) return;

                    if (msg.startsWith("REGISTER:")) {
                        String[] parts = msg.split(":");
                        if (parts.length == 3) {
                            String user = parts[1];
                            String pass = parts[2];
                            if (!accounts.containsKey(user)) {
                                accounts.put(user, pass);
                                out.println("REGISTER_SUCCESS");
                                log("🟢 Tài khoản mới: " + user, new Color(0, 128, 0));
                            } else {
                                out.println("REGISTER_FAIL");
                            }
                        }
                    } else if (msg.startsWith("LOGIN:")) {
                        String[] parts = msg.split(":");
                        if (parts.length == 3) {
                            String user = parts[1];
                            String pass = parts[2];
                            if (accounts.containsKey(user) && accounts.get(user).equals(pass)) {
                                nickname = user;
                                loggedIn = true;
                                out.println("LOGIN_SUCCESS");
                                log("🔑 " + nickname + " đã đăng nhập.", Color.BLUE);
                                synchronized (clients) {
                                    clients.add(this);
                                }
                                updateUserList();
                                broadcast("🔔 " + nickname + " đã tham gia.");
                            } else {
                                out.println("LOGIN_FAIL");
                            }
                        }
                    }
                }

                // Vòng lặp chat
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equalsIgnoreCase("exit")) break;

                    if (msg.startsWith("MSG:")) {
                        String text = msg.substring(4);
                        log("💬 " + nickname + ": " + text, Color.BLACK);
                        broadcast("👤 " + nickname + ": " + text);
                    } else if (msg.startsWith("IMG:")) {
                        String base64 = msg.substring(4);
                        log("🖼 " + nickname + " đã gửi 1 ảnh.", Color.MAGENTA);
                        broadcast("IMG:" + base64);
                    }
                }
            } catch (IOException e) {
                log("⚠ Mất kết nối với " + nickname, Color.RED);
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
                if (loggedIn) {
                    clients.remove(this);
                    log("❌ " + nickname + " đã thoát.", Color.RED);
                    updateUserList();
                    broadcast("🚪 " + nickname + " đã rời khỏi phòng.");
                }
            }
        }
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String host = JOptionPane.showInputDialog("Nhập host :", "127.0.0.1");
            String portStr = JOptionPane.showInputDialog("Nhập cổng:", "5000");
            int port = 5000;
            try {
                port = Integer.parseInt(portStr);
            } catch (Exception ignored) {}

            boolean started = false;
            while (!started) {
                try {
                    new Server(host, port);
                    started = true;
                } catch (IOException e) {
                    if (e.getMessage().contains("bind")) {
                        JOptionPane.showMessageDialog(null,
                                "⚠ Port " + port + " đã bị chiếm, thử port " + (port + 1),
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                        port++;
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "❌ Không thể khởi động server: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
            }
        });
    }

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
#.Client.java  

package Chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Base64;
import javax.imageio.ImageIO;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    private JFrame chatFrame;
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField inputField;

    public Client(String serverAddress, int port) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            showLoginForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Không kết nối được server!");
            System.exit(1);
        }
    }

    /** Giao diện đăng nhập / đăng ký */
    private void showLoginForm() {
        JFrame loginFrame = new JFrame("🔑 Đăng nhập / Đăng ký");
        loginFrame.setSize(400, 220);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Đăng nhập");
        JButton registerButton = new JButton("Đăng ký");

        panel.add(new JLabel("👤 Tên tài khoản:"));
        panel.add(usernameField);
        panel.add(new JLabel("🔒 Mật khẩu:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginFrame.add(panel);
        loginFrame.setVisible(true);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();
            if (!user.isEmpty() && !pass.isEmpty()) {
                out.println("LOGIN:" + user + ":" + pass);
                new Thread(() -> {
                    try {
                        String response = in.readLine();
                        if ("LOGIN_SUCCESS".equals(response)) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(loginFrame, "✅ Đăng nhập thành công!");
                                this.username = user;
                                loginFrame.dispose();
                                showChatWindow();
                                startMessageReader();
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(loginFrame, "❌ Sai tài khoản hoặc mật khẩu!"));
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        registerButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();
            if (!user.isEmpty() && !pass.isEmpty()) {
                out.println("REGISTER:" + user + ":" + pass);
                new Thread(() -> {
                    try {
                        String response = in.readLine();
                        if ("REGISTER_SUCCESS".equals(response)) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(loginFrame, "✅ Đăng ký thành công, hãy đăng nhập!"));
                        } else {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(loginFrame, "❌ Tài khoản đã tồn tại!"));
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }

    /** Giao diện chat kiểu Zalo */
    private void showChatWindow() {
        chatFrame = new JFrame("💬 Chat - " + username);
        chatFrame.setSize(650, 500);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setLocationRelativeTo(null);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        inputField = new JTextField();
        JButton sendButton = new JButton("📩");
        JButton emojiButton = new JButton("😊");
        JButton imageButton = new JButton("🖼");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(emojiButton);
        buttonPanel.add(imageButton);
        buttonPanel.add(sendButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        chatFrame.add(chatScrollPane, BorderLayout.CENTER);
        chatFrame.add(bottomPanel, BorderLayout.SOUTH);
        chatFrame.setVisible(true);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        emojiButton.addActionListener(e -> {
            String[] emojis = {"😊", "😂", "❤️", "👍", "😢", "🔥"};
            String emoji = (String) JOptionPane.showInputDialog(
                    chatFrame,
                    "Chọn emoji:",
                    "Emoji",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    emojis,
                    emojis[0]
            );
            if (emoji != null) inputField.setText(inputField.getText() + emoji);
        });

        imageButton.addActionListener(e -> sendImage());
    }

    /** Gửi text */
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            out.println("MSG:" + username + ":" + msg);
            addMessageBubble(username, msg, true);
            inputField.setText("");
        }
    }

    /** Gửi ảnh */
    private void sendImage() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(chatFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                out.println("IMG:" + username + ":" + base64);
                addImageBubble(username, img, true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(chatFrame, "❌ Không gửi được ảnh!");
            }
        }
    }

    /** Luồng đọc tin nhắn */
    private void startMessageReader() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("IMG:")) {
                        showImageMessage(msg);
                    } else if (msg.startsWith("MSG:")) {
                        String[] parts = msg.split(":", 3);
                        if (parts.length >= 3) addMessageBubble(parts[1], parts[2], !parts[1].equals(username));
                    }
                }
            } catch (IOException e) {
                addMessageBubble("System", "❌ Mất kết nối server.", false);
            }
        }).start();
    }

    private void showImageMessage(String msg) {
        try {
            String[] parts = msg.split(":", 3);
            if (parts.length >= 3) {
                String sender = parts[1];
                byte[] bytes = Base64.getDecoder().decode(parts[2]);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                addImageBubble(sender, img, !sender.equals(username));
            }
        } catch (Exception e) {
            addMessageBubble("System", "❌ Lỗi hiển thị ảnh.", false);
        }
    }

    /** Thêm bubble text */
    private void addMessageBubble(String sender, String message, boolean isSelf) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(isSelf ? new Color(0, 162, 255) : new Color(220, 220, 220));
        bubble.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

        JLabel lbl = new JLabel("<html>" + sender + ": " + message + "</html>");
        lbl.setForeground(isSelf ? Color.WHITE : Color.BLACK);
        bubble.add(lbl);

        JPanel wrapper = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        chatPanel.add(wrapper);
        chatPanel.revalidate();
        chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
    }

    /** Thêm bubble ảnh */
    private void addImageBubble(String sender, BufferedImage img, boolean isSelf) {
        Image scaled = img.getScaledInstance(150, -1, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);
        JLabel lbl = new JLabel(icon);
        lbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFrame f = new JFrame(sender + "'s image");
                f.add(new JLabel(new ImageIcon(img)));
                f.pack();
                f.setVisible(true);
            }
        });

        JPanel bubble = new JPanel(new BorderLayout());
        bubble.add(lbl, BorderLayout.CENTER);
        bubble.setBackground(isSelf ? new Color(0, 162, 255) : new Color(220, 220, 220));
        bubble.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel wrapper = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        chatPanel.add(wrapper);
        chatPanel.revalidate();
        chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client("localhost", 5000));
    }
    }
📞 Thông tin liên hệ

👤 Tác giả: Nguyễn Hoàng Anh

📧 Email: juliangray1611@gmail.com

📱 Số điện thoại : 0399776626
 
