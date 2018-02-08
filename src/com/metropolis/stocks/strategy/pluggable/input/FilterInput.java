package com.metropolis.stocks.strategy.pluggable.input;


import com.fasterxml.jackson.annotation.JsonTypeName;
import com.metropolis.stocks.account.Account;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@JsonTypeName("FilterInput")
public class FilterInput extends Input {

    public Integer minimumPrice = Account.MINIMUM_SECURITY_PRICE;

    public FilterInput() {}

    public FilterInput(final Integer minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    @Override
    public void loadFromHashMap(final LinkedHashMap<String, String> linkedHashMap) {
        if (linkedHashMap.containsKey("minimumPrice")) {
            minimumPrice = Integer.valueOf(linkedHashMap.get("minimumPrice"));
        }
    }
}
