package com.renzobiz.simpletodo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.UUID;

public class Task implements Parcelable {
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
        this(UUID.randomUUID());
    }

    public Task(UUID id){
        taskId = id;
        taskDeadline = new Date();
    }

    public Task(Parcel source){
        taskTitle = source.readString();
        taskDetails = source.readString();
        taskComplete = source.readByte() != 0;
        taskDeadline = (Date) source.readSerializable();
        taskId = (UUID) source.readSerializable();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(taskTitle);
        dest.writeString(taskDetails);
        dest.writeByte((byte) (taskComplete ? 1:0));
        dest.writeSerializable(taskDeadline);
        dest.writeSerializable(taskId);
    }

    //parcelable boiler plate code
    public static final Creator<Task> CREATOR = new Creator<Task>(){
        @Override
        public Task[] newArray(int size){
            return new Task[size];
        }

        @Override
        public Task createFromParcel(Parcel source){
            return new Task(source);
        }
    };
}
