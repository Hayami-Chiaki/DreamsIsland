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
    private DreamDatabaseHelper databaseHelper;

    public MyDreamsViewModel(@NonNull Application application) {
        super(application);
        dreamsList = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
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

    // 加载用户的梦境列表（默认加载ID为1的用户）
    public void loadUserDreams(int userId) {
        isLoading.setValue(true);
        new Thread(() -> {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = databaseHelper.getReadableDatabase();
                String query = "SELECT dream_id, user_id, title, content, nature, tags, is_public, created_at " +
                        "FROM dreams WHERE user_id = ? ORDER BY created_at DESC";
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
                String query = "INSERT INTO dreams (user_id, title, content, nature, tags, is_public, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                db.execSQL(query, new Object[]{
                        dream.getUserId(),
                        dream.getTitle(),
                        dream.getContent(),
                        dream.getNature(),
                        dream.getTags(),
                        dream.isPublic() ? 1 : 0,
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

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源
    }
}