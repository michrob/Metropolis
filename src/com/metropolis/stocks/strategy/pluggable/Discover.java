package com.metropolis.stocks.strategy.pluggable;


import com.google.common.collect.ImmutableList;
import com.metropolis.stocks.data.Candidate;
import com.metropolis.stocks.data.State;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.Market;
import com.metropolis.stocks.source.WSJ;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.stocks.strategy.pluggable.input.DiscoverInput;
import com.metropolis.stocks.strategy.pluggable.input.Input;
import com.metropolis.util.Dates;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Discover implements Pluggable {

    private static final Market staticMarket = Market.instance();

    public static void discover(final Strategy strategy, final Object... inputs) {
        for (final Object object : inputs) {
            if (object instanceof DiscoverInput) {
                DiscoverInput discoverInput = (DiscoverInput) object;
                int daysAgo = Math.max(1, discoverInput.daysAgo);
                List<Symbol> symbols = WSJ.getSymbols(discoverInput.market, discoverInput.criteria,
                                                      Dates.businessDaysAgo(State.getInstance().today(), daysAgo));

                int topN = Math.min(symbols.size() - 1, discoverInput.topN);
                topN = Math.max(0, topN);
                symbols = symbols.subList(0, topN);

                final Map<Symbol, Float> quotes =
                        staticMarket.retrieveQuotesForSymbols(symbols.stream().collect(Collectors.toSet()));

                for (final Map.Entry<Symbol, Float> quote : quotes.entrySet()) {
                    strategy.getCandidates().add(new Candidate(quote.getKey(), quote.getValue()));
                }
            }
        }
    }

    @Override
    public void accept(final Strategy strategy, final List<Input> inputs) {
        discover(strategy, inputs);
    }

    @Override
    public List<Class<?>> getValidInputs() {
        return ImmutableList.of(DiscoverInput.class);
    }

}
