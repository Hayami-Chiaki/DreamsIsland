package com.example.dreamisland.ui.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.dreamisland.R;
import com.example.dreamisland.databinding.ActivityEditProfileBinding;
import com.example.dreamisland.model.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    
    private ActivityEditProfileBinding binding;
    private ProfileViewModel viewModel;
    private User currentUser;
    private Bitmap selectedAvatar;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topBar);

        // 初始化日期格式化器
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        calendar = Calendar.getInstance();

        // 设置ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.edit_profile_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ProfileViewModelFactory factory = new ProfileViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);

        // 加载当前用户信息
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                loadUserData(user);
            }
        });
        viewModel.loadUserInfo();

        setupViews();
    }

    private void setupViews() {
        // 头像点击事件
        binding.avatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        // 保存按钮
        binding.saveButton.setOnClickListener(v -> saveProfile());

        // 取消按钮
        binding.cancelButton.setOnClickListener(v -> finish());
        
        // 确保所有输入框都是可编辑的
        binding.usernameEditText.setEnabled(true);
        binding.usernameEditText.setFocusable(true);
        binding.usernameEditText.setFocusableInTouchMode(true);
        
        binding.emailEditText.setEnabled(true);
        binding.emailEditText.setFocusable(true);
        binding.emailEditText.setFocusableInTouchMode(true);
        
        binding.phoneEditText.setEnabled(true);
        binding.phoneEditText.setFocusable(true);
        binding.phoneEditText.setFocusableInTouchMode(true);
        
        // 设置生日输入框的日历选择器功能
        setupBirthdayInput();
        
        // 点击事件
        binding.birthdayEditText.setOnClickListener(v -> showDatePickerDialog());
        
        // 触摸事件 - 确保点击时打开日期选择器
        binding.birthdayEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                showDatePickerDialog();
                return true;
            }
            return false;
        });
        
        // 阻止获得焦点时弹出键盘
        binding.birthdayEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.birthdayEditText.clearFocus();
                showDatePickerDialog();
            }
        });
        
        // 为TextInputLayout的endIcon（日历图标）添加点击事件
        binding.birthdayTextInputLayout.setEndIconOnClickListener(v -> showDatePickerDialog());
        
        // 为TextInputLayout本身添加点击事件
        binding.birthdayTextInputLayout.setOnClickListener(v -> showDatePickerDialog());
        
        binding.bioEditText.setEnabled(true);
        binding.bioEditText.setFocusable(true);
        binding.bioEditText.setFocusableInTouchMode(true);

        String[] options = getResources().getStringArray(R.array.gender_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(ContextCompat.getColor(EditProfileActivity.this, android.R.color.white));
                return v;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(ContextCompat.getColor(EditProfileActivity.this, android.R.color.white));
                ((TextView) v).setBackgroundColor(ContextCompat.getColor(EditProfileActivity.this, R.color.tertiary_30));
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.genderSpinner.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        // 如果有已保存的日期，解析并设置为默认日期
        Calendar defaultCalendar = Calendar.getInstance();
        String currentDate = binding.birthdayEditText.getText().toString();
        
        String hintText = getString(R.string.click_to_select_date);
        if (currentDate != null && !currentDate.isEmpty() && !currentDate.equals(hintText)) {
            try {
                java.util.Date date = dateFormatter.parse(currentDate);
                if (date != null) {
                    defaultCalendar.setTime(date);
                }
            } catch (Exception e) {
                // 如果解析失败，使用18年前的今天作为默认值（合理的默认生日）
                defaultCalendar = Calendar.getInstance();
                defaultCalendar.add(Calendar.YEAR, -18);
            }
        } else {
            // 如果没有设置过生日，使用18年前的今天作为默认值
            defaultCalendar = Calendar.getInstance();
            defaultCalendar.add(Calendar.YEAR, -18);
        }

        // 创建日期选择对话框
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // 格式化日期并显示在输入框中
                    String formattedDate = dateFormatter.format(calendar.getTime());
                    binding.birthdayEditText.setText(formattedDate);
                },
                defaultCalendar.get(Calendar.YEAR),
                defaultCalendar.get(Calendar.MONTH),
                defaultCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // 设置最大日期为今天（不能选择未来的日期）
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // 显示对话框
        datePickerDialog.show();
    }

    private void loadUserData(User user) {
        binding.usernameEditText.setText(user.getUsername());
        if (user.getEmail() != null) {
            binding.emailEditText.setText(user.getEmail());
        }
        if (user.getPhone() != null) {
            binding.phoneEditText.setText(user.getPhone());
        }
        if (user.getGender() != null) {
            int genderIndex = 0;
            // 直接比较中文字符串，因为arrays.xml中使用的是中文
            if (user.getGender().equals("女")) {
                genderIndex = 1;
            } else if (user.getGender().equals("其他")) {
                genderIndex = 2;
            }
            binding.genderSpinner.setSelection(genderIndex);
        }
        if (user.getBirthday() != null && !user.getBirthday().isEmpty()) {
            binding.birthdayEditText.setText(user.getBirthday());
        }
        if (user.getBio() != null) {
            binding.bioEditText.setText(user.getBio());
        }
        if (user.getAvatar() != null) {
            binding.avatarImageView.setImageBitmap(user.getAvatar());
        }
        
        // 重新确保生日输入框的设置（防止加载数据后状态被重置）
        setupBirthdayInput();
    }
    
    private void setupBirthdayInput() {
        // 确保生日输入框完全不可编辑，只能通过日历选择器选择
        binding.birthdayEditText.setFocusable(false);
        binding.birthdayEditText.setFocusableInTouchMode(false);
        binding.birthdayEditText.setClickable(true);
        binding.birthdayEditText.setLongClickable(false);
        binding.birthdayEditText.setCursorVisible(false);
        binding.birthdayEditText.setInputType(android.text.InputType.TYPE_NULL);
        
        // 隐藏键盘（如果有）
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
            getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.birthdayEditText.getWindowToken(), 0);
        }
    }

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, R.string.profile_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新用户信息
        currentUser.setUsername(binding.usernameEditText.getText().toString().trim());
        String email = binding.emailEditText.getText().toString().trim();
        if (!android.text.TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser.setEmail(email);
        currentUser.setPhone(binding.phoneEditText.getText().toString().trim());
        currentUser.setBirthday(binding.birthdayEditText.getText().toString().trim());
        currentUser.setBio(binding.bioEditText.getText().toString().trim());

        String selectedGender = binding.genderSpinner.getSelectedItem().toString();
        currentUser.setGender(selectedGender);

        if (selectedAvatar != null) {
            currentUser.setAvatar(selectedAvatar);
        }

        // 保存到数据库
        viewModel.updateUserInfo(currentUser);

        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();

        // 返回结果
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.avatarImageView.setImageBitmap(selectedAvatar);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}


