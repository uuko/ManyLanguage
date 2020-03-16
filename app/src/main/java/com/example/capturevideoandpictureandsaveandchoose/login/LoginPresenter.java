package com.example.capturevideoandpictureandsaveandchoose.login;

import android.util.Log;

import com.example.capturevideoandpictureandsaveandchoose.base.BasePresenter;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.ApiService;
import com.example.capturevideoandpictureandsaveandchoose.utils.rxjava.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class LoginPresenter <V extends LoginContract.View> extends BasePresenter<V> implements LoginContract.Presenter<V> {

    @Inject
    public LoginPresenter(ApiService api, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable) {
        super(api, schedulerProvider, compositeDisposable);
    }

    @Override
    public void onLogin() {
        Log.e("gggg","sssss");
    }
}
