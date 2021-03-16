package com.dtako.freecall.network;

import androidx.annotation.Keep;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

/**
 * Created by Nguyen Kim Khanh on 9/3/2020.
 */

public interface ApiCloudService {
    @POST("send")
    Call<String> sendRemoteMessage(
            @HeaderMap HashMap<String, String> header,
            @Body String remoteBody
    );
}
