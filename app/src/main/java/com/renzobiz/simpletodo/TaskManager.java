package com.renzobiz.simpletodo;

import android.os.AsyncTask;
import android.content.Context;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TaskManager {
    private static TaskManager sTaskManager;
    private static TaskDataBase taskDataBase;

    private TaskManager(Context context){
        PopulateDbAsync task = new PopulateDbAsync(context.getApplicationContext());
        task.execute();
    }

    public static TaskManager get(Context context){
        if(sTaskManager == null){
            sTaskManager = new TaskManager(context);
        }
        return sTaskManager;
    }

    private static void addTask(Task task){
        taskDataBase.taskDao().insert(task);
    }

    public void addAsync(Task task){
        AddDbAsync addTask = new AddDbAsync(task);
        addTask.execute();
    }

    private static void deleteTask(Task task){
        taskDataBase.taskDao().delete(task);
    }

    public void deleteAsync(Task task){
        DelDbAsync delTask = new DelDbAsync(task);
        delTask.execute();
    }

    public List<Task> getAllAsync() throws ExecutionException, InterruptedException {
        GetDbAsync getDb = new GetDbAsync();
        return getDb.execute().get();
    }

    public Task getTaskAsync(UUID taskID) throws ExecutionException, InterruptedException {
        GetTaskAsync getTask = new GetTaskAsync(taskID);
        return getTask.execute().get();
    }

    private static void updateTask(Task task){
        taskDataBase.taskDao().update(task);
    }

    public void updateAsync(Task task){
        UpdateDbAsync updateTask = new UpdateDbAsync(task);
        updateTask.execute();
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final Context newContext;

        PopulateDbAsync(Context context) {
            newContext = context;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            //everything must run in here or else...
            taskDataBase = TaskDataBase.getTaskDatabase(newContext);
            return null;
        }
    }

    private static class UpdateDbAsync extends AsyncTask<Void, Void, Void> {
        private final Task mTask;

        UpdateDbAsync(Task task) {
            mTask = task;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            //everything must run in here or else...
            updateTask(mTask);
            return null;
        }

    }

    private static class AddDbAsync extends AsyncTask<Void, Void, Void> {
        private final Task mTask;

        AddDbAsync(Task task) {
            mTask = task;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            addTask(mTask);
            return null;
        }
    }

    private static class GetDbAsync extends AsyncTask<Void, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(final Void... params) {
            return taskDataBase.taskDao().getAll();
        }
    }

    private static class GetTaskAsync extends AsyncTask<Void, Void, Task> {
        private final UUID mTaskId;

        GetTaskAsync(UUID taskid){
            mTaskId = taskid;
        }

        @Override
        protected Task doInBackground(final Void... params) {
            return taskDataBase.taskDao().findById(mTaskId);
        }
    }

    private static class DelDbAsync extends AsyncTask<Void, Void, Void> {
        private final Task mTask;

        DelDbAsync(Task task) {
            mTask = task;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            deleteTask(mTask);
            return null;
        }
    }
}
