package com.rullykj.productidentification.rest;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Rully Kusumajaya on 8/13/2023.
 * Badan Riset dan Inovasi Nasional
 * rull001@brin.go.id
 * r.kusumajaya@liverpool.ac.uk
 */
public class RestClient {
    public static ProductRetrofitService getProductRetrofitService() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

        // set log level
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

        //add logging
        //okHttpClient.addInterceptor(httpLoggingInterceptor).build();

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build().create(ProductRetrofitService.class);
    }
}
