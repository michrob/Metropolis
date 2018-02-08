package com.metropolis.stocks.strategy.pluggable;


import com.google.common.collect.ImmutableList;
import com.metropolis.stocks.account.Account;
import com.metropolis.stocks.data.BuyOrder;
import com.metropolis.stocks.data.Candidate;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.stocks.strategy.pluggable.input.FilterInput;
import com.metropolis.stocks.strategy.pluggable.input.Input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Filter implements Pluggable {

    public static void filter(final Strategy strategy, final Object... inputs) {

        for (final Object object : inputs) {
            if (object instanceof FilterInput) {
                FilterInput filterInput = (FilterInput) object;

                Account account = strategy.getAccount();
                List<Candidate> candidates = strategy.getCandidates();

                if (candidates.size() <= 0) {
                    continue;
                }

                int minimumPrice = Math.max(Account.MINIMUM_SECURITY_PRICE, filterInput.minimumPrice);

                candidates.removeIf(candidate -> candidate.getQuote() < minimumPrice);
            }
        }
    }

    @Override
    public void accept(final Strategy strategy, final List<Input> inputs) {
        filter(strategy, inputs);
    }

    @Override
    public List<Class<?>> getValidInputs() {
        return ImmutableList.of(FilterInput.class);
    }

}
