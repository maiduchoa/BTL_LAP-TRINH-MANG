package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import model.Message;
import utils.Database;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9090;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
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
    private Map<String, JPanel> chatPanels = new HashMap<>(); // Chat riêng theo từng user

    private volatile boolean running = false;
    private BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<>();

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

    private void connectToServer() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        running = true;

        new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    if (!(obj instanceof Message msg)) continue;
                    String type = msg.getType();
                    if (type == null) continue;

                    switch (type) {
                        case "chat" -> SwingUtilities.invokeLater(() -> showMessageBubble(msg));
                        case "image", "file" -> {
                            saveReceivedFile(msg);
                            SwingUtilities.invokeLater(() -> showMessageBubble(msg));
                        }
                        case "avatar" -> {
                            updateAvatar(msg.getFrom(), msg.getData());
                            saveFriendAvatar(msg.getFrom(), msg.getData());
                            userList.repaint();
                        }
                        case "online_list" -> updateUserList(msg.getContent());
                        case "login", "register", "logout" -> responseQueue.offer(msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (running) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame, "Mất kết nối với máy chủ: " + e.getMessage(),
                                    "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
            }
        }).start();
    }

    private void showLoginWindow() {
        JFrame loginFrame = new JFrame("Chatter - Đăng Nhập");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 250);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Ứng Dụng Chatter", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.add(new JLabel("Tên người dùng:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Mật khẩu:"));
        inputPanel.add(passwordField);

        JButton loginButton = new JButton("Đăng Nhập");
        JButton registerButton = new JButton("Đăng Ký");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        loadConfig(usernameField, passwordField);

        loginButton.addActionListener(e -> {
            try {
                connectToServer();
                Message msg = new Message("login", usernameField.getText(), null,
                        new String(passwordField.getPassword()));
                out.writeObject(msg);
                out.flush();

                Message response = responseQueue.take();
                if ("Đăng nhập thành công".equals(response.getContent())) {
                    username = usernameField.getText();
                    saveConfig(username, new String(passwordField.getPassword()));
                    loginFrame.dispose();
                    showChatWindow();
                    loadLocalAvatar();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, response.getContent(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(loginFrame, "Lỗi kết nối: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        registerButton.addActionListener(e -> {
            try {
                connectToServer();
                Message msg = new Message("register", usernameField.getText(), null,
                        new String(passwordField.getPassword()));
                out.writeObject(msg);
                out.flush();

                Message response = responseQueue.take();
                JOptionPane.showMessageDialog(loginFrame, response.getContent(),
                        response.getFrom(), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(loginFrame, "Lỗi kết nối: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }

    private void showChatWindow() {
        frame = new JFrame("Chatter - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

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
                panel.setBorder(new EmptyBorder(2, 2, 2, 2));
                panel.setBackground(isSelected ? new Color(200, 200, 255) : Color.WHITE);

                String user = (String) value;
                JLabel label = new JLabel(user);
                label.setBorder(new EmptyBorder(5,5,5,5));
                label.setFont(new Font("Arial", Font.PLAIN, 14));

                byte[] avatarBytes = loadLocalAvatarBytes(user);
                ImageIcon icon;
                if (avatarBytes != null) icon = new ImageIcon(avatarBytes);
                else icon = new ImageIcon(new ImageIcon("default_avatar.png").getImage());

                Image img = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
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
                if(selectedUser != null && chatPanels.containsKey(selectedUser)){
                    chatPanel.add(chatPanels.get(selectedUser));
                }
                updateChatAvatar(selectedUser);
                chatPanel.revalidate();
                chatPanel.repaint();
            }
        });

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(250, 0));

        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftTopPanel.setBorder(new EmptyBorder(5,5,5,5));
        JLabel myAvatarLabel = new JLabel();
        myAvatarLabel.setPreferredSize(new Dimension(40,40));
        byte[] myAvatarBytes = loadLocalAvatarBytes(username);
        if(myAvatarBytes != null){
            ImageIcon icon = new ImageIcon(myAvatarBytes);
            Image img = icon.getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH);
            myAvatarLabel.setIcon(new ImageIcon(img));
        } else {
            ImageIcon icon = new ImageIcon(new ImageIcon("default_avatar.png").getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH));
            myAvatarLabel.setIcon(icon);
        }
        JLabel myNameLabel = new JLabel(username);
        myNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
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
            public void mouseClicked(MouseEvent e){
                avatarMenu.show(myAvatarLabel, e.getX(), e.getY());
            }
        });
        changeAvatarItem.addActionListener(e -> changeAvatar());
        logoutItem.addActionListener(e -> logout());

        JPanel leftPanel = new JPanel(new BorderLayout(5,5));
        leftPanel.add(leftTopPanel, BorderLayout.NORTH);
        leftPanel.add(userScroll, BorderLayout.CENTER);

        // ===== Center: Chat =====
        JPanel chatContainer = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        chatTitle = new JLabel("Chatter");
        chatTitle.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(avatarLabel);
        topPanel.add(chatTitle);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane chatScroll = new JScrollPane(chatPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
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

        JPanel buttonPanel = new JPanel(new FlowLayout());
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

    private void updateChatAvatar(String user){
        if(user == null) return;
        byte[] avatarBytes = loadLocalAvatarBytes(user);
        if(avatarBytes != null){
            ImageIcon icon = new ImageIcon(avatarBytes);
            Image img = icon.getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(img));
        } else {
            ImageIcon icon = new ImageIcon(new ImageIcon("default_avatar.png").getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH));
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
        showLoginWindow();
    }

    private void sendMessage() {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "Vui lòng chọn người để chat",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            try {
                Message msg = new Message("chat", username, selectedUser, text);
                out.writeObject(msg);
                out.flush();
                addBubble(selectedUser, text, true); // Thêm vào chat riêng của người này
                messageField.setText("");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Lỗi gửi tin nhắn: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendFile(String type) {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "Vui lòng chọn người để chat",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                addBubble(selectedUser, "đã gửi " + type + ": " + file.getName(), true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Lỗi gửi file: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "png"));
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
                JOptionPane.showMessageDialog(frame, "Lỗi thay đổi avatar: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateAvatar(String username, byte[] avatarBytes) {
        SwingUtilities.invokeLater(() -> {
            db.updateAvatar(username, avatarBytes);
            saveLocalAvatar(username, avatarBytes);
        });
    }

    private void saveFriendAvatar(String friendName, byte[] avatarBytes) {
        saveLocalAvatar(friendName, avatarBytes);
    }

    private void saveReceivedFile(Message msg) {
        try {
            byte[] data = msg.getData();
            File directory = new File("chat_files");
            if (!directory.exists()) directory.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(new File(directory, msg.getContent()))) {
                fos.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserList(String onlineUsers) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            if (onlineUsers != null && !onlineUsers.isEmpty()) {
                for (String user : onlineUsers.split(",")) {
                    if (!user.equals(username)) {
                        userListModel.addElement(user);
                        if (!chatPanels.containsKey(user)) {
                            JPanel panel = new JPanel();
                            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                            chatPanels.put(user, panel);
                        }
                    }
                }
            }
        });
    }

    private void showMessageBubble(Message msg) {
        addBubble(msg.getFrom(), msg.getContent(), msg.getFrom().equals(username));
    }

    private void addBubble(String targetUser, String text, boolean isMine){
        if(targetUser == null) return;
        JPanel panel = chatPanels.get(targetUser);
        if(panel == null){
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            chatPanels.put(targetUser, panel);
        }

        JPanel wrapper = new JPanel(new BorderLayout(5,5));
        wrapper.setOpaque(false);

        JLabel avatar = new JLabel();
        byte[] avatarBytes = loadLocalAvatarBytes(isMine ? username : targetUser);
        if(avatarBytes != null){
            ImageIcon icon = new ImageIcon(avatarBytes);
            Image img = icon.getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));
        } else {
            avatar.setIcon(new ImageIcon(new ImageIcon("default_avatar.png").getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH)));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String time = sdf.format(new Date());

        JLabel msgLabel = new JLabel("<html>" + text + "<br><font size='2'>" + time + "</font></html>");
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        msgLabel.setOpaque(true);
        msgLabel.setBackground(new Color(245,245,245));
        msgLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1,true),
                new EmptyBorder(8,12,8,12)
        ));

        JPanel bubblePanel = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        bubblePanel.setOpaque(false);
        if(isMine) bubblePanel.add(msgLabel);
        else{
            bubblePanel.add(avatar);
            bubblePanel.add(msgLabel);
        }

        wrapper.add(bubblePanel, BorderLayout.CENTER);
        panel.add(wrapper);
        panel.revalidate();
        panel.repaint();

        if(selectedUser != null && selectedUser.equals(targetUser)){
            chatPanel.removeAll();
            chatPanel.add(panel);
            chatPanel.revalidate();
            chatPanel.repaint();
        }
    }

    private void saveConfig(String username, String password) {
        Properties prop = new Properties();
        try (FileOutputStream fos = new FileOutputStream("resources/config.properties")) {
            prop.setProperty("username", username);
            prop.setProperty("password", password);
            prop.store(fos, null);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadConfig(JTextField usernameField, JPasswordField passwordField) {
        Properties prop = new Properties();
        File configFile = new File("resources/config.properties");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                prop.load(fis);
                usernameField.setText(prop.getProperty("username", ""));
                passwordField.setText(prop.getProperty("password", ""));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void saveLocalAvatar(String user, byte[] avatarBytes) {
        try {
            File dir = new File("avatars");
            if (!dir.exists()) dir.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(new File(dir, user + ".png"))) {
                fos.write(avatarBytes);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadLocalAvatar() {
        byte[] avatarBytes = loadLocalAvatarBytes(username);
        if (avatarBytes != null) {
            ImageIcon icon = new ImageIcon(avatarBytes);
            Image image = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(image));
        }
    }

    private byte[] loadLocalAvatarBytes(String user) {
        File file = new File("avatars/" + user + ".png");
        if (file.exists()) {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) { e.printStackTrace(); }
        }
        return null;
    }

    private void showEmojiPanel() {
        JDialog emojiDialog = new JDialog(frame, "Chọn Emoji", false);
        emojiDialog.setSize(300,100);
        emojiDialog.setLayout(new FlowLayout());

        String[] emojis = {"😊","😂","😍","😢","👍","🙏"};
        for(String e: emojis){
            JButton btn = new JButton(e);
            btn.setFont(new Font("Segoe UI Emoji",Font.PLAIN,20));
            btn.addActionListener(ae -> {
                messageField.setText(messageField.getText() + e);
                emojiDialog.dispose();
            });
            emojiDialog.add(btn);
        }
        emojiDialog.setLocationRelativeTo(frame);
        emojiDialog.setVisible(true);
    }
}
