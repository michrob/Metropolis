package com.metropolis.stocks.market.impl;


import com.google.common.collect.ImmutableMap;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.Market;
import com.metropolis.stocks.source.Robinhood;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class RealTimeMarket extends Market {

    private Robinhood robinhood = new Robinhood("username", "password");

    @Override
    public Map<Symbol, Float> retrieveQuotesForSymbols(final Collection<Symbol> symbols) {
        return ImmutableMap.of();//robinhood.getQuotes(symbols);
    }

}
