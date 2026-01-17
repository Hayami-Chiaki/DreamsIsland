// BodyState.java
package com.example.dreamisland.entity;

import androidx.annotation.NonNull;

public class BodyState {
    private int stateId;
    private int userId;
    private String state; // 疲惫、精神、一般
    private String recordDate; // yyyy-MM-dd

    public BodyState() {
    }

    public BodyState(int userId, String state, String recordDate) {
        this.userId = userId;
        this.state = state;
        this.recordDate = recordDate;
    }

    // Getters and Setters
    public int getStateId() { return stateId; }
    public void setStateId(int stateId) { this.stateId = stateId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }

    @NonNull
    @Override
    public String toString() {
        return "BodyState{" +
                "stateId=" + stateId +
                ", userId=" + userId +
                ", state='" + state + '\'' +
                ", recordDate='" + recordDate + '\'' +
                '}';
    }
}