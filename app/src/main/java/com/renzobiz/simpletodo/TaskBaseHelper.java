package com.renzobiz.simpletodo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.renzobiz.simpletodo.TaskDbSchema.TaskTable;

//a sqliteopenhelper is a class designed to get rid of the grunt work of opening sqlitedatabase
//use it inside of taskmanager to create your task database.

public class TaskBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "taskBase.db";

    public TaskBaseHelper(Context context){
        super(context, DATABASE_NAME, null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TaskTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                TaskTable.Cols.UUID + ", " +
                TaskTable.Cols.Title + ", " +
                TaskTable.Cols.Details + ", " +
                TaskTable.Cols.DueDate + ", " +
                TaskTable.Cols.Complete + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
