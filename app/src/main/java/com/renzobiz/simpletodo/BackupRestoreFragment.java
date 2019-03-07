package com.renzobiz.simpletodo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class BackupRestoreFragment extends DialogFragment {
    private static final String[] STORAGE_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private static final int REQUEST_STORAGE_PERMISSIONS_BACKUP = 0;
    private static final int REQUEST_STORAGE_PERMISSIONS_RESTORE = 1;
    private String currentDBPath;
    private String currentWalPath;
    private String backupWalPath;
    private String backupDBPath;
    private Button mBackupButton;
    private Button mRestoreButton;
    private TextView mTitle;

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
        File sd = Environment.getExternalStorageDirectory();
        FileChannel source;
        FileChannel source_wal;
        FileChannel destination;
        FileChannel destination_wal;

        currentDBPath = getActivity().getDatabasePath(TaskDataBase.DATABASE_NAME).getAbsolutePath();
        currentWalPath = getActivity().getDatabasePath(TaskDataBase.DATABASE_NAME).getAbsolutePath() + "-wal";
        backupDBPath = "/task_database";
        backupWalPath = "/task_database-wal";

        File currentDB = new File(currentDBPath);
        File currentWal = new File(currentWalPath);
        File backupDB = new File(sd, backupDBPath);
        File backupWal = new File(sd, backupWalPath);

        try{
            source = new FileInputStream(currentDB).getChannel();
            source_wal = new FileInputStream(currentWal).getChannel();

            destination = new FileOutputStream(backupDB).getChannel();
            destination_wal = new FileOutputStream(backupWal).getChannel();

            destination.transferFrom(source, 0, source.size());
            destination_wal.transferFrom(source_wal,0,source_wal.size());

            source.close();
            source_wal.close();

            destination.close();
            destination_wal.close();

            Toast.makeText(getActivity(), "Your tasks are now backed up.", Toast.LENGTH_LONG).show();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void restoreDB(){
        File sd = Environment.getExternalStorageDirectory();
        FileChannel source;
        FileChannel source_wal;
        FileChannel destination;
        FileChannel destination_wal;

        currentDBPath = getActivity().getDatabasePath(TaskDataBase.DATABASE_NAME).getAbsolutePath();
        currentWalPath = getActivity().getDatabasePath(TaskDataBase.DATABASE_NAME).getAbsolutePath() + "-wal";
        backupDBPath = "/task_database";
        backupWalPath = "/task_database-wal";

        File currentDB = new File(currentDBPath);
        File currentWal = new File(currentWalPath);
        File backupDB = new File(sd, backupDBPath);
        File backupWal = new File(sd, backupWalPath);

        try{
            source = new FileInputStream(backupDB).getChannel();
            source_wal = new FileInputStream(backupWal).getChannel();

            destination = new FileOutputStream(currentDB).getChannel();
            destination_wal = new FileOutputStream(currentWal).getChannel();

            destination.transferFrom(source, 0, source.size());
            destination_wal.transferFrom(source_wal,0,source_wal.size());

            source.close();
            source_wal.close();

            destination.close();
            destination_wal.close();

            Toast.makeText(getActivity(), "Your tasks are now restored.", Toast.LENGTH_LONG).show();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
