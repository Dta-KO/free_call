package com.dtako.freecall.network;

import com.dtako.freecall.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Nguyen Kim Khanh on 9/3/2020.
 */

public class ApiCloudClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Builder().baseUrl(Constants.BASE_URL_FIREBASE)
                    .addConverterFactory(ScalarsConverterFactory.create()).build();
        }
        return retrofit;
    }
}
