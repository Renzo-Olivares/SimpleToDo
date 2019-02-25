package com.renzobiz.simpletodo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.renzobiz.simpletodo.TaskDbSchema.TaskTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskManager {
    private static TaskManager sTaskManager;
    private Context newContext;
    private SQLiteDatabase taskDataBase;

    private TaskManager(Context context){
        newContext = context.getApplicationContext();
        taskDataBase = new TaskBaseHelper(newContext).getWritableDatabase();
    }

    public static TaskManager get(Context context){
        if(sTaskManager == null){
            sTaskManager = new TaskManager(context);
        }
        return sTaskManager;
    }

    public void addTask(Task task){
        ContentValues values = getContentValues(task);
        taskDataBase.insert(TaskTable.NAME, null, values);
    }

    public void deleteTask(UUID taskID){
        String uuidString = taskID.toString();
        taskDataBase.delete(TaskTable.NAME, TaskTable.Cols.UUID + " = ?", new String[] { uuidString });
    }

    public List<Task> getTasks(){
        Map<UUID, Task> tasks= new LinkedHashMap<>();

        TaskCursorWrapper cursor = queryTasks(null,null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                tasks.put(cursor.getTask().getTaskId(), cursor.getTask());
                cursor.moveToNext();
            }
        } finally{
            cursor.close();
        }

        return new ArrayList<>(tasks.values());
    }

    public Task getTask(UUID taskID){
        TaskCursorWrapper cursor = queryTasks(
                TaskTable.Cols.UUID + " = ?",
                new String[] { taskID.toString() }
        );

        try{
            if(cursor.getCount() == 0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getTask();
        }finally{
            cursor.close();
        }
    }

    private static ContentValues getContentValues(Task task){
        ContentValues values = new ContentValues();
        values.put(TaskTable.Cols.UUID, task.getTaskId().toString());
        values.put(TaskTable.Cols.Title, task.getTaskTitle());
        values.put(TaskTable.Cols.Details, task.getTaskDetails());
        values.put(TaskTable.Cols.DueDate, task.getTaskDeadline().getTime());
        values.put(TaskTable.Cols.Complete, task.isTaskComplete()? 1:0);

        return values;
    }

    public void updateTask(Task task){
        String uuidString = task.getTaskId().toString();
        ContentValues values = getContentValues(task);

        taskDataBase.update(TaskTable.NAME, values,
                TaskTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private TaskCursorWrapper queryTasks(String whereClause, String[] whereArgs){
        Cursor cursor = taskDataBase.query(
                TaskTable.NAME,
                null,//columns - null selects all columns
                whereClause,
                whereArgs,
                null,//groupby
                null,//having
                null//orderby
        );
        return new TaskCursorWrapper(cursor);
    }
}
