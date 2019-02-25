package com.renzobiz.simpletodo;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;

import static com.renzobiz.simpletodo.TaskDbSchema.TaskTable.Cols.UUID;
import static java.util.UUID.fromString;

public class TaskCursorWrapper extends CursorWrapper {
    public TaskCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Task getTask(){
        //extract data
        String uuidString = getString(getColumnIndex(TaskDbSchema.TaskTable.Cols.UUID));
        String title = getString(getColumnIndex(TaskDbSchema.TaskTable.Cols.Title));
        String details = getString(getColumnIndex(TaskDbSchema.TaskTable.Cols.Details));
        long dueDate = getLong(getColumnIndex(TaskDbSchema.TaskTable.Cols.DueDate));
        int isCompleted = getInt(getColumnIndex(TaskDbSchema.TaskTable.Cols.Complete));

        //assign data to new task
        Task task = new Task(fromString(uuidString));
        task.setTaskTitle(title);
        task.setTaskDetails(details);
        task.setTaskDeadline(new Date(dueDate));
        task.setTaskComplete(isCompleted != 0);

        return task;
    }
}
