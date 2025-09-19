package model;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;      // loại message: login, register, chat, search, logout...
    private String from;      // người gửi
    private String to;        // người nhận (có thể null nếu gửi server)
    private String content;   // nội dung text
    private byte[] data;      // dữ liệu kèm theo (ảnh, file...)

    // Constructor cơ bản (chat, login, register, search, logout...)
    public Message(String type, String from, String to, String content) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.content = content;
    }

    // Constructor có file/image/avatar
    public Message(String type, String from, String to, String content, byte[] data) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.content = content;
        this.data = data;
    }

    // Getters & Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", content='" + content + '\'' +
                ", data=" + (data != null ? data.length + " bytes" : "null") +
                '}';
    }
}
