package com.renzobiz.simpletodo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

public class TaskFragment extends Fragment {
    private EditText mTaskTitle;
    private EditText mTaskDetails;
    private Button mDateButton;
    private Task mTask;
    private static final String DIALOG_DATE="DialogDate";
    private static final String ARGS_TASKID = "taskid";
    private static final int REQUEST_DATE = 0;

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
        mDateButton = v.findViewById(R.id.due_button);

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

        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mTask.getTaskDeadline());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTaskTitle.setText(mTask.getTaskTitle());
        mTaskDetails.setText(mTask.getTaskDetails());

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mTask.setTaskDeadline(date);
            updateDate();
        }
    }

    private void updateDate() {
        mDateButton.setText(mTask.getTaskDeadline().toString());
    }

    public static TaskFragment newInstance(UUID mTaskID){
        Bundle args = new Bundle();
        args.putSerializable(ARGS_TASKID, mTaskID);
        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
