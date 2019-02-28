package com.renzobiz.simpletodo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TaskListActivity extends AppCompatActivity {
    private static final String EXTRA_DRAFT= "com.renzobiz.android.simpletodo.hasdraft";
    private static final String EXTRA_SAVETASK= "com.renzobiz.android.simpletodo.savetask";

    public static Intent newIntent(Context packageContext, boolean hasDraft, Task saveTask){
        Intent intent = new Intent(packageContext, TaskListActivity.class);
        intent.putExtra(EXTRA_DRAFT, hasDraft);
        intent.putExtra(EXTRA_SAVETASK, saveTask);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        boolean draftSnack = getIntent().getBooleanExtra(EXTRA_DRAFT, false);
        Task saveTask = getIntent().getParcelableExtra(EXTRA_SAVETASK);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = TaskListFragment.newInstance(draftSnack, saveTask);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
