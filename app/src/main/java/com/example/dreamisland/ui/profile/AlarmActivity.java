package com.example.dreamisland.ui.profile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.dreamisland.R;
import com.example.dreamisland.databinding.ActivityAlarmBinding;

import java.util.Calendar;

public class AlarmActivity extends AppCompatActivity {
    private ActivityAlarmBinding binding;
    private SharedPreferences sharedPreferences;
    private AlarmManager alarmManager;
    private String currentType = "sleep"; // sleep 或 wake

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topBar);

        // 设置ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.alarm_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        loadAlarmSettings();
        setupViews();
    }

    private void setupViews() {
        // 类型切换
        binding.chipSleep.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentType = "sleep";
                binding.chipWake.setChecked(false);
                loadAlarmSettings();
                updateChipTextColors();
            }
        });
        binding.chipWake.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentType = "wake";
                binding.chipSleep.setChecked(false);
                loadAlarmSettings();
                updateChipTextColors();
            }
        });

        // 启用闹钟开关
        binding.enableAlarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveAlarmEnabled(isChecked);
            if (isChecked) {
                setAlarm();
                Toast.makeText(this, "闹钟已启用", Toast.LENGTH_SHORT).show();
            } else {
                cancelAlarm();
                Toast.makeText(this, "闹钟已禁用", Toast.LENGTH_SHORT).show();
            }
        });

        // 时间选择器
        binding.timePicker.setIs24HourView(false);
        binding.timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            saveAlarmTime(hourOfDay, minute);
            if (binding.enableAlarmSwitch.isChecked()) {
                setAlarm();
            }
        });

        // 重复设置
        setupRepeatCheckboxes();
        updateChipTextColors();
    }

    private void setupRepeatCheckboxes() {
        CompoundButton.OnCheckedChangeListener repeatListener = (buttonView, isChecked) -> {
            saveRepeatDays();
            if (binding.enableAlarmSwitch.isChecked()) {
                setAlarm();
            }
        };

        binding.mondayCheckBox.setOnCheckedChangeListener(repeatListener);
        binding.tuesdayCheckBox.setOnCheckedChangeListener(repeatListener);
        binding.wednesdayCheckBox.setOnCheckedChangeListener(repeatListener);
        binding.thursdayCheckBox.setOnCheckedChangeListener(repeatListener);
        binding.fridayCheckBox.setOnCheckedChangeListener(repeatListener);
        binding.saturdayCheckBox.setOnCheckedChangeListener(repeatListener);
        binding.sundayCheckBox.setOnCheckedChangeListener(repeatListener);
    }

    private void loadAlarmSettings() {
        boolean enabled = sharedPreferences.getBoolean(currentType + "_enabled", false);
        binding.enableAlarmSwitch.setChecked(enabled);

        int hour = sharedPreferences.getInt(currentType + "_hour", 7);
        int minute = sharedPreferences.getInt(currentType + "_minute", 0);
        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(minute);

        binding.mondayCheckBox.setChecked(sharedPreferences.getBoolean(currentType + "_repeat_" + java.util.Calendar.MONDAY, false));
        binding.tuesdayCheckBox.setChecked(sharedPreferences.getBoolean(currentType + "_repeat_" + java.util.Calendar.TUESDAY, false));
        binding.wednesdayCheckBox.setChecked(sharedPreferences.getBoolean(currentType + "_repeat_" + java.util.Calendar.WEDNESDAY, false));
        binding.thursdayCheckBox.setChecked(sharedPreferences.getBoolean(currentType + "_repeat_" + java.util.Calendar.THURSDAY, false));
        binding.fridayCheckBox.setChecked(sharedPreferences.getBoolean(currentType + "_repeat_" + java.util.Calendar.FRIDAY, false));
        binding.saturdayCheckBox.setChecked(sharedPreferences.getBoolean(currentType + "_repeat_" + java.util.Calendar.SATURDAY, false));
        binding.sundayCheckBox.setChecked(sharedPreferences.getBoolean(currentType + "_repeat_" + java.util.Calendar.SUNDAY, false));
    }

    private void saveAlarmEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(currentType + "_enabled", enabled).apply();
    }

    private void saveAlarmTime(int hour, int minute) {
        sharedPreferences.edit()
                .putInt(currentType + "_hour", hour)
                .putInt(currentType + "_minute", minute)
                .apply();
    }

    private void saveRepeatDays() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(currentType + "_repeat_" + java.util.Calendar.MONDAY, binding.mondayCheckBox.isChecked());
        editor.putBoolean(currentType + "_repeat_" + java.util.Calendar.TUESDAY, binding.tuesdayCheckBox.isChecked());
        editor.putBoolean(currentType + "_repeat_" + java.util.Calendar.WEDNESDAY, binding.wednesdayCheckBox.isChecked());
        editor.putBoolean(currentType + "_repeat_" + java.util.Calendar.THURSDAY, binding.thursdayCheckBox.isChecked());
        editor.putBoolean(currentType + "_repeat_" + java.util.Calendar.FRIDAY, binding.fridayCheckBox.isChecked());
        editor.putBoolean(currentType + "_repeat_" + java.util.Calendar.SATURDAY, binding.saturdayCheckBox.isChecked());
        editor.putBoolean(currentType + "_repeat_" + java.util.Calendar.SUNDAY, binding.sundayCheckBox.isChecked());
        editor.apply();
    }

    private void updateChipTextColors() {
        int white = ContextCompat.getColor(this, R.color.white);
        int black = ContextCompat.getColor(this, R.color.black);
        binding.chipSleep.setTextColor(binding.chipSleep.isChecked() ? white : black);
        binding.chipWake.setTextColor(binding.chipWake.isChecked() ? white : black);
    }

    private void setAlarm() {
        int hour = binding.timePicker.getHour();
        int minute = binding.timePicker.getMinute();
        saveAlarmTime(hour, minute);

        // 先取消本类型的所有预约
        cancelAlarm();

        for (int day = java.util.Calendar.SUNDAY; day <= java.util.Calendar.SATURDAY; day++) {
            boolean checked = sharedPreferences.getBoolean(currentType + "_repeat_" + day, false);
            if (!checked) continue;
            Intent i = new Intent(this, AlarmReceiver.class);
            i.putExtra("alarm_type", currentType);
            i.putExtra("weekday", day);
            int requestCode = ("sleep".equals(currentType) ? 1000 : 2000) + day;
            PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
            calendar.set(java.util.Calendar.MINUTE, minute);
            calendar.set(java.util.Calendar.SECOND, 0);

            int currentDow = calendar.get(java.util.Calendar.DAY_OF_WEEK);
            int delta = (day - currentDow + 7) % 7;
            if (delta == 0 && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                delta = 7;
            }
            calendar.add(java.util.Calendar.DAY_OF_YEAR, delta);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }

    private void cancelAlarm() {
        for (int day = java.util.Calendar.SUNDAY; day <= java.util.Calendar.SATURDAY; day++) {
            Intent i = new Intent(this, AlarmReceiver.class);
            i.putExtra("alarm_type", currentType);
            i.putExtra("weekday", day);
            int requestCode = ("sleep".equals(currentType) ? 1000 : 2000) + day;
            PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pi);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注意：这里不取消pendingIntent，因为我们需要保持闹钟运行
    }
}


