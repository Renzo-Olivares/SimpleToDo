package com.renzobiz.simpletodo.Controller;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.renzobiz.simpletodo.Controller.Helpers.ListSeperatorDecoration;
import com.renzobiz.simpletodo.Controller.Helpers.SwipeToDeleteCallback;
import com.renzobiz.simpletodo.Model.Task;
import com.renzobiz.simpletodo.Model.TaskManager;
import com.renzobiz.simpletodo.R;
import com.renzobiz.simpletodo.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.renzobiz.simpletodo.Controller.BackupRestoreFragment.EXTRA_RESTORE;
import static java.text.DateFormat.SHORT;

public class TaskListFragment extends Fragment {
    private static final String EXTRA_POSITION = "com.renzobiz.android.simpletodo.adapterposition";
    private static final String ARGS_DRAFT = "has_draft";
    private static final String ARGS_SAVETASK = "save_task";
    private static final String ARGS_NOTDRAFT = "not_draft";
    private static final String DIALOG_BACKUP_RESTORE = "DialogBackupRestore";
    private static final int REQUEST_RESTORE = 0;

    private RecyclerView mTaskRecycler;
    private TextView mRecyclerPlaceHolder;
    private FloatingActionButton mFloatingActionButton;
    private TaskAdapter mAdapter;
    private static boolean stopSnack;
    private static int mdeletedPosition;
    private int mAdapterPosition = -1;

    private LinearLayoutManager linearLay;


