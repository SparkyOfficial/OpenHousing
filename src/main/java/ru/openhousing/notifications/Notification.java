package ru.openhousing.notifications;

/**
 * Класс уведомления
 */
public class Notification {
    
    private final NotificationType type;
    private final String message;
    private final long timestamp;
    private boolean read;
    
    public Notification(NotificationType type, String message, long timestamp) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
}
