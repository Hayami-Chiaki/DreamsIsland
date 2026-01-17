package com.example.dreamisland.model;

public class Dream {
    private int dreamId;
    private int userId;
    private String title;
    private String content;
    private String nature; // 好梦、噩梦、其他
    private String tags;
    private boolean isPublic;
    private String createdAt;
    private String username;
    private String previewContent;

    // 构造函数
    public Dream(int dreamId, int userId, String title, String content, String nature, String tags, boolean isPublic, String createdAt) {
        this.dreamId = dreamId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.nature = nature;
        this.tags = tags;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }

    // 无参构造函数
    public Dream() {
    }

    // Getter and Setter methods
    public int getDreamId() {
        return dreamId;
    }

    public void setDreamId(int dreamId) {
        this.dreamId = dreamId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }
}
