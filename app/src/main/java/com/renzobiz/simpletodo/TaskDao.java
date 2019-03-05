package com.renzobiz.simpletodo;

import java.util.List;
import java.util.UUID;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskDao {
    //get all
    @Query("SELECT * FROM task")
    List<Task> getAll();

    //find by UUID
    @Query("SELECT * FROM task where taskid LIKE  :taskid ")
    Task findById(UUID taskid);

    //number of tasks
    @Query("SELECT COUNT(*) from task")
    int countTasks();

    //insert all
    @Insert
    void insertAll(Task... tasks);

    //insert one
    @Insert
    void insert(Task task);

    //remove
    @Delete
    void delete(Task sinTask);

    //update
    @Update
    void update(Task task);
}
