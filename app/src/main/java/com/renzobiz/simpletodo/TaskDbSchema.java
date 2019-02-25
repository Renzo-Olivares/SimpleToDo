package com.renzobiz.simpletodo;

public class TaskDbSchema {
    public static final class TaskTable{
        //table title
        public static final String NAME = "tasks";

        //column labels
        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String Title = "title";
            public static final String Details = "details";
            public static final String DueDate = "date";
            public static final String Complete = "completed";
        }
    }
}
