package com.renzobiz.simpletodo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;
import java.util.UUID;

public class TaskPagerActivity extends AppCompatActivity {
    private static final String EXTRA_TASKID = "com.renzobiz.android.simpletodo.taskid";
    private ViewPager mTaskPager;
    private List<Task> mTasks;
    private int currentItem;

    public static Intent newIntent(Context packageContext, UUID taskID){
        Intent intent = new Intent(packageContext, TaskPagerActivity.class);
        intent.putExtra(EXTRA_TASKID, taskID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_pager);

        UUID taskID = (UUID) getIntent().getSerializableExtra(EXTRA_TASKID);
        mTasks = TaskManager.get().getTasks();
        mTaskPager = findViewById(R.id.task_view_pager);
        FragmentManager fm = getSupportFragmentManager();

        mTaskPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                Task task = mTasks.get(position);
                return TaskFragment.newInstance(task.getTaskId());
            }

            @Override
            public int getCount() {
                return mTasks.size();
            }
        });

        for(int i = 0; i < mTasks.size(); i++){
            if(mTasks.get(i).getTaskId().equals(taskID)){
                currentItem = i;
                mTaskPager.setCurrentItem(currentItem);
                break;
            }
        }
    }
}
