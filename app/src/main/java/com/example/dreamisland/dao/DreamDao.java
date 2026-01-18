// DreamDao.java
package com.example.dreamisland.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dreamisland.model.Dream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DreamDao {

    private SQLiteDatabase db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DreamDao(SQLiteDatabase db) {
        this.db = db;
    }

    // 查询用户的梦境记录
    public List<Dream> getDreamsByUserId(int userId) {
        List<Dream> dreams = new ArrayList<>();

        Cursor cursor = db.query("dreams",
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                "created_at DESC");

        while (cursor.moveToNext()) {
            Dream dream = cursorToDream(cursor);
            dreams.add(dream);
        }

        cursor.close();
        return dreams;
    }

    // 查询用户指定日期范围内的梦境
    public List<Dream> getDreamsByDateRange(int userId, String startDate, String endDate) {
        List<Dream> dreams = new ArrayList<>();

        String sql = "SELECT * FROM dreams WHERE user_id = ? " +
                "AND date(created_at) BETWEEN ? AND ? " +
                "ORDER BY created_at DESC";

        Cursor cursor = db.rawQuery(sql,
                new String[]{String.valueOf(userId), startDate, endDate});

        while (cursor.moveToNext()) {
            Dream dream = cursorToDream(cursor);
            dreams.add(dream);
        }

        cursor.close();
        return dreams;
    }

    // 查询用户最近一年的梦境数据（用于热力图）
    public Map<String, String> getDreamsForHeatMap(int userId) {
        Map<String, String> dreamMap = new HashMap<>();

        try {
            // 计算一年前的日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -1);
            String oneYearAgo = dateFormat.format(calendar.getTime());

            // 获取今天的日期
            String today = dateFormat.format(new Date());

            // 查询一年内的梦境数据
            String sql = "SELECT date(created_at) as dream_date, nature " +
                    "FROM dreams " +
                    "WHERE user_id = ? " +
                    "AND date(created_at) BETWEEN ? AND ? " +
                    "ORDER BY dream_date";

            Cursor cursor = db.rawQuery(sql,
                    new String[]{String.valueOf(userId), oneYearAgo, today});

            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("dream_date"));
                String nature = cursor.getString(cursor.getColumnIndexOrThrow("nature"));
                dreamMap.put(date, nature);
            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dreamMap;
    }

    // 统计一周内的梦境次数
    public int getDreamCountInWeek(int userId) {
        int count = 0;

        try {
            // 计算一周前的日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            String oneWeekAgo = dateFormat.format(calendar.getTime());

            // 获取今天的日期
            String today = dateFormat.format(new Date());

            String sql = "SELECT COUNT(*) as count FROM dreams " +
                    "WHERE user_id = ? " +
                    "AND date(created_at) BETWEEN ? AND ?";

            Cursor cursor = db.rawQuery(sql,
                    new String[]{String.valueOf(userId), oneWeekAgo, today});

            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    // Cursor 转 Dream 对象
    private Dream cursorToDream(Cursor cursor) {
        Dream dream = new Dream();
        dream.setDreamId(cursor.getInt(cursor.getColumnIndexOrThrow("dream_id")));
        dream.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        dream.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        dream.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        dream.setNature(cursor.getString(cursor.getColumnIndexOrThrow("nature")));
        dream.setTags(cursor.getString(cursor.getColumnIndexOrThrow("tags")));
        dream.setPublic(cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1);
        dream.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return dream;
    }
}