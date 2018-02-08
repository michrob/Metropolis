package com.metropolis.stocks.strategy.pluggable.input.sell;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.metropolis.stocks.strategy.pluggable.input.Input;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@JsonTypeName("ProfitLossSellInput")
public class ProfitLossSellInput extends Input {

    public Float profitThreshold = .0f;
    public Float lossThreshold = .0f;

    public ProfitLossSellInput() {
    }

    public ProfitLossSellInput(final Float profitThreshold, final Float lossThreshold) {
        this.profitThreshold = profitThreshold;
        this.lossThreshold = lossThreshold;
    }

    @Override
    public String toString() {
        return "[profitThreshold:" + profitThreshold + ", lossThreshold:" + lossThreshold + "]";
    }

    @Override
    public void loadFromHashMap(final LinkedHashMap<String, String> linkedHashMap) {
        if (linkedHashMap.containsKey("profitThreshold")) {
            profitThreshold = Float.valueOf(linkedHashMap.get("profitThreshold"));
        }
        if (linkedHashMap.containsKey("lossThreshold")) {
            lossThreshold = Float.valueOf(linkedHashMap.get("lossThreshold"));
        }
    }
}
