package com.example.capturevideoandpictureandsaveandchoose.utils.api;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface ErpAPI {
    @GET("xiaohua.json")
    Observable<List<DataResponse>> getData();
}
