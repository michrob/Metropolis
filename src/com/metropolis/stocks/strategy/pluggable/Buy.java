package com.metropolis.stocks.strategy.pluggable;


import com.google.common.collect.ImmutableList;
import com.metropolis.stocks.account.Account;
import com.metropolis.stocks.data.BuyOrder;
import com.metropolis.stocks.data.Candidate;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.stocks.strategy.pluggable.input.BuyInput;
import com.metropolis.stocks.strategy.pluggable.input.Input;

import java.util.ArrayList;
import java.util.List;

public class Buy implements Pluggable {

    public static void buy(final Strategy strategy, final Object... inputs) {
        for (final Object object : inputs) {
            if (object instanceof BuyInput) {
                BuyInput buyInput = (BuyInput) object;

                Account account = strategy.getAccount();
                List<Candidate> candidates = strategy.getCandidates();

                if (candidates.size() <= 0) {
                    continue;
                }

                int sharesToBuy = Math.max(1, buyInput.sharesToBuy);

                final List<BuyOrder> buyOrders = new ArrayList<>();

                for (final Candidate candidate : candidates) {
                    if (candidates.size() == 0) {
                        break;
                    }

                    buyOrders.add(new BuyOrder(candidate.getSymbol(), sharesToBuy));
                }

                candidates.clear();
                account.buySymbols(buyOrders);
            }
        }
    }


    @Override
    public void accept(final Strategy strategy, final List<Input> inputs) {
        buy(strategy, inputs);
    }

    @Override
    public List<Class<?>> getValidInputs() {
        return ImmutableList.of(BuyInput.class);
    }

}
