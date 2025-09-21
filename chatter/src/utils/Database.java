package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.Message;

public class Database {
    private final HashMap<String, String> users = new HashMap<>();    // username -> password
    private final HashMap<String, byte[]> avatars = new HashMap<>();  // username -> avatar (byte[])
    private final List<Message> messages = new ArrayList<>();         // lưu tin nhắn

    // --- Đăng ký ---
    public synchronized boolean registerUser(String username, String password) {
        if (username == null || password == null) return false;
        if (users.containsKey(username)) return false;
        users.put(username, password);
        return true;
    }

    // --- Đăng nhập ---
    public synchronized boolean validateLogin(String username, String password) {
        if (username == null || password == null) return false;
        return users.containsKey(username) && users.get(username).equals(password);
    }

    // --- Lưu tin nhắn ---
    public synchronized void saveMessage(Message msg) {
        if (msg != null) {
            messages.add(msg);
        }
    }

    // --- Lấy toàn bộ tin nhắn của 1 user ---
    public synchronized List<Message> getMessages(String username) {
        List<Message> userMessages = new ArrayList<>();
        if (username == null) return userMessages;

        for (Message msg : messages) {
            if (msg == null) continue;
            if ((msg.getTo() != null && msg.getTo().equals(username))
                || (msg.getFrom() != null && msg.getFrom().equals(username))) {
                userMessages.add(msg);
            }
        }
        return userMessages;
    }

    // --- Cập nhật avatar ---
    public synchronized void updateAvatar(String username, byte[] data) {
        if (username != null && data != null) {
            avatars.put(username, data);
        }
    }

    // --- Lấy avatar ---
    public synchronized byte[] getAvatar(String username) {
        if (username == null) return null;
        return avatars.get(username);
    }

    // --- Tìm kiếm bạn bè theo username ---
    public synchronized List<String> searchUsers(String keyword) {
        List<String> results = new ArrayList<>();
        if (keyword == null) return results;

        for (String user : users.keySet()) {
            if (user.toLowerCase().contains(keyword.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }
}
