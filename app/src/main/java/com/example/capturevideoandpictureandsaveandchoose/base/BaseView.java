package com.example.capturevideoandpictureandsaveandchoose.base;

import android.app.DatePickerDialog;
import android.content.DialogInterface;

import androidx.annotation.StringRes;

/**
 * Created by 5*N on 2017/12/22
 */

public interface BaseView {
    void init();

    void showProgressDialog(@StringRes int text);
    void showProgressDialog(String text);
    void dismissProgressDialog();

    void showDialogCaveatMessage(String message);
    void showDialogMessage(String message);
    void showDialogMessage(String title,String message);
    void showDialogCaveatMessage(String title,String message);
    void showDatePickerDialog(DatePickerDialog.OnDateSetListener onDateSetListener);
    void showSelectDialog(String text, DialogInterface.OnClickListener onClickListener);

    String getResourceString(@StringRes int text);
    String getTodayTime();
    void showToast(String text);

    void showToast(@StringRes int text);
}
