package com.payline.payment.p24.utils;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.util.Map;

public class MltiPartRequestBodyBuilder {

    Map<String, String> formData;

    public MltiPartRequestBodyBuilder withFormData(Map<String, String> formData) {
        this.formData = formData;
        return this;
    }

    public RequestBody build() {
        MultipartBody.Builder mbb = new MultipartBody.Builder();
        mbb.setType(MultipartBody.FORM);
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (entry.getValue() != null) {
                mbb.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        return mbb.build();
    }

}
