package com.renzobiz.simpletodo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskManager {
    private Map<UUID,Task> taskMap;
    private static TaskManager sTaskManager;

    private TaskManager(){
        taskMap = new LinkedHashMap<>();
    }

    public TaskManager get(){
        if(sTaskManager == null){
            sTaskManager = new TaskManager();
        }
        return sTaskManager;
    }

    private List<Task> getTasks(){
        return new ArrayList<>(taskMap.values());
    }

    private Task getTask(UUID taskID){
        return taskMap.get(taskID);
    }
}
