package com.renzobiz.simpletodo.Database;

import android.content.Context;

import com.renzobiz.simpletodo.Model.Task;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Task.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class TaskDataBase extends RoomDatabase {
    public static final String DATABASE_NAME = "task-database";
    private static TaskDataBase INSTANCE;
    public abstract TaskDao taskDao();

    public static TaskDataBase getTaskDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TaskDataBase.class, DATABASE_NAME).build();
        }
        return INSTANCE;
    }

    public static void destroyInstance(){
        INSTANCE = null;
    }


}
