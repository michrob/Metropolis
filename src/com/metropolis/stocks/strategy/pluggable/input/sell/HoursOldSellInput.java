package com.metropolis.stocks.strategy.pluggable.input.sell;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.metropolis.stocks.strategy.pluggable.input.Input;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@JsonTypeName("HoursOldSellInput")
public class HoursOldSellInput extends Input {

    public Integer sharesToSell;
    public Integer hoursOld;

    public HoursOldSellInput() {
    }

    public HoursOldSellInput(final Integer sharesToSell, final Integer hoursOld) {
        this.sharesToSell = sharesToSell;
        this.hoursOld = hoursOld;
    }

    @Override
    public String toString() {
        return "[sharesToSell:" + sharesToSell + ", hoursOld:" + hoursOld + "]";
    }

    @Override
    public void loadFromHashMap(final LinkedHashMap<String, String> linkedHashMap) {
        if (linkedHashMap.containsKey("sharesToSell")) {
            sharesToSell = Integer.valueOf(linkedHashMap.get("sharesToSell"));
        }
        if (linkedHashMap.containsKey("hoursOld")) {
            hoursOld = Integer.valueOf(linkedHashMap.get("hoursOld"));
        }
    }
}
