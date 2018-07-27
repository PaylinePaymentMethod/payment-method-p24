package com.payline.payment.p24.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpClient {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();


    public static Response doPost(String path, Map<String, String> body) throws IOException {
        // create body from Map param
        RequestBody requestBody = createMultipartBody(body);

        // create url
        HttpUrl url = new HttpUrl.Builder()
                .scheme(P24Constants.SCHEME)
                .host(P24Constants.HOST)
                .addPathSegment(path)
                .build();

        // create request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        // do the request
        return client.newCall(request).execute();
    }


    /**
     * create requestBody from a map of args
     *
     * @param formData a Map containing all fields to put in the requestBody
     * @return the requesBody created
     */
    private static RequestBody createMultipartBody(Map<String, String> formData) {
        MultipartBody.Builder mbb = new MultipartBody.Builder();
        mbb.setType(MultipartBody.FORM);
        for (String key : formData.keySet()) {
            mbb.addFormDataPart(key, formData.get(key));
        }
        return mbb.build();
    }

}
