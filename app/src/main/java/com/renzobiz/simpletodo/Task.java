package com.renzobiz.simpletodo;

import java.util.Date;
import java.util.UUID;

public class Task {
    private String taskTitle;
    private String taskDetails;
    private Date taskDeadline;
    private UUID taskId;
    boolean taskComplete;

    public boolean isTaskComplete() {
        return taskComplete;
    }

    public void setTaskComplete(boolean taskComplete) {
        this.taskComplete = taskComplete;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public Date getTaskDeadline() {
        return taskDeadline;
    }

    public void setTaskDeadline(Date taskDeadline) {
        this.taskDeadline = taskDeadline;
    }

    public Task(){
        taskId = UUID.randomUUID();
        taskDeadline = new Date();
    }
}
