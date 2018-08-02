package com.payline.payment.p24.utils;

public enum P24Url {
    REST_HOST("secure.przelewy24.pl", "sandbox.przelewy24.pl"),
    SOAP_SER("https://secure.przelewy24.pl/external/wsdl/service.php", "https://sandbox.przelewy24.pl/external/wsdl/service.php"),
    SOAP_ENDPOINT("https://secure.przelewy24.pl/external/wsdl/service.php", "https://sandbox.przelewy24.pl/external/wsdl/service.php");

    private String prodUrl;

    private String sandboxUrl;


    P24Url(String prodUrl, String sandboxUrl) {
        this.prodUrl = prodUrl;
        this.sandboxUrl = sandboxUrl;
    }

    /**
     * @param isSandBox
     * @return the corresponding prod / sandbox url
     */
    public String getUrl(boolean isSandBox) {
        if (!isSandBox) {
            return prodUrl;
        }
        return sandboxUrl;
    }
}