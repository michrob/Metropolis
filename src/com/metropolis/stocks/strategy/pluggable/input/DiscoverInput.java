package com.metropolis.stocks.strategy.pluggable.input;


import com.fasterxml.jackson.annotation.JsonTypeName;
import com.metropolis.stocks.data.Criteria;
import com.metropolis.stocks.market.Market;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@JsonTypeName("DiscoverInput")
public class DiscoverInput extends Input {

    public Market.MARKET market = Market.MARKET.NYSE;
    public Criteria criteria = Criteria.gainer;
    public Integer daysAgo = 1;
    public Integer topN = 100;

    public DiscoverInput() {
    }

    public DiscoverInput(final Market.MARKET market, final Criteria criteria, final int daysAgo, final int topN) {
        this.topN = topN;
        this.market = market;
        this.criteria = criteria;
        this.daysAgo = daysAgo;
    }

    @Override
    public String toString() {
        return "[market:" + market + ", criteria:" + criteria + ", daysAgo:" + daysAgo + ", topN:" + topN + "]";
    }

    @Override
    public void loadFromHashMap(final LinkedHashMap<String, String> linkedHashMap) {
        if (linkedHashMap.containsKey("daysAgo")) {
            daysAgo = Integer.valueOf(linkedHashMap.get("daysAgo"));
        }
        if (linkedHashMap.containsKey("market")) {
            market = Market.MARKET.fromString(linkedHashMap.get("market"));
        }
        if (linkedHashMap.containsKey("criteria")) {
            criteria = Criteria.fromString(linkedHashMap.get("criteria"));
        }
        if (linkedHashMap.containsKey("topN")) {
            topN = Integer.valueOf(linkedHashMap.get("topN"));
        }
    }
}