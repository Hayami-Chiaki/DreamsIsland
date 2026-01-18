package com.example.dreamisland.ui.myDreams;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.model.Dream;

import java.util.ArrayList;
import java.util.List;






public class MyDreamsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Dream>> dreamsList;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<MatchInfo> currentMatch;
    private final MutableLiveData<Boolean> isMatching;
    private DreamDatabaseHelper databaseHelper;

    public MyDreamsViewModel(@NonNull Application application) {
        super(application);
        dreamsList = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        currentMatch = new MutableLiveData<>();
        isMatching = new MutableLiveData<>(false);
        databaseHelper = new DreamDatabaseHelper(application);

    }

    // 获取梦境列表的LiveData
    public LiveData<List<Dream>> getDreamsList() {
        return dreamsList;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<MatchInfo> getCurrentMatch() {
        return currentMatch;
    }

    public LiveData<Boolean> isMatching() {
        return isMatching;
    }

    /**
     * 匹配信息内部类
     */
    public static class MatchInfo {
        private int matchId;
        private int matchedUserId;
        private String matchedUsername;
        private List<Dream> matchedUserDreams;

        public MatchInfo(int matchId, int matchedUserId, String matchedUsername, List<Dream> matchedUserDreams) {
            this.matchId = matchId;
            this.matchedUserId = matchedUserId;
            this.matchedUsername = matchedUsername;
            this.matchedUserDreams = matchedUserDreams;
        }

        public int getMatchId() {
            return matchId;
        }

        public int getMatchedUserId() {
            return matchedUserId;
        }

        public String getMatchedUsername() {
            return matchedUsername;
        }

        public List<Dream> getMatchedUserDreams() {
            return matchedUserDreams;
        }
    }

    // 加载用户的梦境列表（默认加载ID为1的用户）
    public void loadUserDreams(int userId) {
        isLoading.postValue(true);
        new Thread(() -> {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = databaseHelper.getReadableDatabase();
                String query = "SELECT dream_id, user_id, title, content, nature, tags, is_public, is_favorite, created_at " +
                        "FROM dreams WHERE user_id = ? ORDER BY is_favorite DESC, created_at DESC";

                cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

                List<Dream> dreams = new ArrayList<>();
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        Dream dream = new Dream(
                                cursor.getInt(cursor.getColumnIndexOrThrow("dream_id")),
                                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                                cursor.getString(cursor.getColumnIndexOrThrow("content")),
                                cursor.getString(cursor.getColumnIndexOrThrow("nature")),
                                cursor.getString(cursor.getColumnIndexOrThrow("tags")),
                                cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1,
                                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                        );
                        dreams.add(dream);
                    } while (cursor.moveToNext());
                }
                dreamsList.postValue(dreams);
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "加载梦境失败", e);
                errorMessage.postValue("加载梦境失败：" + e.getMessage());
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
                isLoading.postValue(false);
            }
        }).start();
    }

    // 添加新梦境
    public void addDream(Dream dream) {
        isLoading.setValue(true);
        new Thread(() -> {
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase();
                String query = "INSERT INTO dreams (user_id, title, content, nature, tags, is_public, is_favorite, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                db.execSQL(query, new Object[]{
                        dream.getUserId(),
                        dream.getTitle(),
                        dream.getContent(),
                        dream.getNature(),
                        dream.getTags(),
                        dream.isPublic() ? 1 : 0,
                        dream.isFavorite() ? 1 : 0,
                        dream.getCreatedAt()
                });
                // 重新加载列表
                loadUserDreams(dream.getUserId());
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "添加梦境失败", e);
                errorMessage.postValue("添加梦境失败：" + e.getMessage());
            } finally {
                if (db != null) db.close();
                isLoading.postValue(false);
            }
        }).start();
    }

    // 删除梦境
    public void deleteDream(int dreamId, int userId) {
        isLoading.setValue(true);
        new Thread(() -> {
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase();
                String query = "DELETE FROM dreams WHERE dream_id = ?";
                db.execSQL(query, new Object[]{dreamId});
                // 重新加载列表
                loadUserDreams(userId);
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "删除梦境失败", e);
                errorMessage.postValue("删除梦境失败：" + e.getMessage());
            } finally {
                if (db != null) db.close();
                isLoading.postValue(false);
            }
        }).start();
    }

    // 切换收藏状态
    public void toggleFavorite(int dreamId, int userId, boolean currentFavorite) {
        isLoading.setValue(true);
        new Thread(() -> {
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase();
                String query = "UPDATE dreams SET is_favorite = ? WHERE dream_id = ?";
                db.execSQL(query, new Object[]{currentFavorite ? 0 : 1, dreamId});
                // 重新加载列表
                loadUserDreams(userId);
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "切换收藏状态失败", e);
                errorMessage.postValue("切换收藏状态失败：" + e.getMessage());
            } finally {
                if (db != null) db.close();
                isLoading.postValue(false);
            }
        }).start();
    }

    // 更新梦境
    public void updateDream(Dream dream) {
        isLoading.setValue(true);
        new Thread(() -> {
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase();
                String query = "UPDATE dreams SET title = ?, content = ?, nature = ?, tags = ?, is_public = ?, is_favorite = ? WHERE dream_id = ?";
                db.execSQL(query, new Object[]{
                        dream.getTitle(),
                        dream.getContent(),
                        dream.getNature(),
                        dream.getTags(),
                        dream.isPublic() ? 1 : 0,
                        dream.isFavorite() ? 1 : 0,
                        dream.getDreamId()
                });
                // 重新加载列表
                loadUserDreams(dream.getUserId());
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "更新梦境失败", e);
                errorMessage.postValue("更新梦境失败：" + e.getMessage());
            } finally {
                if (db != null) db.close();
                isLoading.postValue(false);
            }
        }).start();
    }

    // 开始匹配 - 仅用于UI状态管理，实际匹配逻辑在confirmMatching中
    public void startMatching(int currentUserId) {
        isMatching.setValue(true);
        // 实际的匹配逻辑现在在Fragment中处理，然后调用confirmMatching
        // 这里只需要设置匹配状态
    }

    // 确认匹配
    public void confirmMatching(int currentUserId, String matchedUsername) {
        new Thread(() -> {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                // 使用可写数据库，因为我们可能需要插入用户记录
                db = databaseHelper.getWritableDatabase();

                // 查找指定用户名的用户ID
                String query = "SELECT user_id FROM users WHERE username = ?";
                cursor = db.rawQuery(query, new String[]{matchedUsername});

                int matchedUserId;
                boolean userExists = (cursor != null && cursor.moveToFirst());

                if (userExists) {
                    // 用户存在，获取用户ID
                    matchedUserId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                } else {
                    // 用户不存在，创建一个临时用户
                    matchedUserId = createTempUser(db, matchedUsername);
                }

                // 创建匹配记录
                String startTime = java.text.SimpleDateFormat.getInstance().format(new java.util.Date());
                String insertQuery = "INSERT INTO matches (user1_id, user2_id, start_time) VALUES (?, ?, ?)";
                db.execSQL(insertQuery, new Object[]{currentUserId, matchedUserId, startTime});

                // 获取刚创建的匹配ID
                Cursor matchCursor = db.rawQuery("SELECT last_insert_rowid()", null);
                int matchId = -1;
                if (matchCursor != null && matchCursor.moveToFirst()) {
                    matchId = matchCursor.getInt(0);
                    matchCursor.close();
                }

                // 获取匹配用户的梦境列表
                List<Dream> matchedUserDreams = getDreamsByUserId(matchedUserId, db);

                // 创建匹配信息对象
                MatchInfo matchInfo = new MatchInfo(matchId, matchedUserId, matchedUsername, matchedUserDreams);
                currentMatch.postValue(matchInfo);
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "匹配失败", e);
                errorMessage.postValue("匹配失败：" + e.getMessage());
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
                isMatching.postValue(false);
            }
        }).start();
    }

    /**
     * 创建临时用户
     */
    private int createTempUser(SQLiteDatabase db, String username) {
        try {
            // 插入临时用户记录
            String insertQuery = "INSERT INTO users (username, created_at) VALUES (?, ?)";
            String currentTime = java.text.SimpleDateFormat.getInstance().format(new java.util.Date());
            db.execSQL(insertQuery, new Object[]{username, currentTime});

            // 获取刚创建的用户ID
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            if (cursor != null && cursor.moveToFirst()) {
                int userId = cursor.getInt(0);
                cursor.close();
                return userId;
            }
        } catch (Exception e) {
            Log.e("MyDreamsViewModel", "创建临时用户失败", e);
        }

        // 如果创建失败，返回默认ID
        return 2; // 默认用户ID
    }

    // 结束匹配
    public void endMatching(int matchId) {
        isLoading.setValue(true);
        new Thread(() -> {
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase();

                // 更新匹配记录，设置结束时间
                String endTime = java.text.SimpleDateFormat.getInstance().format(new java.util.Date());
                String query = "UPDATE matches SET end_time = ? WHERE match_id = ?";
                db.execSQL(query, new Object[]{endTime, matchId});

                // 清除当前匹配信息
                currentMatch.postValue(null);
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "结束匹配失败", e);
                errorMessage.postValue("结束匹配失败：" + e.getMessage());
            } finally {
                if (db != null) db.close();
                isLoading.postValue(false);
            }
        }).start();
    }

    // 发送聊天消息
    public void sendMessage(int matchId, int senderId, int receiverId, String content) {
        new Thread(() -> {
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase();

                // 插入聊天消息
                String createdAt = java.text.SimpleDateFormat.getInstance().format(new java.util.Date());
                String query = "INSERT INTO chat_messages (match_id, sender_id, receiver_id, content, created_at) VALUES (?, ?, ?, ?, ?)";
                db.execSQL(query, new Object[]{matchId, senderId, receiverId, content, createdAt});
            } catch (Exception e) {
                Log.e("MyDreamsViewModel", "发送消息失败", e);
                errorMessage.postValue("发送消息失败：" + e.getMessage());
            } finally {
                if (db != null) db.close();
            }
        }).start();
    }

    // 根据用户ID获取梦境列表（私有方法，供内部使用）
    private List<Dream> getDreamsByUserId(int userId, SQLiteDatabase db) {
        List<Dream> dreams = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query = "SELECT dream_id, user_id, title, content, nature, tags, is_public, is_favorite, created_at " +
                    "FROM dreams WHERE user_id = ? ORDER BY created_at DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Dream dream = new Dream(
                            cursor.getInt(cursor.getColumnIndexOrThrow("dream_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("title")),
                            cursor.getString(cursor.getColumnIndexOrThrow("content")),
                            cursor.getString(cursor.getColumnIndexOrThrow("nature")),
                            cursor.getString(cursor.getColumnIndexOrThrow("tags")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1,
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    dreams.add(dream);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("MyDreamsViewModel", "获取用户梦境失败", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return dreams;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源
    }
}