package com.renzobiz.simpletodo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.List;

import static java.text.DateFormat.FULL;

public class TaskListFragment extends Fragment {
    private RecyclerView mTaskRecycler;
    private FloatingActionButton mFloatingActionButton;
    private TaskAdapter mAdapter;

    @Override
    public void onResume() {
        super.onResume();
        updateUI(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_list,container,false);
        mTaskRecycler = v.findViewById(R.id.task_recycler);
        mFloatingActionButton = v.findViewById(R.id.add_task);
        final TaskManager tm = TaskManager.get();

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task task = new Task();
                tm.addTask(task);
                updateUI(true);
                //Toast.makeText(getActivity(), "HELLO", Toast.LENGTH_SHORT).show();
            }
        });

        mTaskRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI(false);
        return v;
    }

    private void updateUI(boolean newAdapter){
        TaskManager tm = TaskManager.get();
        List<Task> tasks = tm.getTasks();

        if(mAdapter == null | newAdapter){
            mAdapter =  new TaskAdapter(tasks);
            mTaskRecycler.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
        }

    }

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTaskTitle;
        private TextView mTaskDate;

        @Override
        public void onClick(View view) {

        }

        public TaskHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_task,parent,false));
            mTaskTitle = itemView.findViewById(R.id.task_title);
            mTaskDate = itemView.findViewById(R.id.task_date);
        }

        public void bind(Task task){
            String date = DateFormat.getDateInstance(FULL).format(task.getTaskDeadline());
            //mTaskTitle.setText(task.getTaskTitle());
            mTaskDate.setText(date);
        }
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder>{
        private List<Task> mTasks;

        @Override
        public TaskHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new TaskHolder(inflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(TaskHolder taskHolder, int position) {
            Task task = mTasks.get(position);
            taskHolder.bind(task);
        }

        public TaskAdapter(List<Task> tasks){
            mTasks = tasks;
        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }
    }
}