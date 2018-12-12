package com.payline.payment.p24.utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

public class P24HttpClient extends AbstractHttpClient {
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String P24_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private P24HttpClient() {
    }

    private static class SingletonWrapper {
        private static final P24HttpClient INSTANCE = new P24HttpClient();
    }

    public static P24HttpClient getInstance() {
        return SingletonWrapper.INSTANCE;
    }
    public HttpResponse doPost(String host, P24Path path, Map<String, String> body) throws IOException, URISyntaxException {
        ArrayList<NameValuePair> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : body.entrySet()) {
            parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity urlEncodedBody = new UrlEncodedFormEntity(parameters);
        InputStreamEntity entity = new InputStreamEntity(urlEncodedBody.getContent());

        Header[] headers = new Header[1];
        headers[0] = new BasicHeader(CONTENT_TYPE_KEY, P24_CONTENT_TYPE);

        return super.doPost(P24Constants.SCHEME, host, path.getPath(), headers, entity);
    }
}
