package com.renzobiz.simpletodo;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
    public void onBackPressed() {
        tellFragments();
        super.onBackPressed();
    }

    private void tellFragments(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment f : fragments){
            if(f != null && f instanceof TaskFragment)
                ((TaskFragment)f).onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_pager);

        UUID taskID;

        if (!getIntent().getBooleanExtra(EXTRA_TOOL, false)) {
            Task task = new Task();
            TaskManager.get(this).addAsync(task);
            taskID = task.getTaskId();
        }else{
            taskID = (UUID) getIntent().getSerializableExtra(EXTRA_TASKID);
        }

        try {
            mTasks = TaskManager.get(this).getAllAsync();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
