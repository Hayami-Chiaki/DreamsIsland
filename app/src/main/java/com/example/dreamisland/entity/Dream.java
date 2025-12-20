// Dream.java
package com.example.dreamisland.entity;

public class Dream {
    private int dreamId;
    private int userId;
    private String title;
    private String content;
    private String nature; // 好梦、噩梦、其他
    private String tags;
    private int isPublic;
    private String createdAt;

    public Dream() {
    }

    public Dream(int userId, String title, String content, String nature, String tags, int isPublic, String createdAt) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.nature = nature;
        this.tags = tags;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getDreamId() { return dreamId; }
    public void setDreamId(int dreamId) { this.dreamId = dreamId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getNature() { return nature; }
    public void setNature(String nature) { this.nature = nature; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public int getIsPublic() { return isPublic; }
    public void setIsPublic(int isPublic) { this.isPublic = isPublic; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Dream{" +
                "dreamId=" + dreamId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", nature='" + nature + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}