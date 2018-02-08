package com.metropolis.stocks.data;


import com.metropolis.util.Config;
import com.metropolis.util.Dates;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;


public class State {

    private static DateTime today = new DateTime();
    private static State instance = null;
    @Getter
    @Setter
    private Event thisEvent = Event.UNKNOWN;

    private State() {
    }

    public static State getInstance() {
        if (instance == null) {
            instance = new State();
        }
        return instance;
    }

    public DateTime today() {
        return today;
    }

    public String todayStr() {
        return Dates.dateToString(today());
    }

    public void setToday(final DateTime date) {
        if (Config.isProduction()) {
            return;
        }
        today = date;
    }
}
