package com.example.dreamisland.ui.profile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sp = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
            AlarmScheduler scheduler = new AlarmScheduler(context, sp);
            scheduler.rescheduleAll();
        }
    }

    static class AlarmScheduler {
        private final Context context;
        private final SharedPreferences sp;
        private final AlarmManager alarmManager;

        AlarmScheduler(Context context, SharedPreferences sp) {
            this.context = context;
            this.sp = sp;
            this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        void rescheduleAll() {
            scheduleType("sleep");
            scheduleType("wake");
        }

        void scheduleType(String type) {
            boolean enabled = sp.getBoolean(type + "_enabled", false);
            if (!enabled) return;
            int hour = sp.getInt(type + "_hour", 7);
            int minute = sp.getInt(type + "_minute", 0);
            scheduleForSelectedDays(type, hour, minute);
        }

        private void scheduleForSelectedDays(String type, int hour, int minute) {
            for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
                boolean checked = sp.getBoolean(type + "_repeat_" + day, false);
                if (checked) {
                    PendingIntent pi = buildPendingIntent(type, day);
                    Calendar cal = nextOccurrenceForDay(day, hour, minute);
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                }
            }
        }

        private PendingIntent buildPendingIntent(String type, int day) {
            Intent i = new Intent(context, AlarmReceiver.class);
            i.putExtra("alarm_type", type);
            i.putExtra("weekday", day);
            int requestCode = ("sleep".equals(type) ? 1000 : 2000) + day;
            return PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        private Calendar nextOccurrenceForDay(int dayOfWeek, int hour, int minute) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            int currentDow = c.get(Calendar.DAY_OF_WEEK);
            int delta = (dayOfWeek - currentDow + 7) % 7;
            if (delta == 0 && c.getTimeInMillis() <= System.currentTimeMillis()) {
                delta = 7;
            }
            c.add(Calendar.DAY_OF_YEAR, delta);
            return c;
        }
    }
}


