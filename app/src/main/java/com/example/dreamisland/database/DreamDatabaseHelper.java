package com.example.dreamisland.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DreamDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DreamsIsland.db";
    private static final int DATABASE_VERSION = 2; // 升级版本号

    // 构造函数
    public DreamDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 启用外键约束
        db.execSQL("PRAGMA foreign_keys = ON;");

        // 1. 用户表（users）
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "avatar BLOB," +
                "email TEXT," +
                "phone TEXT," +
                "gender TEXT," +
                "birthday TEXT," +
                "bio TEXT" +
                ");");

        // 2. 梦境记录表（dreams）
        db.execSQL("CREATE TABLE IF NOT EXISTS dreams (" +
                "dream_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "content TEXT NOT NULL," +
                "nature TEXT NOT NULL CHECK (nature IN ('好梦', '噩梦', '其他'))," +
                "tags TEXT," +
                "is_public INTEGER DEFAULT 0 CHECK (is_public IN (0, 1))," +
                "created_at TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE" +
                ");");

        // 3. 身体状态表（body_states）
        db.execSQL("CREATE TABLE IF NOT EXISTS body_states (" +
                "state_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "state TEXT NOT NULL CHECK (state IN ('疲惫', '精神', '一般'))," +
                "record_date TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE," +
                "UNIQUE (user_id, record_date)" +
                ");");

        // 4. 匹配记录表（matches）
        db.execSQL("CREATE TABLE IF NOT EXISTS matches (" +
                "match_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user1_id INTEGER NOT NULL," +
                "user2_id INTEGER NOT NULL," +
                "start_time TEXT NOT NULL," +
                "end_time TEXT," +
                "FOREIGN KEY (user1_id) REFERENCES users (user_id) ON DELETE CASCADE," +
                "FOREIGN KEY (user2_id) REFERENCES users (user_id) ON DELETE CASCADE," +
                "CHECK (user1_id != user2_id)" +
                ");");

        // 5. 讨论区帖子表（posts）
        db.execSQL("CREATE TABLE IF NOT EXISTS posts (" +
                "post_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "content TEXT NOT NULL," +
                "created_at TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE" +
                ");");

        // 6. 回复表（replies）
        db.execSQL("CREATE TABLE IF NOT EXISTS replies (" +
                "reply_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "post_id INTEGER NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "content TEXT NOT NULL," +
                "created_at TEXT NOT NULL," +
                "FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE," +
                "FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE" +
                ");");

        // 7. 索引（移除了消息表相关索引）
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_dreams_user_created ON dreams (user_id, created_at DESC);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_dreams_public_created ON dreams (is_public, created_at DESC);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_body_states_user_date ON body_states (user_id, record_date DESC);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_posts_created ON posts (created_at DESC);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_replies_post_created ON replies (post_id, created_at ASC);");

        // 8. 初始化测试数据（移除了消息表相关数据）
        db.execSQL("INSERT OR IGNORE INTO users (username, password) " +
                "VALUES ('test_user', '123456');");

        db.execSQL("INSERT OR IGNORE INTO dreams (user_id, title, content, nature, tags, is_public, created_at) " +
                "VALUES (1, '第一次测试梦境', '这是一条测试用的梦境内容，用于验证数据库功能', '好梦', '[\"测试\",\"好梦\"]', 1, '2024-01-01 10:00');");

        db.execSQL("INSERT OR IGNORE INTO body_states (user_id, state, record_date) " +
                "VALUES (1, '精神', '2024-01-01');");

        db.execSQL("INSERT OR IGNORE INTO posts (user_id, title, content, created_at) " +
                "VALUES (1, '为什么总是做噩梦？', '最近经常做噩梦，有没有人知道原因呀？', '2024-01-01 11:00');");

        db.execSQL("INSERT OR IGNORE INTO replies (post_id, user_id, content, created_at) " +
                "VALUES (1, 1, '自己先顶一下，期待大家的回复～', '2024-01-01 11:01');");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 添加用户信息扩展字段
            try {
                db.execSQL("ALTER TABLE users ADD COLUMN email TEXT");
                db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
                db.execSQL("ALTER TABLE users ADD COLUMN gender TEXT");
                db.execSQL("ALTER TABLE users ADD COLUMN birthday TEXT");
                db.execSQL("ALTER TABLE users ADD COLUMN bio TEXT");
                Log.d("DreamDatabaseHelper", "Database upgraded to version 2: Added user profile fields");
            } catch (Exception e) {
                Log.e("DreamDatabaseHelper", "Error upgrading database", e);
            }
        }
    }

    // 每次打开数据库时确保外键生效（必须保留）
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }
}