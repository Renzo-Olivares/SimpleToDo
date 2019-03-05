package com.renzobiz.simpletodo;

import android.content.Context;
import java.util.List;
import java.util.UUID;

public class TaskManager {
    private static TaskManager sTaskManager;
    private Context newContext;
    private TaskDataBase taskDataBase;

    private TaskManager(Context context){
        newContext = context.getApplicationContext();
        taskDataBase = TaskDataBase.getTaskDatabase(newContext);
    }

    public static TaskManager get(Context context){
        if(sTaskManager == null){
            sTaskManager = new TaskManager(context);
        }
        return sTaskManager;
    }

    public void addTask(Task task){
        taskDataBase.taskDao().insert(task);
    }

    public void deleteTask(Task task){
        taskDataBase.taskDao().delete(task);
    }

    public List<Task> getTasks(){
        return taskDataBase.taskDao().getAll();
    }

    public Task getTask(UUID taskID){
        return taskDataBase.taskDao().findById(taskID);
    }

    public void updateTask(Task task){
        taskDataBase.taskDao().update(task);
    }
}
