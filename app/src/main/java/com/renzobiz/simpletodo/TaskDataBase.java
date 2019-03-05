package com.renzobiz.simpletodo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Task.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class TaskDataBase extends RoomDatabase {
    private static TaskDataBase INSTANCE;
    public abstract TaskDao taskDao();

    public static TaskDataBase getTaskDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TaskDataBase.class, "task-database").build();
        }
        return INSTANCE;
    }

    public static void destroyInstance(){
        INSTANCE = null;
    }


}
