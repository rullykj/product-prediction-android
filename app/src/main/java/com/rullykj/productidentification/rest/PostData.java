package com.rullykj.productidentification.rest;

import retrofit2.http.Field;

/**
 * Created by Rully Kusumajaya on 8/13/2023.
 * Badan Riset dan Inovasi Nasional
 * rull001@brin.go.id
 * r.kusumajaya@liverpool.ac.uk
 */
public class PostData {
    int id;
    String operation;
    String requestTime;
    String token;
    String image;

    public PostData(int id, String operation, String requestTime, String token, String image) {
        this.id = id;
        this.operation = operation;
        this.requestTime = requestTime;
        this.token = token;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operasi) {
        this.operation = operation;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
