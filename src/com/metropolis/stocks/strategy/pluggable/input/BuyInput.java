package com.metropolis.stocks.strategy.pluggable.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.metropolis.stocks.account.Account;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@JsonTypeName("BuyInput")
public class BuyInput extends Input {

    public Integer sharesToBuy = 1;

    public BuyInput() {
    }

    public BuyInput(final int sharesToBuy, final int firstN) {
        this.sharesToBuy = sharesToBuy;
    }

    @Override
    public String toString() {
        return "[sharesToBuy:" + sharesToBuy + "]";
    }

    @Override
    public void loadFromHashMap(final LinkedHashMap<String, String> linkedHashMap) {
        if (linkedHashMap.containsKey("sharesToBuy")) {
            sharesToBuy = Integer.valueOf(linkedHashMap.get("sharesToBuy"));
        }
    }
}