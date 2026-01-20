package com.example.dreamisland.ui.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("alarm_type");
        
        // 启动闹钟提醒 Activity
        Intent alertIntent = new Intent(context, AlarmAlertActivity.class);
        alertIntent.putExtra("alarm_type", type);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(alertIntent);

        // 为同一类型与星期，顺延一周再次设置
        int weekday = intent.getIntExtra("weekday", -1);
        if (type != null && weekday != -1) {
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    ("sleep".equals(type) ? 1000 : 2000) + weekday,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            SharedPreferences sp = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
            boolean enabled = sp.getBoolean(type + "_enabled", false);
            if (!enabled) return;
            int hour = sp.getInt(type + "_hour", 7);
            int minute = sp.getInt(type + "_minute", 0);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            int currentDow = cal.get(Calendar.DAY_OF_WEEK);
            int delta = (weekday - currentDow + 7) % 7;
            if (delta == 0 && cal.getTimeInMillis() <= System.currentTimeMillis()) {
                delta = 7;
            }
            cal.add(Calendar.DAY_OF_YEAR, delta);
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }
}

