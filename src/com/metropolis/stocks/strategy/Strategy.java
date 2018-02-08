package com.metropolis.stocks.strategy;

import com.google.common.collect.ImmutableList;
import com.metropolis.stocks.account.Account;
import com.metropolis.stocks.data.Candidate;
import com.metropolis.stocks.data.Event;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.Market;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Getter
@Setter
public class Strategy {

    private static final Market staticMarket = Market.instance();

    private final Account account;
    private final String name;
    private final SortedMap<Event, List<StrategyMethod>> strategyMethods;
    private final Score score = new Score();

    @Setter
    private List<Candidate> candidates = new ArrayList<>();

    public Strategy() {
        this.name = "DEFAULT";
        this.account = new Account(this.name);
        this.strategyMethods = new TreeMap<>();
    }

    public Strategy(final String name, final Map<Event, List<StrategyMethod>> strategyMap) {
        this.account = new Account(name);
        this.name = name;
        this.strategyMethods = new TreeMap<>(strategyMap);
    }

    public Strategy(final String name) {
        this.account = new Account(name);
        this.name = name;
        this.strategyMethods = new TreeMap<>();
    }

    public Strategy(final Strategy otherStrategy) {
        this.account = new Account(otherStrategy.getAccount());
        this.name = otherStrategy.getName();
        this.strategyMethods = new TreeMap<>();

        for (final Map.Entry<Event, List<StrategyMethod>> mapEntry : otherStrategy.getStrategyMethods().entrySet()) {
            List<StrategyMethod> newStrategyMethods = new ArrayList<>();

            StrategyMethod lastStrategyMethod = null;
            for (final StrategyMethod strategyMethod : mapEntry.getValue()) {

                if (lastStrategyMethod != null &&
                    lastStrategyMethod.getBasicMethod().getName().equals(strategyMethod.getBasicMethod().getName())) {
                    lastStrategyMethod.addParameters(strategyMethod.getParameters());
                } else {
                    newStrategyMethods.add(new StrategyMethod(strategyMethod));
                }

                lastStrategyMethod = newStrategyMethods.get(newStrategyMethods.size() - 1);
            }

            this.strategyMethods.put(mapEntry.getKey(), newStrategyMethods);
        }
    }

    private void updateCandidateQuotes() {
        final Set<Symbol> symbols = candidates.stream().map(Candidate::getSymbol).collect(Collectors.toSet());
        final Map<Symbol, Float> quotes = staticMarket.retrieveQuotesForSymbols(symbols);
        for (final Candidate candidate : candidates) {
            if (quotes.containsKey(candidate.getSymbol())) {
                candidate.setQuote(quotes.get(candidate.getSymbol()));
            }
        }
    }

    public void processEvent(final Event event) {
        updateCandidateQuotes();
        final List<StrategyMethod> methods = strategyMethods.getOrDefault(event, ImmutableList.of());
        for (final StrategyMethod method : methods) {
            method.invoke(this);
        }
        score.updateScore(account);
    }

    public void addMethod(final Event event, final StrategyMethod strategyMethod) {
        List<StrategyMethod> methodsForEvent = strategyMethods.computeIfAbsent(event, list -> new ArrayList<>());
        methodsForEvent.add(strategyMethod);
    }

    public int getWeight() {
        final int[] totalWeight = {0};
        strategyMethods.values().forEach(list -> list.forEach(sm -> totalWeight[0] += sm.getParameters().size()));
        return totalWeight[0];
    }

    public void finalize() {
        this.score.finalizeScore(account);
    }

    public void reset() {
        this.candidates.clear();
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Strategy: ").append(name).append(" -> ")
                     .append(account).append(" -> ").append(getWeight()).append("\n");
        for (final Event event : strategyMethods.keySet()) {
            stringBuilder.append(event.getText());
            for (final StrategyMethod strategyMethod : strategyMethods.get(event)) {
                stringBuilder.append(" -> ").append(strategyMethod.toString());
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
