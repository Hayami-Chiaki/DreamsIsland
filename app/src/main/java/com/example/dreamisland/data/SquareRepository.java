package com.example.dreamisland.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.model.Dream;
import com.example.dreamisland.model.Post;

import java.util.ArrayList;
import java.util.List;

public class SquareRepository {
    private static SquareRepository instance;
    private final DreamDatabaseHelper dbHelper;

    private SquareRepository(Context context) {
        this.dbHelper = new DreamDatabaseHelper(context.getApplicationContext());
    }

    public static synchronized SquareRepository getInstance(Context context) {
        if (instance == null) instance = new SquareRepository(context);
        return instance;
    }

    public List<Dream> fetchPublicDreams(int limit, int offset) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Dream> list = new ArrayList<>();
        String sql = "SELECT d.dream_id, d.title, d.content, d.nature, d.tags, d.created_at, u.username " +
                "FROM dreams d JOIN users u ON d.user_id = u.user_id " +
                "WHERE d.is_public = 1 ORDER BY d.created_at DESC LIMIT ? OFFSET ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(limit), String.valueOf(offset)});
        while (c.moveToNext()) {
            Dream d = new Dream();
            d.setDreamId(c.getInt(0));
            d.setTitle(c.getString(1));
            String content = c.getString(2);
            d.setPreviewContent(content == null ? "" : content);
            d.setNature(c.getString(3));
            d.setTags(c.getString(4));
            d.setCreatedAt(c.getString(5));
            d.setUsername(c.getString(6));
            list.add(d);
        }
        c.close();
        db.close();
        return list;
    }

    public List<Post> fetchPostsWithReplyCount(int limit, int offset) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Post> list = new ArrayList<>();
        String sql = "SELECT p.post_id, p.title, p.content, p.created_at, u.username, " +
                "(SELECT COUNT(1) FROM replies r WHERE r.post_id = p.post_id) AS reply_count " +
                "FROM posts p JOIN users u ON p.user_id = u.user_id " +
                "ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(limit), String.valueOf(offset)});
        while (c.moveToNext()) {
            Post p = new Post();
            p.postId = c.getInt(0);
            p.title = c.getString(1);
            String content = c.getString(2);
            p.previewContent = content == null ? "" : content;
            p.createdAt = c.getString(3);
            p.username = c.getString(4);
            p.replyCount = c.getInt(5);
            list.add(p);
        }
        c.close();
        db.close();
        return list;
    }

    public void insertPost(int userId, String title, String content, String createdAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("title", title);
        cv.put("content", content);
        cv.put("created_at", createdAt);
        db.insert("posts", null, cv);
        db.close();
    }

    public void insertReply(int postId, int userId, String content, String createdAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("post_id", postId);
        cv.put("user_id", userId);
        cv.put("content", content);
        cv.put("created_at", createdAt);
        db.insert("replies", null, cv);
        db.close();
    }
}


