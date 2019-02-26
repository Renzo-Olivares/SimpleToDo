package com.renzobiz.simpletodo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.UUID;

public class TaskPagerActivity extends AppCompatActivity {
    private static final String EXTRA_TASKID = "com.renzobiz.android.simpletodo.taskid";
    private static final String EXTRA_TOOL = "com.renzobiz.android.simpletodo.tool";
    private ViewPager mTaskPager;
    private List<Task> mTasks;
    private int currentItem;

    public static Intent newIntent(Context packageContext, UUID taskID, boolean toolbar){
        Intent intent = new Intent(packageContext, TaskPagerActivity.class);
        intent.putExtra(EXTRA_TASKID, taskID);
        intent.putExtra(EXTRA_TOOL, toolbar);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_pager);

        UUID taskID = (UUID) getIntent().getSerializableExtra(EXTRA_TASKID);

        mTasks = TaskManager.get(this).getTasks();
        mTaskPager = findViewById(R.id.task_view_pager);
        FragmentManager fm = getSupportFragmentManager();

        mTaskPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                boolean tool = getIntent().getBooleanExtra(EXTRA_TOOL,false);
                Task task = mTasks.get(position);
                return TaskFragment.newInstance(task.getTaskId(), tool);
            }

            @Override
            public int getCount() {
                return mTasks.size();
            }
        });

        mTaskPager.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(getIntent().getBooleanExtra(EXTRA_TOOL,false)){
                    return false;
                }else{
                    return true;
                }
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
