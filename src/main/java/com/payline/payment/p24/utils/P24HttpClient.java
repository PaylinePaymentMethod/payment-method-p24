package com.payline.payment.p24.utils;

import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class P24HttpClient extends AbstractHttpClient {

    public static final String P24_CONTENT_TYPE = "application/x-www-form-urlencoded";


    public Response doPost(String host, P24Path path, Map<String, String> body) throws IOException {
        RequestBody requestBody = new MltiPartRequestBodyBuilder().withFormData(body).build();
        return super.doPost(P24Constants.SCHEME, host, path.getPath(), requestBody, P24_CONTENT_TYPE);
    }

}
