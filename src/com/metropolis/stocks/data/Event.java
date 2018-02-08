package com.metropolis.stocks.data;


import lombok.Getter;

public enum Event {

    // Time events.
    BEFORE_OPEN("BEFORE_OPEN"),
    OPEN("OPEN"),
    TICK("TICK"),
    CLOSE("CLOSE"),
    AFTER_CLOSE("AFTER_CLOSE"),

    // Instruction events.
    PERSIST("PERSIST"),
    UNKNOWN("UNKNOWN");

    @Getter
    private String text;

    Event(final String text) {
        this.text = text;
    }

    public static Event fromString(final String text) {
        if (text != null) {
            for (Event b : Event.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }
}
