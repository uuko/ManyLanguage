package com.example.capturevideoandpictureandsaveandchoose.ui.main;

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
    }

    interface Presenter<V extends MainContract.View> extends BaseAttacher<V> {
        void onGetDisposableToken(String DeviceId);
        void onAddChkInfo(ChooseDeviceItemData mChooseDeviceItemData);
        String getDisposableToken();
    }
}
