package com.renzobiz.simpletodo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static java.text.DateFormat.FULL;
import static java.text.DateFormat.SHORT;

public class TaskFragment extends Fragment {
    private EditText mTaskTitle;
    private EditText mTaskDetails;
    private TextView mDueDateText;
    private TextView mDueTimeText;
    private Task mTask;
    private Date mMasterDate;
    private MaterialButton mSaveButton;
    private static final String DIALOG_DATE="DialogDate";
    private static final String DIALOG_TIME="DialogTime";
    private static final String ARGS_TASKID = "taskid";
    private static final String ARGS_TOOL = "tool";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;

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
        inflater.inflate(R.menu.fragment_task,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID mTaskID = (UUID) getArguments().getSerializable(ARGS_TASKID);
        mTask = TaskManager.get(getActivity()).getTask(mTaskID);
        setHasOptionsMenu(getArguments().getBoolean(ARGS_TOOL));
    }

    @Override
    public void onPause() {
        super.onPause();
        TaskManager.get(getActivity()).updateTask(mTask);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task,container,false);
        boolean tool = getArguments().getBoolean(ARGS_TOOL);
        setUpToolbar(v, tool);
        mTaskTitle = v.findViewById(R.id.task_title);
        mTaskDetails = v.findViewById(R.id.task_details);
        mDueDateText = v.findViewById(R.id.date_text_view);
        mDueTimeText = v.findViewById(R.id.due_time_text_view);
        mSaveButton = v.findViewById(R.id.save_button);



        mMasterDate = mTask.getTaskDeadline();

        if(!getArguments().getBoolean(ARGS_TOOL)){
            v.setFocusableInTouchMode(true);
            v.requestFocus();
            v.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if(i == KeyEvent.KEYCODE_BACK)
                    {
                        TaskManager.get(getActivity()).deleteTask((UUID) getArguments().getSerializable(ARGS_TASKID));
                        Intent intent = TaskListActivity.newIntent(getActivity());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        getActivity().finish();
                        return true;
                    }
                    return false;
                }
            });

            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = TaskListActivity.newIntent(getActivity());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }
        else{
            mSaveButton.setVisibility(View.GONE);
        }

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
        mDueDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mTask.getTaskDeadline());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mDueTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mTask.getTaskDeadline());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
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

        switch(requestCode) {
            case REQUEST_DATE:
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                Calendar calendar = setupCalendar(date);

                Calendar cal = setupCalendar(mMasterDate);
                cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));

                Date newDate = cal.getTime();
                mTask.setTaskDeadline(newDate);
                updateDate(cal);
                break;
            case REQUEST_TIME:
                Date dateTime = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                Calendar timeCalendar = setupCalendar(dateTime);

                Calendar time = setupCalendar(mMasterDate);
                time.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                time.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

                Date newTime = time.getTime();

                mTask.setTaskDeadline(newTime);
                updateDate(time);
                break;
            default:
                super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private Calendar setupCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    private void updateDate(Calendar cal) {
        mDueDateText.setText(DateFormat.getDateInstance(FULL).format(mTask.getTaskDeadline()));
        mDueTimeText.setText(DateFormat.getTimeInstance(SHORT).format(mTask.getTaskDeadline()));
        mMasterDate = cal.getTime();
    }

    private void updateDate(){
        mDueDateText.setText(DateFormat.getDateInstance(FULL).format(mTask.getTaskDeadline()));
        mDueTimeText.setText(DateFormat.getTimeInstance(SHORT).format(mTask.getTaskDeadline()));
    }

    public static TaskFragment newInstance(UUID mTaskID, boolean tool){
        Bundle args = new Bundle();
        args.putSerializable(ARGS_TASKID, mTaskID);
        args.putSerializable(ARGS_TOOL, tool);
        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void setUpToolbar(View view, boolean tool) {
        Toolbar toolbarEdit = view.findViewById(R.id.edit_task_app_bar);
        Toolbar toolbarNew = view.findViewById(R.id.new_task_app_bar);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            if(tool){
                activity.setSupportActionBar(toolbarEdit);
                toolbarNew.setVisibility(View.GONE);
                toolbarEdit.inflateMenu(R.menu.fragment_task);//changed
                toolbarEdit.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() ==  R.id.delete_task) {
                            TaskManager.get(getActivity()).deleteTask(mTask.getTaskId());
                            Intent intent = TaskListActivity.newIntent(getActivity());
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }
                });
                toolbarEdit.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().onNavigateUp();
                    }
                });
            }
            else{
                activity.setSupportActionBar(toolbarNew);
                toolbarEdit.setVisibility(View.GONE);
            }
        }
    }


}
