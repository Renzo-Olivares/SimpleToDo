package com.renzobiz.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.List;
import java.util.UUID;

import static java.text.DateFormat.FULL;

public class TaskListFragment extends Fragment {
    private static final String EXTRA_POSITION = "com.renzobiz.android.simpletodo.adapterposition";
    private static final String EXTRA_TASKID = "com.renzobiz.android.simpletodo.taskid";
    private static final String ARGS_DRAFT = "has_draft";
    private static final String ARGS_SAVETASK = "save_task";
    private static final String ARGS_NOTDRAFT = "not_draft";

    private RecyclerView mTaskRecycler;
    private FloatingActionButton mFloatingActionButton;
    private TaskAdapter mAdapter;
    private int mAdapterPosition = -1;
    private UUID taskid;


    @Override
    public void onResume() {
        super.onResume();
        updateUI(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_list,container,false);
        setUpToolbar(v);

        boolean hasDraft = getArguments().getBoolean(ARGS_DRAFT, false);
        boolean notDraft = getArguments().getBoolean(ARGS_NOTDRAFT, false);

        if(savedInstanceState != null) {
            mAdapterPosition = savedInstanceState.getInt(EXTRA_POSITION, -1);
            taskid = (UUID) savedInstanceState.getSerializable(EXTRA_TASKID);
        }


        if(hasDraft){

            List<Task> mTasks = TaskManager.get(getActivity()).getTasks();
            Task tempTask = getArguments().getParcelable(ARGS_SAVETASK);
            boolean stopSnack = false;

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
                                updateUI(true);
                                Task saveTask = getArguments().getParcelable(ARGS_SAVETASK);
                                TaskManager.get(getActivity()).addTask(saveTask);
                                mAdapter.restoreItem(saveTask, (mAdapterPosition + 1));
                                updateUI(false);
                            }
                        }).show();
            }
        }

        mTaskRecycler = v.findViewById(R.id.task_recycler);
        mFloatingActionButton = v.findViewById(R.id.add_task);

        mTaskRecycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        SwipeToDeleteCallback swipeHandler = new SwipeToDeleteCallback(getActivity()){
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return super.onMove(recyclerView, viewHolder, viewHolder1);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                super.onSwiped(viewHolder, i);
                //save task into temporary task for restore
                final Task saveTask = TaskManager.get(getActivity()).getTask(taskid);
                final int deletedPosition = viewHolder.getAdapterPosition();

                mAdapter.removeItem(viewHolder.getAdapterPosition());
                TaskManager.get(getActivity()).deleteTask(taskid);
                updateUI(true);

                Snackbar.make(getView(), R.string.undo_snack_label, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo_snack_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TaskManager.get(getActivity()).addTask(saveTask);
                                mAdapter.restoreItem(saveTask, deletedPosition);
                                updateUI(false);
                            }
                        }).show();
            }
        };

        ItemTouchHelper itemHelper = new ItemTouchHelper(swipeHandler);
        itemHelper.attachToRecyclerView(mTaskRecycler);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = TaskPagerActivity.newIntent(getActivity(),null, false);
                startActivity(intent);
                updateUI(true);
            }
        });

        mTaskRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI(false);
        return v;
    }

    private void updateUI(boolean newAdapter){
        TaskManager tm = TaskManager.get(getActivity());
        List<Task> tasks = tm.getTasks();

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
        private Task mTask;

        @Override
        public void onClick(View view) {
            Intent intent = TaskPagerActivity.newIntent(getActivity(), mTask.getTaskId(), true);
            startActivity(intent);
        }

        public TaskHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_task,parent,false));
            mTaskTitle = itemView.findViewById(R.id.task_title);
            mTaskDate = itemView.findViewById(R.id.task_date);
            itemView.setOnClickListener(this);
        }

        public void bind(Task task){
            mTask = task;
            String date = DateFormat.getDateInstance(FULL).format(task.getTaskDeadline());
            mTaskTitle.setText(mTask.getTaskTitle());
            mTaskDate.setText(date);
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
            taskid = task.getTaskId();
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

        public void removeItem(int position) {
            mTasks.remove(position);
            notifyItemRemoved(position);
        }

        public void restoreItem(Task task, int position) {
            mTasks.add(position, task);
            notifyItemInserted(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_POSITION, mAdapterPosition);
        outState.putSerializable(EXTRA_TASKID,taskid);
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
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
}