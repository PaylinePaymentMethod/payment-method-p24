package com.payline.payment.p24.service.enums;

public enum ChannelKeys {

    CHANNEL_1CC("channelCC", 1),
    CHANNEL_2BT("channelBT", 2),
    CHANNEL_4MT("channelMT", 4),
    CHANNEL_8NA("channelNA", 8),
    CHANNEL_16AM("channelAM", 16),
    CHANNEL_32UP("channelUP", 32);

    private String keyLabel;

    private int weight;

    ChannelKeys(String label, int weight) {
        this.keyLabel = label;
        this.weight = weight;
    }

    public String getKey() {
        return keyLabel;
    }

    public int getValue() {
        return weight;
    }
}
