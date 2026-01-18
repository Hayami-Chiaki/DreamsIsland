// BodyStateDao.java - 修改为原生SQLite版本
package com.example.dreamisland.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dreamisland.entity.BodyState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BodyStateDao {

    private SQLiteDatabase db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public BodyStateDao(SQLiteDatabase db) {
        this.db = db;
    }

    // 插入记录
    public long insert(BodyState bodyState) {
        ContentValues values = new ContentValues();
        values.put("user_id", bodyState.getUserId());
        values.put("state", bodyState.getState());
        values.put("record_date", bodyState.getRecordDate());

        return db.insert("body_states", null, values);
    }

    // 更新记录
    public int update(BodyState bodyState) {
        ContentValues values = new ContentValues();
        values.put("state", bodyState.getState());
        values.put("record_date", bodyState.getRecordDate());

        return db.update("body_states", values,
                "state_id = ?", new String[]{String.valueOf(bodyState.getStateId())});
    }

    // 删除记录
    public int delete(BodyState bodyState) {
        return db.delete("body_states",
                "state_id = ?", new String[]{String.valueOf(bodyState.getStateId())});
    }

    // 查询用户的所有记录
    public List<BodyState> getBodyStatesByUserId(int userId) {
        List<BodyState> bodyStates = new ArrayList<>();

        Cursor cursor = db.query("body_states",
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                "record_date DESC");

        while (cursor.moveToNext()) {
            BodyState state = cursorToBodyState(cursor);
            bodyStates.add(state);
        }

        cursor.close();
        return bodyStates;
    }

    // 查询用户某一天的记录
    public BodyState getBodyStateByDate(int userId, String date) {
        BodyState bodyState = null;

        Cursor cursor = db.query("body_states",
                null,
                "user_id = ? AND record_date = ?",
                new String[]{String.valueOf(userId), date},
                null, null, null);

        if (cursor.moveToFirst()) {
            bodyState = cursorToBodyState(cursor);
        }

        cursor.close();
        return bodyState;
    }

    // 查询用户某个月份的记录
    public List<BodyState> getBodyStatesByMonth(int userId, String monthPattern) {
        List<BodyState> bodyStates = new ArrayList<>();

        Cursor cursor = db.query("body_states",
                null,
                "user_id = ? AND record_date LIKE ?",
                new String[]{String.valueOf(userId), monthPattern + "%"},
                null, null,
                "record_date");

        while (cursor.moveToNext()) {
            BodyState state = cursorToBodyState(cursor);
            bodyStates.add(state);
        }

        cursor.close();
        return bodyStates;
    }

    // 统计某个月份各状态数量
    public List<StateCount> getStateStatistics(int userId, String monthPattern) {
        List<StateCount> stateCounts = new ArrayList<>();

        String sql = "SELECT state, COUNT(*) as count FROM body_states " +
                "WHERE user_id = ? AND record_date LIKE ? " +
                "GROUP BY state";

        Cursor cursor = db.rawQuery(sql,
                new String[]{String.valueOf(userId), monthPattern + "%"});

        while (cursor.moveToNext()) {
            StateCount stateCount = new StateCount();
            stateCount.state = cursor.getString(cursor.getColumnIndexOrThrow("state"));
            stateCount.count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            stateCounts.add(stateCount);
        }

        cursor.close();
        return stateCounts;
    }

    // 查询指定日期范围内的记录
    public List<StateDatePair> getStatesInDateRange(int userId, String startDate, String endDate) {
        List<StateDatePair> pairs = new ArrayList<>();

        // 由于SQLite的递归CTE在较旧版本可能不支持，我们使用更兼容的方法
        String sql = "SELECT record_date as date, state FROM body_states " +
                "WHERE user_id = ? AND record_date BETWEEN ? AND ? " +
                "ORDER BY record_date";

        Cursor cursor = db.rawQuery(sql,
                new String[]{String.valueOf(userId), startDate, endDate});

        while (cursor.moveToNext()) {
            StateDatePair pair = new StateDatePair();
            pair.date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            pair.state = cursor.getString(cursor.getColumnIndexOrThrow("state"));
            pairs.add(pair);
        }

        cursor.close();
        return pairs;
    }

    // 检查是否已记录今天的状态
    public boolean isAlreadyRecordedToday(int userId) {
        String today = dateFormat.format(Calendar.getInstance().getTime());

        Cursor cursor = db.query("body_states",
                new String[]{"state_id"},
                "user_id = ? AND record_date = ?",
                new String[]{String.valueOf(userId), today},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // 检查连续疲惫天数
    public int checkConsecutiveTiredDays(int userId, int daysToCheck) {
        try {
            Calendar calendar = Calendar.getInstance();
            List<String> recentDates = new ArrayList<>();

            // 获取最近N天的日期
            for (int i = 0; i < daysToCheck; i++) {
                String date = dateFormat.format(calendar.getTime());
                recentDates.add(date);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }

            // 查询这些日期的状态
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < recentDates.size(); i++) {
                placeholders.append("?");
                if (i < recentDates.size() - 1) {
                    placeholders.append(",");
                }
            }

            String sql = "SELECT record_date, state FROM body_states " +
                    "WHERE user_id = ? AND record_date IN (" + placeholders + ") " +
                    "ORDER BY record_date DESC";

            // 构建参数数组
            String[] params = new String[recentDates.size() + 1];
            params[0] = String.valueOf(userId);
            for (int i = 0; i < recentDates.size(); i++) {
                params[i + 1] = recentDates.get(i);
            }

            Cursor cursor = db.rawQuery(sql, params);

            int consecutiveTired = 0;
            while (cursor.moveToNext()) {
                String state = cursor.getString(cursor.getColumnIndexOrThrow("state"));
                if ("疲惫".equals(state)) {
                    consecutiveTired++;
                    if (consecutiveTired >= daysToCheck) {
                        break;
                    }
                } else {
                    consecutiveTired = 0;
                }
            }

            cursor.close();
            return consecutiveTired;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Cursor 转 BodyState 对象
    private BodyState cursorToBodyState(Cursor cursor) {
        BodyState bodyState = new BodyState();
        bodyState.setStateId(cursor.getInt(cursor.getColumnIndexOrThrow("state_id")));
        bodyState.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        bodyState.setState(cursor.getString(cursor.getColumnIndexOrThrow("state")));
        bodyState.setRecordDate(cursor.getString(cursor.getColumnIndexOrThrow("record_date")));
        return bodyState;
    }

    // 内部类
    public static class StateDatePair {
        public String date;
        public String state;
    }

    public static class StateCount {
        public String state;
        public int count;
    }
}