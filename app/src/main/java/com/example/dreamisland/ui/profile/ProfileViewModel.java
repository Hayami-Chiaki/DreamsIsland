package com.example.dreamisland.ui.profile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.model.User;

public class ProfileViewModel extends AndroidViewModel {
    private MutableLiveData<User> userLiveData;
    private DreamDatabaseHelper databaseHelper;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userLiveData = new MutableLiveData<>();
        databaseHelper = new DreamDatabaseHelper(application);
    }

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void loadUserInfo() {
        SharedPreferences sp = getApplication().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        int userId = sp.getInt("logged_in_user_id", -1);
        if (userId == -1) {
            return;
        }
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "users",
                new String[]{"user_id", "username", "password", "avatar", "email", "phone", "gender", "birthday", "bio"},
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            User user = new User();
            user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));

            byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
            if (avatarBytes != null && avatarBytes.length > 0) {
                Bitmap avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                user.setAvatar(avatar);
            }

            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            user.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
            user.setBirthday(cursor.getString(cursor.getColumnIndexOrThrow("birthday")));
            user.setBio(cursor.getString(cursor.getColumnIndexOrThrow("bio")));
            userLiveData.postValue(user);
        }

        cursor.close();
        db.close();
    }

    public void updateUserInfo(User user) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        
        byte[] avatarBytes = null;
        if (user.getAvatar() != null) {
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
            user.getAvatar().compress(Bitmap.CompressFormat.PNG, 100, stream);
            avatarBytes = stream.toByteArray();
        }

        android.content.ContentValues values = new android.content.ContentValues();
        values.put("username", user.getUsername());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("gender", user.getGender());
        values.put("birthday", user.getBirthday());
        values.put("bio", user.getBio());
        if (avatarBytes != null) {
            values.put("avatar", avatarBytes);
        }

        db.update("users", values, "user_id = ?", new String[]{String.valueOf(user.getUserId())});
        db.close();

        // 重新加载用户信息
        loadUserInfo();
    }
}
