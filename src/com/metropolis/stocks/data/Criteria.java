package com.metropolis.stocks.data;


import lombok.Getter;

public enum Criteria {
    gainer("gainer"),
    loser("loser"),
    volume("volume");

    @Getter
    private String text;

    Criteria(final String text) {
        this.text = text;
    }

    public static Criteria fromString(final String text) {
        if (text != null) {
            for (Criteria b : Criteria.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }
}
