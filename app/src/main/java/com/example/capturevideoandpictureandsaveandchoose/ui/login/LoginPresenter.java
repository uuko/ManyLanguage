package com.example.capturevideoandpictureandsaveandchoose.ui.login;

import android.util.Log;

import com.example.capturevideoandpictureandsaveandchoose.R;
import com.example.capturevideoandpictureandsaveandchoose.base.BasePresenter;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.ApiService;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.ErpAPI;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.login.LoginRequest;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.login.LoginResponse;
import com.example.capturevideoandpictureandsaveandchoose.utils.rxjava.SchedulerProvider;
import com.example.capturevideoandpictureandsaveandchoose.utils.sharepreferences.LoginPreferencesProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class LoginPresenter<V extends LoginContract.View> extends BasePresenter<V> implements LoginContract.Presenter<V> {
    @Inject
    LoginPreferencesProvider mLoginPreferencesProvider;
    private String LOGIN_AUTHORIZED_ID = "acd9be92-46bf-4185-8721-5b60c67f0742";
    private String AccessToken = "";

    @Inject
    public LoginPresenter(ApiService api, ErpAPI erpAPI, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable) {
        super(api, erpAPI, schedulerProvider, compositeDisposable);
    }

    @Override
    public void onLogin(String account, String password) {
        getView().showProgressDialog("登入中");
        String url = getView().getResourceString(R.string.api_on_Login);
//        LoginRequest mLoginRequest=new LoginRequest(LOGIN_AUTHORIZED_ID,"N000054949","1203-Z");
//        LoginRequest mLoginRequest=new LoginRequest(LOGIN_AUTHORIZED_ID,"N000135056","1203-Z");
        LoginRequest mLoginRequest = new LoginRequest(LOGIN_AUTHORIZED_ID, account, password);
        getCompositeDisposable().add(getApiService().onLogin(url, mLoginRequest)
                        .subscribeOn(getSchedulerProvider().io())
                        .observeOn(getSchedulerProvider().ui())
                        .subscribeWith(new DisposableObserver<LoginResponse>() {

                            @Override
                            public void onNext(LoginResponse loginResponse) {
                                Log.v("LoginResponse", "" + loginResponse.getmResult());

                                if ("true".equals(loginResponse.getmResult())) {
                                    AccessToken = loginResponse.getmToken();
                                    mLoginPreferencesProvider.setToken(loginResponse.getmToken());
                                    getView().dismissProgressDialog();
                                    getView().onCompleteLogin();
                                } else {
                                    getView().showDialogCaveatMessage("登入失敗");
                                    getView().dismissProgressDialog();
//                            getView().onCompleteLogin();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.v("LoginResponse", "" + e.getMessage());
                                if ("HTTP 400 Bad Request".equals(e.getMessage())) {
                                    getView().showDialogCaveatMessage("登入失敗，帳號密碼輸入格式不符合");
                                } else if ("java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 2 path $".equals(e.getMessage())) {
                                    getView().showDialogCaveatMessage("登入失敗");
                                } else {
                                    getView().showDialogCaveatMessage("登入失敗，請檢查網路狀況");
                                }
                                getView().dismissProgressDialog();
//                        getView().onCompleteLogin();        //API壞掉，暫時修改
                            }

                            @Override
                            public void onComplete() {

                            }
                        })
        );
    }

    @Override
    public void onAutoLogin(final String account, String password) {
        getView().showProgressDialog("自動登入中");
        String url = getView().getResourceString(R.string.api_on_Login);
        LoginRequest mLoginRequest = new LoginRequest(LOGIN_AUTHORIZED_ID, account, password);
//        LoginRequest mLoginRequest=new LoginRequest(LOGIN_AUTHORIZED_ID,"N000135056","1203-Z");
//        LoginRequest mLoginRequest=new LoginRequest(LOGIN_AUTHORIZED_ID,account,password);
        getCompositeDisposable().add(getApiService().onLogin(url, mLoginRequest)
                        .subscribeOn(getSchedulerProvider().io())
                        .observeOn(getSchedulerProvider().ui())
                        .subscribeWith(new DisposableObserver<LoginResponse>() {

                            @Override
                            public void onNext(LoginResponse loginResponse) {
                                if ("true".equals(loginResponse.getmResult())) {
                                    AccessToken = loginResponse.getmToken();
                                    mLoginPreferencesProvider.setToken(loginResponse.getmToken());
                                    getView().dismissProgressDialog();
                                    getView().onCoompleteAutoLogin(account);
                                } else {
                                    getView().showDialogCaveatMessage("自動登入失敗");
                                    getView().dismissProgressDialog();
//                            getView().onCompleteLogin();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                getView().dismissProgressDialog();
                                getView().showDialogCaveatMessage("自動登入失敗");
                            }

                            @Override
                            public void onComplete() {

                            }
                        })
        );
    }

    public String getAccessToken() {
        return AccessToken;
    }
}
