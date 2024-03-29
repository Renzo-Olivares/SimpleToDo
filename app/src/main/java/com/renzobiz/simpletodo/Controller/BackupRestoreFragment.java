package com.renzobiz.simpletodo.Controller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.renzobiz.simpletodo.Model.Task;
import com.renzobiz.simpletodo.Model.TaskManager;
import com.renzobiz.simpletodo.R;
import com.renzobiz.simpletodo.Worker.NotificationWorker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class BackupRestoreFragment extends DialogFragment {
    private static final String[] STORAGE_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private static final int REQUEST_STORAGE_PERMISSIONS_BACKUP = 0;
    private static final int REQUEST_STORAGE_PERMISSIONS_RESTORE = 1;
    private static final String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/taskBackup.ser";
    private Button mBackupButton;
    private Button mRestoreButton;
    private TextView mTitle;
    public static final String EXTRA_RESTORE = "com.renzobiz.android.simpletodo.restore";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_backup, null);
        mBackupButton = v.findViewById(R.id.backup_button);
        mRestoreButton = v.findViewById(R.id.restore_button);

        //Create custom title
        mTitle = new TextView(getActivity());
        mTitle.setText(R.string.backup_restore_dialog_title);
        mTitle.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            mTitle.setTextAppearance(R.style.TextAppearance_MaterialComponents_Headline6);
        }
        mTitle.setTextSize(20);

        mBackupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasStoragePermission()){
                    backupDB();
                }else{
                    requestPermissions(STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS_BACKUP);
                }
            }
        });

        mRestoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasStoragePermission()){
                    restoreDB();
                }else{
                    requestPermissions(STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS_RESTORE);
                }
            }
        });

        return new AlertDialog.Builder(getActivity(), R.style.backup_dialog_theme)
                .setView(v)
                .setCustomTitle(mTitle)
                .create();
    }

    private boolean hasStoragePermission(){
        int result = ContextCompat.checkSelfPermission(getActivity(), STORAGE_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            case REQUEST_STORAGE_PERMISSIONS_BACKUP:
                if(hasStoragePermission()){
                    backupDB();
                }
                break;
            case REQUEST_STORAGE_PERMISSIONS_RESTORE:
                if(hasStoragePermission()){
                    restoreDB();
                }
        }
    }

    private void backupDB(){
        List<Task> backupTasks = null;
        try {
            backupTasks = TaskManager.get(getActivity()).getAllAsync();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try{
            FileOutputStream backupFile = new FileOutputStream(destinationPath);
            ObjectOutput backupObjects = new ObjectOutputStream(backupFile);
            backupObjects.writeObject(backupTasks);
            backupObjects.close();
            backupFile.close();
            Toast.makeText(getActivity(), "Backup successful. File located in root directory of" +
                    " internal storage as taskBackup.ser", Toast.LENGTH_LONG).show();
        }catch (IOException e){
            Toast.makeText(getActivity(), "Backup unsuccessful, please try again and contact the" +
                    " developer if the problem persists.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void restoreDB() {
        List<Task> restoreTasks;
        try {
            FileInputStream backUp = new FileInputStream(destinationPath);
            ObjectInputStream restoreFile = new ObjectInputStream(backUp);
            restoreTasks = (List<Task>) restoreFile.readObject();
            TaskManager.get(getActivity()).destroyAsync();
            TaskManager.get(getActivity()).updateAllAsync(restoreTasks);
            Toast.makeText(getActivity(), "Restore successful.", Toast.LENGTH_SHORT).show();
            createWork(restoreTasks);
            sendResult(Activity.RESULT_OK);
        } catch (IOException | ClassNotFoundException e) {
            Toast.makeText(getActivity(), "Restore unsuccessful, please make sure your taskBackup.ser file " +
                    "is in the root directory of your internal storage and try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }

    private void createWork(List<Task> restoreTasks) {
        WorkManager.getInstance().cancelAllWork();
        for(int i = 0; i < restoreTasks.size(); i++){
            Task rTask = restoreTasks.get(i);
            if(rTask.isRemindersEnabled() && (rTask.getTaskDeadline().getTime() > new Date().getTime())){
                String TAG_NOTIFICATIONS = rTask.getTaskTitle().toUpperCase().replace(" ", "_");

                Data data = new Data.Builder()
                        .putString(NotificationWorker.EXTRA_TITLE, rTask.getTaskTitle())
                        .putString(NotificationWorker.EXTRA_ID, rTask.getTaskId().toString())
                        .putString(NotificationWorker.EXTRA_DETAILS, rTask.getTaskDetails())
                        .build();

                OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInitialDelay(calculateDelay(rTask.getTaskDeadline()), TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag(TAG_NOTIFICATIONS)
                        .build();

                WorkManager.getInstance().enqueueUniqueWork(TAG_NOTIFICATIONS, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
            }
        }
    }

    private long calculateDelay(Date taskDeadline) {
        long targetMilli = taskDeadline.getTime();
        long initialMilli = new Date().getTime();
        long delay = 25000;

        return (targetMilli - initialMilli) - delay;
    }

    private void sendResult(int resultCode){
        if(getTargetFragment() == null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESTORE, true);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
