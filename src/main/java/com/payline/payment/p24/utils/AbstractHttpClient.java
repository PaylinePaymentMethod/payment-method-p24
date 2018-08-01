package com.payline.payment.p24.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

abstract class AbstractHttpClient {

    public static final String CONTENT_TYPE = "Content-Type";
    private OkHttpClient client;

    /**
     * Constructeur par défaut.
     */
    public AbstractHttpClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    /**
     * @param scheme      URI schenme [http | https]
     * @param host
     * @param path
     * @param requestBody
     * @param contentType
     * @return okhttp3 response
     * @throws IOException
     */
    public Response doPost(String scheme, String host, String path, RequestBody requestBody, String contentType) throws IOException {

        // create url
        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .addPathSegment(path)
                .build();

        // create request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader(CONTENT_TYPE, contentType)
                .build();

        // do the request
        return client.newCall(request).execute();
    }


}
