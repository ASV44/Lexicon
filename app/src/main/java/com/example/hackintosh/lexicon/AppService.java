package com.example.hackintosh.lexicon;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by hackintosh on 2/24/17.
 */

public class AppService extends Service {
    private NotificationCompat.Builder mBuilder;
    private CountDownTimer timer;
    private int ID = -1;
    private List<String[]> lexicon;
    private int time;
    private Random random = new Random();

    @Override
    public void onCreate() {
        Log.d("Service","create");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("Service","start");
        getIntentExtras(intent);
        setNotificationTime();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // STOP YOUR TASKS
        Log.d("Service","Stop and Destroy");
        sendBroadcast(time);
        timer.cancel();
        stopSelf();
        //super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("Service", "TASK REMOVED");
        sendBroadcast(time);
        stopSelf();

//        PendingIntent service = PendingIntent.getService(
//                getApplicationContext(),
//                0,
//                new Intent(getApplicationContext(), AppService.class),
//                0);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);



        super.onTaskRemoved(rootIntent);
    }

    public void getIntentExtras(Intent intent) {
        if(intent.getExtras().getSerializable("lexicon") != null) {
            lexicon = ((NotificationLexicon) intent.getExtras().getSerializable("lexicon")).getLexicon();
            time = ((NotificationLexicon) intent.getExtras().getSerializable("lexicon")).getTime();
            if(time <= 0) { time = 10800000 + random.nextInt(10800001); }
            Log.d("Time","" + time);
        }
    }

    public void setNotificationBuilder() {
        Random random = new Random();
        int index = random.nextInt(lexicon.size());
        String message = lexicon.get(index)[0];
        String translation = lexicon.get(index)[1];
        ID++;
        mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Improve your LexIcon")
                .setContentText(message + "\t" + translation)
                .setAutoCancel(true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.icon);
        } else {
            mBuilder.setSmallIcon(R.drawable.lexicon);
        }
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(ID, mBuilder.build());
    }

    public void setNotificationTime() {
        final int tick = 1000;

        timer = new CountDownTimer(time,tick) {
            @Override
            public void onTick(long l) {
                time -= tick;
                //Log.d("Time_tick","" + time);
            }

            @Override
            public void onFinish() {
                time = 10800000 + random.nextInt(10800001);
                Log.d("Time","" + time);
                setNotificationBuilder();
                setNotificationTime();
                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
//                stopSelf();
            }
        }.start();
    }

    private void sendBroadcast (int time){
        Intent intent = new Intent ("TIME"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("time", time);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
