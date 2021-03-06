package com.example.capturevideoandpictureandsaveandchoose.ui.main;

import android.content.Intent;

import com.example.capturevideoandpictureandsaveandchoose.base.BaseAttacher;
import com.example.capturevideoandpictureandsaveandchoose.base.BaseView;
import com.example.capturevideoandpictureandsaveandchoose.ui.choosedevice.ChooseDeviceItemData;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searchco.CORequest;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searcheqkd.EQKDRequest;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searcheqno.EQNORequest;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searchpmfct.PMFCTRequest;

public interface MainContract {
    interface View extends BaseView {
        void onNonService();
        void onSetEQKDNMData(String EQKDNM,int ispickImage);
        void onSetEQKDdataNoTalk(String EQKDM, Intent data,Integer nowInList,Integer urlNow,Integer pickWhat);
    }

    interface Presenter<V extends MainContract.View> extends BaseAttacher<V> {
        void onGetDisposableToken(String DeviceId);
        //void onAddChkInfo(ChooseDeviceItemData mChooseDeviceItemData,boolean loginStatus);
        void onAddChkInfo(ChooseDeviceItemData mChooseDeviceItemData);
        void onAddEndChkInfo(ChooseDeviceItemData mChooseDeviceItemData);
        void onGetEQKDData(String account,String CO,String PMFCT,String EQKD,int ispickImage);
        void onGetEQKDDataNoImg(String account,String CO,String PMFCT,String EQKD,Intent data,Integer nowInList,Integer urlNow,Integer pickWhat);
        String getDisposableToken();
    }
}
