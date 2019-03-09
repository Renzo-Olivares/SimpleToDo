package com.renzobiz.simpletodo.Controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;
import com.renzobiz.simpletodo.Controller.Helpers.IOnBackPressed;
import com.renzobiz.simpletodo.Model.Task;
import com.renzobiz.simpletodo.Model.TaskManager;
import com.renzobiz.simpletodo.R;
import com.renzobiz.simpletodo.Worker.NotificationWorker;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.FULL;
import static java.text.DateFormat.SHORT;

public class TaskFragment extends Fragment implements IOnBackPressed {
    private EditText mTaskTitle;
    private EditText mTaskDetails;
    private Button mDueDateText;
    private Button mDueTimeText;
    private CardView mCardReminder;
    private Switch mReminderSwitch;
    private Task mTask;
    private Date mMasterDate;
    private MaterialButton mSaveButton;
    private static final String DIALOG_DATE="DialogDate";
    private static final String DIALOG_TIME="DialogTime";
    private static final String ARGS_TASKID = "taskid";
    private static final String ARGS_TOOL = "tool";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static int back_counter = 0;

   @Override
    public void onBackPressed() {
       if (back_counter == 0) {
           if(!getArguments().getBoolean(ARGS_TOOL)){
               back_counter++;
               Task saveTask = mTask;
               if(mTask.isRemindersEnabled() && remindHasChanged()){
                   createWork();
               }
               TaskManager.get(getActivity()).deleteAsync(mTask);
               backPressIntent(true, saveTask);
           }else{
               back_counter++;
               if(mTask.isRemindersEnabled() && remindHasChanged()){
                   createWork();
               }
               backPressIntent(false,null);
           }
       }
    }

    private boolean remindHasChanged() {
       boolean isReminderChanged;
       long originTime = mMasterDate.getTime();
       long newTime = mTask.getTaskDeadline().getTime();

       if(newTime ==  originTime){
           isReminderChanged = false;
       }else if(newTime > originTime){
           isReminderChanged = (newTime > new Date().getTime())? true:false;
       }else{
           isReminderChanged = (newTime > new Date().getTime()? true:false);
       }

       return isReminderChanged;
    }

    private void backPressIntent(boolean hasDraft, Task task) {
        Intent intent = TaskListActivity.newIntent(getActivity(), hasDraft, task ,false);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_task, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID mTaskID = (UUID) getArguments().getSerializable(ARGS_TASKID);
        back_counter = 0;

        try {
            mTask = TaskManager.get(getActivity()).getTaskAsync(mTaskID);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mMasterDate = mTask.getTaskDeadline();
        setHasOptionsMenu(getArguments().getBoolean(ARGS_TOOL));
    }

    @Override
    public void onPause() {
        super.onPause();
        TaskManager.get(getActivity()).updateAsync(mTask);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task,container,false);
        boolean tool = getArguments().getBoolean(ARGS_TOOL);
        setUpToolbar(v, tool);
        mTaskTitle = v.findViewById(R.id.task_title);
        mTaskDetails = v.findViewById(R.id.task_details);
        mDueDateText = v.findViewById(R.id.date_button);
        mDueTimeText = v.findViewById(R.id.time_button);
        mSaveButton = v.findViewById(R.id.save_button);
        mCardReminder = v.findViewById(R.id.cardView);
        mReminderSwitch = v.findViewById(R.id.switchReminder);

        if(!getArguments().getBoolean(ARGS_TOOL)){
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mTask.isRemindersEnabled() && remindHasChanged()){
                        createWork();
                    }
                    backPressIntent(false,null);
                }
            });
        }
        else{
            mSaveButton.setVisibility(View.GONE);
        }

        mCardReminder.setVisibility(mTask.isRemindersEnabled()? View.VISIBLE:View.INVISIBLE);

        mTaskTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String taskTitle = charSequence.toString();
                    mTask.setTaskTitle(taskTitle);
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

        mReminderSwitch.setChecked(mTask.isRemindersEnabled());
        mReminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mCardReminder.setVisibility(View.VISIBLE);
                    mTask.setRemindersEnabled(b);
                }else{
                    mCardReminder.setVisibility(View.INVISIBLE);
                    mTask.setRemindersEnabled(b);
                }
            }
        });

        mTaskTitle.setText(mTask.getTaskTitle());
        mTaskDetails.setText(mTask.getTaskDetails());

        return v;
    }

    private void createWork() {
        String TAG_NOTIFICATIONS = mTask.getTaskTitle().toUpperCase().replace(" ", "_");

        Data data = new Data.Builder()
                .putString(NotificationWorker.EXTRA_TITLE, mTask.getTaskTitle())
                .putString(NotificationWorker.EXTRA_ID, mTask.getTaskId().toString())
                .putString(NotificationWorker.EXTRA_DETAILS, mTask.getTaskDetails())
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(calculateDelay(mTask.getTaskDeadline()), TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(TAG_NOTIFICATIONS)
                .build();

        WorkManager.getInstance().enqueueUniqueWork(TAG_NOTIFICATIONS, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
    }

    private long calculateDelay(Date taskDeadline) {
       long targetMilli = taskDeadline.getTime();
       long initialMilli = new Date().getTime();
       long delay = 25000;

       return (targetMilli - initialMilli) - delay;
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

                Calendar cal = setupCalendar(mTask.getTaskDeadline());
                cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));

                Date newDate = cal.getTime();
                mTask.setTaskDeadline(newDate);
                updateDate();
                break;
            case REQUEST_TIME:
                Date dateTime = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                Calendar timeCalendar = setupCalendar(dateTime);

                Calendar time = setupCalendar(mTask.getTaskDeadline());
                time.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                time.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

                Date newTime = time.getTime();

                mTask.setTaskDeadline(newTime);
                updateDate();
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
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbarEdit.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() ==  R.id.delete_task) {
                            Task saveTask = mTask;
                            TaskManager.get(getActivity()).deleteAsync(mTask);
                            Intent intent = TaskListActivity.newIntent(getActivity(), true, saveTask, true);
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }
                });
                toolbarEdit.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mTask.isRemindersEnabled() && remindHasChanged()){
                            createWork();
                        }
                        getActivity().onBackPressed();
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
