package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import model.Message;
import utils.Database;

public class Server {
    private static final int PORT = 9090; // Cổng 9090
    private static final ConcurrentHashMap<String, ObjectOutputStream> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 Máy chủ đã khởi động trên cổng " + PORT);
            Database db = new Database();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, db)).start();
            }
        } catch (BindException e) {
            System.err.println("❌ Cổng " + PORT + " đã được sử dụng.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String username;
        private final Database db;

        public ClientHandler(Socket socket, Database db) {
            this.socket = socket;
            this.db = db;
            try {
                // Quan trọng: luôn tạo ObjectOutputStream trước
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                closeResources();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (!(obj instanceof Message msg)) continue;
                    if (msg.getType() == null) continue;

                    switch (msg.getType()) {
                        case "register" -> handleRegister(msg);
                        case "login" -> handleLogin(msg);
                        case "chat", "image", "file" -> handleMessage(msg);
                        case "avatar" -> handleAvatarChange(msg);
                        case "search" -> handleSearch(msg);
                        case "logout" -> {
                            handleLogout();
                            return; // thoát vòng lặp
                        }
                        default -> System.out.println("⚠ Yêu cầu không hợp lệ: " + msg.getType());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                try {
                    handleLogout();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // --- các handler ---
        private void handleRegister(Message msg) throws IOException {
            String username = msg.getFrom();
            String password = msg.getContent();
            boolean success = db.registerUser(username, password);
            Message response = new Message(
                    "register",
                    success ? "success" : "error",
                    null,
                    success ? "Đăng ký thành công" : "Tên người dùng đã tồn tại"
            );
            sendMessage(out, response);
            if (success) broadcastOnlineUsers();
        }

        private void handleLogin(Message msg) throws IOException {
            String username = msg.getFrom();
            String password = msg.getContent();
            boolean success = db.validateLogin(username, password);

            if (success && !clients.containsKey(username)) {
                this.username = username;
                clients.put(username, out);
                sendMessage(out, new Message("login", "success", null, "Đăng nhập thành công"));
                sendChatHistory();
                broadcastOnlineUsers();
            } else {
                sendMessage(out, new Message("login", "error", null,
                        "Thông tin đăng nhập không hợp lệ hoặc đã đăng nhập"));
            }
        }

        private void handleMessage(Message msg) throws IOException {
            db.saveMessage(msg);
            if (msg.getTo() != null && clients.containsKey(msg.getTo())) {
                sendMessage(clients.get(msg.getTo()), msg);
            }
        }

        private void handleAvatarChange(Message msg) throws IOException {
            db.updateAvatar(msg.getFrom(), msg.getData());
            broadcastAvatar(msg.getFrom(), msg.getData());
        }

        private void handleSearch(Message msg) throws IOException {
            String keyword = msg.getContent();
            var users = db.searchUsers(keyword); // bạn cần cài đặt trong Database
            String result = String.join(",", users);
            sendMessage(out, new Message("searchResult", "server", msg.getFrom(), result));
        }

        private void handleLogout() throws IOException {
            if (username != null) {
                clients.remove(username);
                System.out.println("👋 Người dùng " + username + " đã đăng xuất.");
                broadcastOnlineUsers();
            }
            closeResources();
        }

        private void sendChatHistory() throws IOException {
            var history = db.getMessages(username);
            for (Message msg : history) {
                sendMessage(out, msg);
            }
            sendMessage(out, new Message("end_history", null, null, null));
        }

        private void broadcastAvatar(String from, byte[] data) throws IOException {
            for (var clientOut : clients.values()) {
                sendMessage(clientOut, new Message("avatar", from, null, null, data));
            }
        }

        private void broadcastOnlineUsers() throws IOException {
            String onlineUsers = String.join(",", clients.keySet());
            for (var clientOut : clients.values()) {
                sendMessage(clientOut, new Message("online_list", null, null, onlineUsers));
            }
        }

        // --- helper methods ---
        private void sendMessage(ObjectOutputStream oos, Message msg) throws IOException {
            synchronized (oos) { // tránh conflict ghi nhiều thread
                oos.writeObject(msg);
                oos.flush();
            }
        }

        private void closeResources() {
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        }
    }
}
