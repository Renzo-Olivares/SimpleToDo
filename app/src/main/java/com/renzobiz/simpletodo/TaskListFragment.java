package com.renzobiz.simpletodo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TaskListFragment extends Fragment {
    private RecyclerView mTaskRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_list,container,false);
        mTaskRecycler = v.findViewById(R.id.task_recycler);
        mTaskRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
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