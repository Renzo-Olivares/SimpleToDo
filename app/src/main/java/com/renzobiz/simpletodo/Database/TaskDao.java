package com.renzobiz.simpletodo.Database;

import com.renzobiz.simpletodo.Model.Task;

import java.util.List;
import java.util.UUID;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
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

    //insert all
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateAll(List<Task> tasks);

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
