package com.example.dreamisland.ui.myDreams;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyDreamsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MyDreamsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}