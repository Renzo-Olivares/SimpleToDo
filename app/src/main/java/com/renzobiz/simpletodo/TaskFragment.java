package com.renzobiz.simpletodo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.UUID;

public class TaskFragment extends Fragment {
    private EditText mTaskTitle;
    private EditText mTaskDetails;
    private Task mTask;
    private static final String ARGS_TASKID = "taskid";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.delete_task:
                TaskManager.get(getActivity()).deleteTask(mTask.getTaskId());
                Intent intent = TaskListActivity.newIntent(getActivity());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task,menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID mTaskID = (UUID) getArguments().getSerializable(ARGS_TASKID);
        mTask = TaskManager.get(getActivity()).getTask(mTaskID);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        TaskManager.get(getActivity()).updateTask(mTask);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task,container,false);
        mTaskTitle = v.findViewById(R.id.task_title);
        mTaskDetails = v.findViewById(R.id.task_details);

        mTaskTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String taskTitle = charSequence.toString();
                mTask.setTaskTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mTaskDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String taskDetails = charSequence.toString();
                mTask.setTaskDetails(taskDetails);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mTaskTitle.setText(mTask.getTaskTitle());
        mTaskDetails.setText(mTask.getTaskDetails());

        return v;
    }

    public static TaskFragment newInstance(UUID mTaskID){
        Bundle args = new Bundle();
        args.putSerializable(ARGS_TASKID, mTaskID);
        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
