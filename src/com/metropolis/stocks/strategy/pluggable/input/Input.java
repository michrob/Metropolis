package com.metropolis.stocks.strategy.pluggable.input;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.metropolis.stocks.strategy.pluggable.input.sell.HoursOldSellInput;
import com.metropolis.stocks.strategy.pluggable.input.sell.ProfitLossSellInput;

import java.util.LinkedHashMap;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "name")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BuyInput.class, name = "BuyInput"),
        @JsonSubTypes.Type(value = FilterInput.class, name = "FilterInput"),
        @JsonSubTypes.Type(value = HoursOldSellInput.class, name = "HoursOldSellInput"),
        @JsonSubTypes.Type(value = ProfitLossSellInput.class, name = "ProfitLossSellInput"),
        @JsonSubTypes.Type(value = DiscoverInput.class, name = "DiscoverInput"),
})
public abstract class Input {

    public abstract void loadFromHashMap(final LinkedHashMap<String, String> linkedHashMap);

}
