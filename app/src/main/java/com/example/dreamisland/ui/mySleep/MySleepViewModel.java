package com.example.dreamisland.ui.mySleep;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MySleepViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MySleepViewModel() {
        mText = new MutableLiveData<>();
      //  mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}