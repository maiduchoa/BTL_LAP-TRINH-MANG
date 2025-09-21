package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import model.Message;
import utils.Database;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9090;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private Database db = new Database();

    private JFrame frame;
    private JPanel chatPanel;
    private JTextField messageField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private String selectedUser;
    private JLabel avatarLabel;
    private JLabel chatTitle;
    private Map<String, JPanel> chatPanels = new HashMap<>();

    private volatile boolean running = false;
    private final BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Client().start();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi khởi tạo ứng dụng: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void start() {
        showLoginWindow();
    }

    /**
     * Kết nối tới server và bắt đầu thread lắng nghe
     */
    private synchronized void connectToServer() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) return;

        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        running = true;

        new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) continue;
                    Message msg = (Message) obj;
                    String type = msg.getType();
                    if (type == null) continue;

                    switch (type) {
                        case "chat":
                            SwingUtilities.invokeLater(() -> showMessageBubble(msg));
                            break;
                        case "image":
                        case "file":
                            // lưu file nhận được và hiển thị tương ứng
                            saveReceivedFile(msg);
                            SwingUtilities.invokeLater(() -> showMessageBubble(msg));
                            break;
                        case "avatar":
                            updateAvatar(msg.getFrom(), msg.getData());
                            saveFriendAvatar(msg.getFrom(), msg.getData());
                            if (userList != null) userList.repaint();
                            break;
                        case "online_list":
                            updateUserList(msg.getContent());
                            break;
                        case "login":
                        case "register":
                        case "logout":
                            // server trả về trực tiếp message type = "login"/"register"/"logout"
                            responseQueue.offer(msg);
                            break;
                        default:
                            // có thể server dùng "response" hoặc khác — chấp nhận linh hoạt:
                            responseQueue.offer(msg);
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                running = false;
                if (frame != null) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                            "Mất kết nối với máy chủ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
            }
        }, "Client-Listener").start();
    }

    // =================== LOGIN WINDOW ===================
    private void showLoginWindow() {
        JFrame loginFrame = new JFrame("Chatter - Đăng Nhập");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(460, 340);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Đăng Nhập", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(40, 60, 120));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        JLabel userLabel = new JLabel("Tên người dùng:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(userLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Đăng Nhập");
        JButton registerButton = new JButton("Đăng Ký");
        stylePrimaryButton(loginButton);
        styleSecondaryButton(registerButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);

        loadConfig(usernameField, passwordField);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Vui lòng nhập tên đăng nhập và mật khẩu", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                connectToServer();
                Message msg = new Message("login", user, null, pass);
                out.writeObject(msg);
                out.flush();

                // đợi server trả lời (login/register)
                Message response = responseQueue.take(); // sẽ nhận message có type "login" hoặc "register"
                String content = response.getContent() == null ? "" : response.getContent();
                if (content.toLowerCase().contains("thành công") || content.toLowerCase().contains("success") ||
                        content.equalsIgnoreCase("Đăng nhập thành công") || content.equalsIgnoreCase("Login successful")) {
                    username = user;
                    saveConfig(user, pass);
                    loginFrame.dispose();
                    showChatWindow();
                    loadLocalAvatar();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, content, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(loginFrame, "Lỗi kết nối: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        registerButton.addActionListener(e -> {
            loginFrame.dispose();
            showRegisterWindow();
        });

        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }

    // =================== REGISTER WINDOW ===================
    private void showRegisterWindow() {
        JFrame registerFrame = new JFrame("Chatter - Đăng Ký");
        registerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        registerFrame.setSize(520, 420);
        registerFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Đăng Ký", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(40, 60, 120));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        JLabel userLabel = new JLabel("Tên người dùng:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(userLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = 2;
        panel.add(usernameField, gbc);
        gbc.gridwidth = 1;

        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = 2;
        panel.add(passwordField, gbc);
        gbc.gridwidth = 1;

        // Avatar chooser for registration
        JLabel avatarPreview = new JLabel();
        avatarPreview.setPreferredSize(new Dimension(80, 80));
        avatarPreview.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        byte[] defaultAvatarBytes = loadDefaultAvatarBytes();
        if (defaultAvatarBytes != null) {
            ImageIcon ico = new ImageIcon(defaultAvatarBytes);
            Image img = ico.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            avatarPreview.setIcon(new ImageIcon(img));
        }

        JButton chooseAvatarBtn = new JButton("Chọn Avatar");
        styleSecondaryButton(chooseAvatarBtn);

        // store chosen avatar bytes in an array reference
        final byte[][] chosenAvatarHolder = new byte[1][];
        chooseAvatarBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(registerFrame) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    byte[] b = Files.readAllBytes(f.toPath());
                    chosenAvatarHolder[0] = b;
                    ImageIcon ico = new ImageIcon(b);
                    Image img = ico.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    avatarPreview.setIcon(new ImageIcon(img));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(registerFrame, "Không thể đọc file avatar: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Avatar:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(avatarPreview, gbc);
        gbc.gridx = 2; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(chooseAvatarBtn, gbc);

        JButton registerButton = new JButton("Đăng Ký");
        JButton backButton = new JButton("Quay lại");
        stylePrimaryButton(registerButton);
        styleSecondaryButton(backButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(backButton, gbc);

        registerButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(registerFrame, "Vui lòng nhập tên và mật khẩu", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                connectToServer();
                Message msg = new Message("register", user, null, pass);
                out.writeObject(msg);
                out.flush();

                Message response = responseQueue.take();
                String content = response.getContent() == null ? "" : response.getContent();
                JOptionPane.showMessageDialog(registerFrame, content, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                if (content.toLowerCase().contains("thành công") || content.toLowerCase().contains("success")) {
                    // Nếu user đã chọn avatar lúc đăng ký, gửi avatar lên server và lưu local
                    if (chosenAvatarHolder[0] != null) {
                        try {
                            Message avatarMsg = new Message("avatar", user, null, "avatar.png");
                            avatarMsg.setData(chosenAvatarHolder[0]);
                            out.writeObject(avatarMsg);
                            out.flush();
                            saveLocalAvatar(user, chosenAvatarHolder[0]);
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    } else {
                        // nếu không chọn avatar, copy default_avatar.png vào avatars/user.png để hiện avatar mặc định
                        try {
                            File dir = new File("avatars");
                            if (!dir.exists()) dir.mkdirs();
                            File defaultFile = new File("default_avatar.png");
                            if (defaultFile.exists()) {
                                Files.copy(defaultFile.toPath(),
                                        new File(dir, user + ".png").toPath(),
                                        StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (Exception io) {
                            io.printStackTrace();
                        }
                    }

                    registerFrame.dispose();
                    showLoginWindow();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(registerFrame, "Lỗi kết nối: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        backButton.addActionListener(e -> {
            registerFrame.dispose();
            showLoginWindow();
        });

        registerFrame.add(panel);
        registerFrame.setVisible(true);
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(80, 140, 240));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 36));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setBackground(new Color(230, 230, 230));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setPreferredSize(new Dimension(140, 36));
    }

    // =================== CHAT WINDOW ===================
    private void showChatWindow() {
        frame = new JFrame("Chatter - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 640);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // ===== Left: Avatar chính + danh sách bạn bè =====
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setOpaque(true);
                panel.setBorder(new EmptyBorder(4, 4, 4, 4));
                panel.setBackground(isSelected ? new Color(220, 235, 255) : Color.WHITE);

                String user = (String) value;
                JLabel label = new JLabel(user);
                label.setBorder(new EmptyBorder(5, 5, 5, 5));
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                byte[] avatarBytes = loadLocalAvatarBytes(user);
                ImageIcon icon;
                if (avatarBytes != null) icon = new ImageIcon(avatarBytes);
                else icon = new ImageIcon(new ImageIcon("default_avatar.png").getImage());

                Image img = icon.getImage().getScaledInstance(34, 34, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));

                panel.add(label, BorderLayout.CENTER);
                return panel;
            }
        });

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedUser = userList.getSelectedValue();
                chatTitle.setText(selectedUser != null ? selectedUser : "Chatter");
                chatPanel.removeAll();
                if (selectedUser != null && chatPanels.containsKey(selectedUser)) {
                    chatPanel.add(chatPanels.get(selectedUser));
                }
                updateChatAvatar(selectedUser);
                chatPanel.revalidate();
                chatPanel.repaint();
            }
        });

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(260, 0));

        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftTopPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JLabel myAvatarLabel = new JLabel();
        myAvatarLabel.setPreferredSize(new Dimension(44, 44));
        byte[] myAvatarBytes = loadLocalAvatarBytes(username);
        if (myAvatarBytes != null) {
            ImageIcon icon = new ImageIcon(myAvatarBytes);
            Image img = icon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
            myAvatarLabel.setIcon(new ImageIcon(img));
        } else {
            ImageIcon icon = new ImageIcon(new ImageIcon("default_avatar.png").getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH));
            myAvatarLabel.setIcon(icon);
        }
        JLabel myNameLabel = new JLabel(username);
        myNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftTopPanel.add(myAvatarLabel);
        leftTopPanel.add(myNameLabel);

        // Popup menu đổi avatar + đăng xuất
        JPopupMenu avatarMenu = new JPopupMenu();
        JMenuItem changeAvatarItem = new JMenuItem("Đổi Avatar");
        JMenuItem logoutItem = new JMenuItem("Đăng Xuất");
        avatarMenu.add(changeAvatarItem);
        avatarMenu.add(logoutItem);
        myAvatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                avatarMenu.show(myAvatarLabel, e.getX(), e.getY());
            }
        });
        changeAvatarItem.addActionListener(e -> changeAvatar());
        logoutItem.addActionListener(e -> logout());

        JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
        leftPanel.add(leftTopPanel, BorderLayout.NORTH);
        leftPanel.add(userScroll, BorderLayout.CENTER);

        // ===== Center: Chat =====
        JPanel chatContainer = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        chatTitle = new JLabel("Chatter");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(avatarLabel);
        topPanel.add(chatTitle);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane chatScroll = new JScrollPane(chatPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout(6, 6));
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("Gửi");
        JButton sendImageButton = new JButton("Ảnh");
        JButton sendFileButton = new JButton("File");
        JButton emojiButton = new JButton("😊");

        sendButton.addActionListener(e -> sendMessage());
        sendImageButton.addActionListener(e -> sendFile("image"));
        sendFileButton.addActionListener(e -> sendFile("file"));
        emojiButton.addActionListener(e -> showEmojiPanel());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonPanel.add(emojiButton);
        buttonPanel.add(sendImageButton);
        buttonPanel.add(sendFileButton);
        buttonPanel.add(sendButton);

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        chatContainer.add(topPanel, BorderLayout.NORTH);
        chatContainer.add(chatScroll, BorderLayout.CENTER);
        chatContainer.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(chatContainer, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void updateChatAvatar(String user) {
        if (user == null) {
            avatarLabel.setIcon(null);
            return;
        }
        byte[] avatarBytes = loadLocalAvatarBytes(user);
        if (avatarBytes != null) {
            ImageIcon icon = new ImageIcon(avatarBytes);
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(img));
        } else {
            ImageIcon icon = new ImageIcon(new ImageIcon("default_avatar.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            avatarLabel.setIcon(icon);
        }
    }

    private void logout() {
        running = false;
        try {
            if (out != null) {
                out.writeObject(new Message("logout", username, null, null));
                out.flush();
            }
        } catch (IOException ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}

        if (frame != null) frame.dispose();
        chatPanels.clear();
        selectedUser = null;
        username = null;
        showLoginWindow();
    }

    private void sendMessage() {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "Vui lòng chọn người để chat", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            try {
                Message msg = new Message("chat", username, selectedUser, text);
                out.writeObject(msg);
                out.flush();
                addBubble(selectedUser, text, true);
                messageField.setText("");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Lỗi gửi tin nhắn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Gửi file hoặc ảnh. type = "image" hoặc "file".
     */
    private void sendFile(String type) {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "Vui lòng chọn người để chat", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                Message msg = new Message(type, username, selectedUser, file.getName());
                msg.setData(fileBytes);
                out.writeObject(msg);
                out.flush();

                // hiển thị ở giao diện local (preview nếu image, button nếu file)
                if ("image".equals(type)) {
                    addImageBubble(selectedUser, file.getName(), fileBytes, true);
                } else {
                    addFileBubble(selectedUser, file.getName(), fileBytes, true);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Lỗi gửi file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] avatarBytes = Files.readAllBytes(file.toPath());
                Message msg = new Message("avatar", username, null, file.getName());
                msg.setData(avatarBytes);
                out.writeObject(msg);
                out.flush();
                updateAvatar(username, avatarBytes);
                saveLocalAvatar(username, avatarBytes);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Lỗi thay đổi avatar: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateAvatar(String username, byte[] avatarBytes) {
        SwingUtilities.invokeLater(() -> {
            try {
                db.updateAvatar(username, avatarBytes);
            } catch (Exception ignored) {}
            saveLocalAvatar(username, avatarBytes);
            if (username != null && username.equals(this.username)) {
                // avatar cá nhân đã cập nhật
                byte[] my = loadLocalAvatarBytes(this.username);
                if (my != null && avatarLabel != null) {
                    ImageIcon icon = new ImageIcon(my);
                    Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    avatarLabel.setIcon(new ImageIcon(img));
                }
            }
        });
    }

    private void saveFriendAvatar(String friendName, byte[] avatarBytes) {
        saveLocalAvatar(friendName, avatarBytes);
    }

    /**
     * Lưu file nhận được vào thư mục chat_files (tên file = msg.content).
     */
    private void saveReceivedFile(Message msg) {
        try {
            byte[] data = msg.getData();
            if (data == null) return;
            File directory = new File("chat_files");
            if (!directory.exists()) directory.mkdirs();
            File outFile = new File(directory, msg.getContent());
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserList(String onlineUsers) {
        SwingUtilities.invokeLater(() -> {
            if (userListModel == null) userListModel = new DefaultListModel<>();
            userListModel.clear();
            if (onlineUsers != null && !onlineUsers.isEmpty()) {
                for (String user : onlineUsers.split(",")) {
                    user = user.trim();
                    if (!user.isEmpty() && !user.equals(username)) {
                        userListModel.addElement(user);
                        if (!chatPanels.containsKey(user)) {
                            JPanel panel = new JPanel();
                            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                            chatPanels.put(user, panel);
                        }
                    }
                }
            }
            if (userList != null) userList.setModel(userListModel);
        });
    }

    /**
     * Hiển thị message nhận được. Nếu là image/file sẽ gọi hàm hiển thị tương ứng.
     */
    private void showMessageBubble(Message msg) {
        String type = msg.getType();
        String from = msg.getFrom();
        String to = msg.getTo();
        boolean mine = username != null && username.equals(from);
        String chatUser = mine ? to : from;

        if ("image".equals(type)) {
            // dùng data (nếu có) để show preview; nếu không có data thì cố gắng load từ chat_files
            byte[] data = msg.getData();
            if (data == null) {
                // cố đọc file từ chat_files
                try {
                    File f = new File("chat_files", msg.getContent());
                    if (f.exists()) data = Files.readAllBytes(f.toPath());
                } catch (Exception ignored) {}
            }
            addImageBubble(chatUser, msg.getContent(), data, mine);
        } else if ("file".equals(type)) {
            byte[] data = msg.getData();
            if (data == null) {
                try {
                    File f = new File("chat_files", msg.getContent());
                    if (f.exists()) data = Files.readAllBytes(f.toPath());
                } catch (Exception ignored) {}
            }
            addFileBubble(chatUser, msg.getContent(), data, mine);
        } else {
            addBubble(chatUser, msg.getContent(), mine);
        }
    }

    /**
     * Thêm bubble ảnh (preview). Click vào ảnh sẽ mở cửa sổ xem ảnh lớn.
     */
    private void addImageBubble(String targetUser, String filename, byte[] data, boolean isMine) {
        if (targetUser == null) return;
        JPanel panel = chatPanels.get(targetUser);
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            chatPanels.put(targetUser, panel);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JLabel avatar = new JLabel();
        byte[] avatarBytes = loadLocalAvatarBytes(isMine ? username : targetUser);
        if (avatarBytes != null) {
            ImageIcon icon = new ImageIcon(avatarBytes);
            Image img = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));
        } else {
            avatar.setIcon(new ImageIcon(new ImageIcon("default_avatar.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        }

        JLabel imgLabel;
        if (data != null) {
            ImageIcon icon = new ImageIcon(data);
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            imgLabel = new JLabel(new ImageIcon(img));
        } else {
            imgLabel = new JLabel("<Không có dữ liệu ảnh>");
        }
        imgLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        imgLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imgLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // mở ảnh lớn trong JFrame mới
                if (data == null) {
                    JOptionPane.showMessageDialog(frame, "Không có dữ liệu ảnh để hiển thị.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JFrame viewer = new JFrame(filename);
                viewer.setSize(600, 600);
                JLabel big = new JLabel(new ImageIcon(data));
                JScrollPane sp = new JScrollPane(big);
                viewer.add(sp);
                viewer.setLocationRelativeTo(frame);
                viewer.setVisible(true);
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String time = sdf.format(new Date());
        JLabel timeLabel = new JLabel("<html><font size='2'>" + time + "</font></html>");

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(imgLabel, BorderLayout.CENTER);
        contentPanel.add(timeLabel, BorderLayout.SOUTH);

        if (isMine) {
            wrapper.add(contentPanel, BorderLayout.EAST);
            wrapper.add(avatar, BorderLayout.WEST);
        } else {
            wrapper.add(avatar, BorderLayout.WEST);
            wrapper.add(contentPanel, BorderLayout.CENTER);
        }

        panel.add(wrapper);
        panel.add(Box.createVerticalStrut(6));

        if (selectedUser != null && selectedUser.equals(targetUser)) {
            chatPanel.removeAll();
            chatPanel.add(panel);
            chatPanel.add(Box.createVerticalStrut(6));
            chatPanel.revalidate();
            chatPanel.repaint();
        }
    }

    /**
     * Thêm bubble file (nút bấm để lưu/mở).
     */
    private void addFileBubble(String targetUser, String filename, byte[] data, boolean isMine) {
        if (targetUser == null) return;
        JPanel panel = chatPanels.get(targetUser);
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            chatPanels.put(targetUser, panel);
        }

        JPanel wrapper = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));

        JButton fileButton = new JButton("📂 " + filename);
        fileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fileButton.addActionListener(e -> {
            try {
                File dir = new File("downloads");
                if (!dir.exists()) dir.mkdirs();
                File outFile = new File(dir, filename);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    if (data != null) fos.write(data);
                }
                // mở file nếu được hỗ trợ
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(outFile);
                } else {
                    JOptionPane.showMessageDialog(frame, "File đã lưu: " + outFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Không mở được file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String time = sdf.format(new Date());
        JLabel timeLabel = new JLabel("<html><font size='2'>" + time + "</font></html>");

        JPanel box = new JPanel(new BorderLayout());
        box.add(fileButton, BorderLayout.CENTER);
        box.add(timeLabel, BorderLayout.SOUTH);

        wrapper.add(box);

        panel.add(wrapper);
        panel.add(Box.createVerticalStrut(6));

        if (selectedUser != null && selectedUser.equals(targetUser)) {
            chatPanel.removeAll();
            chatPanel.add(panel);
            chatPanel.add(Box.createVerticalStrut(6));
            chatPanel.revalidate();
            chatPanel.repaint();
        }
    }

    private void addBubble(String targetUser, String text, boolean isMine) {
        if (targetUser == null) return;
        JPanel panel = chatPanels.get(targetUser);
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            chatPanels.put(targetUser, panel);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JLabel avatar = new JLabel();
        byte[] avatarBytes = loadLocalAvatarBytes(isMine ? username : targetUser);
        if (avatarBytes != null) {
            ImageIcon icon = new ImageIcon(avatarBytes);
            Image img = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));
        } else {
            avatar.setIcon(new ImageIcon(new ImageIcon("default_avatar.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String time = sdf.format(new Date());

        JLabel msgLabel = new JLabel("<html>" + escapeHtml(text) + "<br><font size='2'>" + time + "</font></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgLabel.setOpaque(true);
        msgLabel.setBackground(new Color(245, 245, 245));
        msgLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        if (isMine) {
            wrapper.add(msgLabel, BorderLayout.EAST);
            wrapper.add(avatar, BorderLayout.WEST);
        } else {
            wrapper.add(avatar, BorderLayout.WEST);
            wrapper.add(msgLabel, BorderLayout.CENTER);
        }

        panel.add(wrapper);
        panel.add(Box.createVerticalStrut(6));

        if (selectedUser != null && selectedUser.equals(targetUser)) {
            chatPanel.removeAll();
            chatPanel.add(panel);
            chatPanel.add(Box.createVerticalStrut(6));
            chatPanel.revalidate();
            chatPanel.repaint();
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private void showEmojiPanel() {
        JDialog emojiDialog = new JDialog(frame, "Chọn emoji", false);
        emojiDialog.setSize(220, 220);
        emojiDialog.setLayout(new GridLayout(4, 4, 6, 6));
        String[] emojis = {"😀","😁","😂","🤣","😃","😄","😅","😆","😉","😊","😍","😘","😎","😢","😭","😡"};
        for (String emoji : emojis) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            btn.addActionListener(e -> {
                messageField.setText(messageField.getText() + emoji);
                emojiDialog.dispose();
            });
            emojiDialog.add(btn);
        }
        emojiDialog.setLocationRelativeTo(frame);
        emojiDialog.setVisible(true);
    }

    private void saveConfig(String username, String password) {
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("password", password);
        try (FileOutputStream fos = new FileOutputStream("config.properties")) {
            props.store(fos, "Chatter Config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig(JTextField usernameField, JPasswordField passwordField) {
        if (usernameField == null || passwordField == null) return;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            usernameField.setText(props.getProperty("username", ""));
            passwordField.setText(props.getProperty("password", ""));
        } catch (IOException ignored) {}
    }

    private void loadLocalAvatar() {
        byte[] avatarBytes = loadLocalAvatarBytes(username);
        if (avatarBytes != null) updateAvatar(username, avatarBytes);
    }

    private void saveLocalAvatar(String user, byte[] avatarBytes) {
        File dir = new File("avatars");
        if (!dir.exists()) dir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(new File(dir, user + ".png"))) {
            fos.write(avatarBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load avatar bytes for user. If user avatar file doesn't exist, try returning default_avatar.png bytes.
     * Returns null only if neither user avatar nor default avatar exist.
     */
    private byte[] loadLocalAvatarBytes(String user) {
        if (user == null) return loadDefaultAvatarBytes();
        File file = new File("avatars", user + ".png");
        if (file.exists()) {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                // continue to fallback
            }
        }
        return loadDefaultAvatarBytes();
    }

    /**
     * Read default_avatar.png bytes if present.
     */
    private byte[] loadDefaultAvatarBytes() {
        File def = new File("default_avatar.png");
        if (def.exists()) {
            try {
                return Files.readAllBytes(def.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
