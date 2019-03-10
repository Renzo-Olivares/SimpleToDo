package com.renzobiz.simpletodo.Worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import com.renzobiz.simpletodo.Controller.TaskPagerActivity;
import com.renzobiz.simpletodo.R;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotificationWorker extends Worker {
    public static final String EXTRA_TITLE = "taskTitle";
    public static final String EXTRA_ID = "taskID";
    public static final String EXTRA_DETAILS = "task_details";
    private static final String simpletodo_notification_channel = "Task Reminder";
    private static final String TASK_GROUP = "Reminders" ;
    private UUID taskID;
    private String taskTitle;
    private String taskDetails;


    public NotificationWorker(Context context, WorkerParameters params){
        super(context, params);
        taskID = UUID.fromString(getInputData().getString(EXTRA_ID));
        taskTitle = getInputData().getString(EXTRA_TITLE);
        taskDetails = getInputData().getString(EXTRA_DETAILS);

    }

    @NonNull
    @Override
    public Result doWork() {
        triggerNotification(taskID, taskTitle, taskDetails);
        return Result.success();
    }

    private void triggerNotification(UUID taskID, String taskTitle, String taskDetails) {
        // Create the NotificationChannel on Android O and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_HIGH;

            //build the channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(simpletodo_notification_channel, simpletodo_notification_channel, importance);

            //add description for channel
            String description = "A channel which shows notifications about tasks at simpletodo";
            channel.setDescription(description);

            //set led color
            channel.setLightColor(Color.MAGENTA);

            //Register channel
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //create a notification
        //create an intent to open the taskfragment
        Intent intent = TaskPagerActivity.newIntent(getApplicationContext(), taskID, true);

        //put together the PendingIntent
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);

        //get notification details
        String notificationTitle = taskTitle + ": ";

        NotificationCompat.Builder summaryBuilder = null;
        //build summary notification
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            summaryBuilder =
                    new NotificationCompat.Builder(getApplicationContext(), simpletodo_notification_channel)
                            .setSmallIcon(R.drawable.ic_reminder_small)
                            .setStyle(new NotificationCompat.InboxStyle()
                                    .setSummaryText(TASK_GROUP))
                            .setGroupSummary(true)
                            .setColorized(true)
                            .setColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark))
                            .setGroup(TASK_GROUP)
                            .setAutoCancel(true);
        }else{
            notificationTitle = "Reminder : " + taskTitle;
        }

        //build notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), simpletodo_notification_channel)
                        .setSmallIcon(R.drawable.ic_reminder_small)
                        .setContentTitle(notificationTitle)
                        .setContentText(taskDetails)
                        .setColorized(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(taskDetails))
                        .setColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[0])
                    .setGroup(TASK_GROUP);
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationBuilder.setGroup(TASK_GROUP);
        }else{
            notificationBuilder.setTicker(notificationTitle)
                    .setSmallIcon(R.drawable.ic_reminder_small_kk);
        }

        //trigger notification
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        //give notification unique ID
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            notificationManager.notify(777, summaryBuilder.build());
        }
        notificationManager.notify(NotificationID.getID(), notificationBuilder.build());
    }
}
