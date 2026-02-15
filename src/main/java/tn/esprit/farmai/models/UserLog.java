package tn.esprit.farmai.models;

import java.time.LocalDateTime;

/**
 * Entity representing a log entry for actions performed on users.
 */
public class UserLog {
    private long id;
    private int userId; // ID of the affected user
    private UserLogAction actionType;
    private String performedBy; // Person who performed the action
    private LocalDateTime timestamp;
    private String description;

    public UserLog() {
    }

    public UserLog(int userId, UserLogAction actionType, String performedBy, String description) {
        this.userId = userId;
        this.actionType = actionType;
        this.performedBy = performedBy;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public UserLogAction getActionType() {
        return actionType;
    }

    public void setActionType(UserLogAction actionType) {
        this.actionType = actionType;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "UserLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", actionType=" + actionType +
                ", performedBy='" + performedBy + '\'' +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}