    @Override
    public void onResume() {
        super.onResume();
        updateUI(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initNightMode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_list,container,false);
        setUpToolbar(v);

        boolean hasDraft = getArguments().getBoolean(ARGS_DRAFT, false);
        final boolean notDraft = getArguments().getBoolean(ARGS_NOTDRAFT, false);

        if(savedInstanceState != null) {
            mAdapterPosition = savedInstanceState.getInt(EXTRA_POSITION, -1);
        }


        if(hasDraft){
            List<Task> mTasks = null;
            final Task saveTask = getArguments().getParcelable(ARGS_SAVETASK);
            try {
                mTasks = TaskManager.get(getActivity()).getAllAsync();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Task tempTask = getArguments().getParcelable(ARGS_SAVETASK);

            for(int i = 0; i < mTasks.size(); i++){
                if(mTasks.get(i).getTaskId().equals(tempTask.getTaskId())){
                    //if task is found then prevent snackbar
                    stopSnack =  true;
                }
            }

            int snack_label;
            int snack_action;

            if(notDraft){
                snack_label = R.string.undo_snack_label;
                snack_action = R.string.undo_snack_action;
            }else{
                snack_label = R.string.draft_snack_label;
                snack_action = R.string.draft_snack_action;
            }

            if(!stopSnack){
                Snackbar.make(container, snack_label, Snackbar.LENGTH_LONG)
                        .setAction(snack_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(notDraft){
                                    mAdapter.restoreItem(saveTask, (mdeletedPosition));
                                }else{
                                    mAdapter.restoreItem(saveTask, (mAdapterPosition + 1));
                                }
                                updateUI(false);
                            }
                        }).addCallback(new Snackbar.Callback(){
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        //prevent snackbar from appearing after dissapears
                        stopSnack =  true;
                        WorkManager.getInstance().cancelAllWorkByTag(saveTask.getTaskTitle().toUpperCase().replace(" ", "_"));
                    }
                }).show();
            }
        }

        mTaskRecycler = v.findViewById(R.id.task_recycler);
        mFloatingActionButton = v.findViewById(R.id.add_task);
        mRecyclerPlaceHolder = v.findViewById(R.id.recycler_placeholder);

        SwipeToDeleteCallback swipeHandler = new SwipeToDeleteCallback(getActivity()){
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return super.onMove(recyclerView, viewHolder, viewHolder1);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                super.onSwiped(viewHolder, i);
                //save task into temporary task for restore
                final int deletedPosition = viewHolder.getAdapterPosition();
                final Task saveTask = mAdapter.removeItem(viewHolder.getAdapterPosition());

                updateUI(false);

                Snackbar.make(getView(), R.string.undo_snack_label, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo_snack_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAdapter.restoreItem(saveTask, deletedPosition);
                                if(mAdapter.getItemCount() == 0){
                                    mRecyclerPlaceHolder.setVisibility(View.VISIBLE);
                                }else{
                                    mRecyclerPlaceHolder.setVisibility(View.GONE);
                                }
                            }
                        }).addCallback(new Snackbar.Callback(){
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        WorkManager.getInstance().cancelAllWorkByTag(saveTask.getTaskTitle().toUpperCase().replace(" ", "_"));
                    }
                }).show();
            }
        };

        ItemTouchHelper itemHelper = new ItemTouchHelper(swipeHandler);
        itemHelper.attachToRecyclerView(mTaskRecycler);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSnack = false;
                Intent intent = TaskPagerActivity.newIntent(getActivity(),null, false);
                startActivity(intent);
                updateUI(true);
            }
        });

        linearLay = new LinearLayoutManager(getActivity());
        mTaskRecycler.setLayoutManager(linearLay);
        mTaskRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        updateUI(false);

        mTaskRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean notAllVisible = linearLay.findLastCompletelyVisibleItemPosition() < mAdapter.getItemCount() - 1;
                if (notAllVisible) {
                    mTaskRecycler.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                }
            }
        });

        RecyclerView.ItemDecoration listSeperator = new ListSeperatorDecoration(mTaskRecycler.getContext(), DividerItemDecoration.VERTICAL);
        mTaskRecycler.addItemDecoration(listSeperator);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_task_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dark_mode:
                int currentNightMode = getCurrentNightMode();
                alternateNightMode(currentNightMode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int getCurrentNightMode() {
        return getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
    }

    private void alternateNightMode(int currentNightMode) {
        int newNightMode;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
        }

        SharedPreferences.setPrefDarkMode(getActivity(),newNightMode);
        getActivity().recreate();
    }

    private void initNightMode() {
        int nightMode = SharedPreferences.getStoredPrefDarkMode(getActivity());
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private void updateUI(boolean newAdapter){
        TaskManager tm = TaskManager.get(getActivity());
        List<Task> tasks = null;
        try {
            tasks = tm.getAllAsync();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mTaskRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if(tasks.size() == 0){
            mRecyclerPlaceHolder.setVisibility(View.VISIBLE);
        }else{
            mRecyclerPlaceHolder.setVisibility(View.GONE);
        }

        if(mAdapter == null | newAdapter){
            mAdapter =  new TaskAdapter(tasks);
            mTaskRecycler.setAdapter(mAdapter);
        }else {
            if (mAdapterPosition < 0) {
                mAdapter.notifyDataSetChanged();
            }else{
                mAdapter.notifyItemChanged(mAdapterPosition);
                mAdapterPosition = -1;
            }
            mAdapter.setTasks(tasks);
        }
    }

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTaskTitle;
        private TextView mTaskDate;
        private TextView mTaskTime;
        private Task mTask;

        @Override
        public void onClick(View view) {
            mdeletedPosition = getAdapterPosition();
            stopSnack = false;
            Intent intent = TaskPagerActivity.newIntent(getActivity(), mTask.getTaskId(), true);
            startActivity(intent);
        }

        public TaskHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_task,parent,false));
            mTaskTitle = itemView.findViewById(R.id.task_title);
            mTaskDate = itemView.findViewById(R.id.task_date);
            mTaskTime = itemView.findViewById(R.id.task_time);
            itemView.setOnClickListener(this);
        }

        public void bind(Task task){
            mTask = task;
            String date = DateFormat.getDateInstance(SHORT).format(task.getTaskDeadline());
            String time = DateFormat.getTimeInstance(SHORT).format(task.getTaskDeadline());
            mTaskTitle.setText(mTask.getTaskTitle());
            mTaskDate.setText(date);
            mTaskTime.setText(time);
            mTaskDate.setVisibility(mTask.isRemindersEnabled()? View.VISIBLE:View.INVISIBLE);
            mTaskTime.setVisibility(mTask.isRemindersEnabled()? View.VISIBLE:View.INVISIBLE);
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
            mAdapterPosition = position;
            taskHolder.bind(task);
        }

        public TaskAdapter(List<Task> tasks){
            mTasks = tasks;
        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }

        public void setTasks(List<Task> tasks){
            mTasks = tasks;
        }

        public Task removeItem(int position) {
            Task restoreTask = null;
            try {
                restoreTask = TaskManager.get(getActivity()).getTaskAsync(mTasks.get(position).getTaskId());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TaskManager.get(getActivity()).deleteAsync(mTasks.get(position));
            mTasks.remove(position);
            notifyItemRemoved(position);
            return restoreTask;
        }

        public void restoreItem(Task task, int position) {
            mTasks.add(position, task);
            TaskManager.get(getActivity()).updateAllAsync(mTasks);
            notifyItemInserted(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_POSITION, mAdapterPosition);
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager manager = getFragmentManager();
                    BackupRestoreFragment brDialog = new BackupRestoreFragment();
                    brDialog.setTargetFragment(TaskListFragment.this, REQUEST_RESTORE);
                    brDialog.show(manager, DIALOG_BACKUP_RESTORE);
                }
            });
        }
    }

    public static TaskListFragment newInstance(boolean hasDraft, Task saveTask, boolean notDraft){
        Bundle args = new Bundle();
        args.putSerializable(ARGS_DRAFT, hasDraft);
        args.putParcelable(ARGS_SAVETASK, saveTask);
        args.putBoolean(ARGS_NOTDRAFT, notDraft);
        TaskListFragment fragment = new TaskListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        switch(requestCode){
            case REQUEST_RESTORE:
                if(data.getBooleanExtra(EXTRA_RESTORE, false)) updateUI(true);
                break;
            default:
                super.onActivityResult(requestCode,resultCode,data);
        }
    }
}