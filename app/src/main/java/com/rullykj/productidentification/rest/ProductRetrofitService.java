package com.rullykj.productidentification.rest;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Rully Kusumajaya on 8/13/2023.
 * Badan Riset dan Inovasi Nasional
 * rull001@brin.go.id
 * r.kusumajaya@liverpool.ac.uk
 */
public interface ProductRetrofitService {
    @Headers({
            "Content-Type: application/json",
            "apikey: AAv7pyA72j93rhFe53AEIjRxByQae1DY"
    })
    //@POST("serviceNew/cek.php")
    @POST("predict")
    Single<String> predict(@Body String jsonInString);
}
