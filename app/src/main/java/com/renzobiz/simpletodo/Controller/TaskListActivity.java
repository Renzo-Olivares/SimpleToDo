package com.renzobiz.simpletodo.Controller;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Parcelable;

import com.renzobiz.simpletodo.Model.Task;
import com.renzobiz.simpletodo.Model.TaskManager;
import com.renzobiz.simpletodo.R;

import java.util.Arrays;

public class TaskListActivity extends AppCompatActivity {
    private static final String EXTRA_DRAFT= "com.renzobiz.android.simpletodo.hasdraft";
    private static final String EXTRA_SAVETASK= "com.renzobiz.android.simpletodo.savetask";
    private static final String EXTRA_NOTDRAFT= "com.renzobiz.android.simpletodo.notdraft";
    private static final String NEW_TASK_SHORTCUT= "ADD_NEW_TASK";

    public static Intent newIntent(Context packageContext, boolean hasDraft, Task saveTask, boolean isNotDraft){
        Intent intent = new Intent(packageContext, TaskListActivity.class);
        intent.putExtra(EXTRA_DRAFT, hasDraft);
        intent.putExtra(EXTRA_SAVETASK, (Parcelable) saveTask);
        intent.putExtra(EXTRA_NOTDRAFT, isNotDraft);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        TaskManager.get(this);

        boolean draftSnack = getIntent().getBooleanExtra(EXTRA_DRAFT, false);
        boolean undoSnack = getIntent().getBooleanExtra(EXTRA_NOTDRAFT, false);

        Task saveTask = getIntent().getParcelableExtra(EXTRA_SAVETASK);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = TaskListFragment.newInstance(draftSnack, saveTask, undoSnack);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        appShortcuts();
    }

    private void appShortcuts(){
        Intent newIntent = TaskPagerActivity.newIntent(this, null, false);
        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(this, NEW_TASK_SHORTCUT)
                .setShortLabel("New Task")
                .setLongLabel("Add New Task")
                .setIcon(IconCompat.createWithResource(this, R.drawable.add_task))
                .setIntent(newIntent.setAction(Intent.ACTION_VIEW))
                .build();
        ShortcutManagerCompat.addDynamicShortcuts(this, Arrays.asList(shortcut));
    }
}
