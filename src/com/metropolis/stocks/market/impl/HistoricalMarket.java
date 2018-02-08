package com.metropolis.stocks.market.impl;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.metropolis.stocks.data.HistoricalStockPrice;
import com.metropolis.stocks.data.State;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.Market;
import com.metropolis.stocks.source.Google;
import com.metropolis.stocks.source.PriceProvider;
import com.metropolis.stocks.source.Yahoo;
import com.metropolis.util.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class HistoricalMarket extends Market {

    private static final PriceProvider[] priceProviders = {new Yahoo(), new Google()};

    private static final int numberOfThreads = Runtime.getRuntime().availableProcessors();

    private static Set<Symbol> symbolsToIgnore = Sets.newConcurrentHashSet();
    private static Set<Symbol> symbolsCached = Sets.newConcurrentHashSet();

    // symbol -> dateStr -> price
    private final LoadingCache<Symbol, Map<String, HistoricalStockPrice>> stockPrices =
            CacheBuilder.newBuilder()
                        .build(new CacheLoader<Symbol, Map<String, HistoricalStockPrice>>() {
                            public Map<String, HistoricalStockPrice> load(final Symbol symbol) {

                                for (final PriceProvider priceProvider : priceProviders) {
                                    try {
                                        Map<String, HistoricalStockPrice> stockPriceMap =
                                                priceProvider.getPriceCache(symbol, Config.CACHE_EPOCH, Config.CACHE_END);
                                        symbolsCached.add(symbol);
                                        return stockPriceMap;
                                    } catch (IOException e) {
                                        //log.debug("{}", e);
                                    }
                                }
                                symbolsToIgnore.add(symbol);
                                return Collections.emptyMap();
                            }
                        });


    /*
    public Map<String, Float> getQuotesForSymbols2(final Set<String> symbols) {
        symbols.removeAll(symbolsToIgnore);

        /*final Set<String> symbolsWithoutQuotes = Sets.difference(symbols, symbolsCached);

        if (symbolsWithoutQuotes.size() > 0) {
            cacheSymbols(symbolsWithoutQuotes);
        }

        final String todayStr = State.instance().todayStr();

        Map<String, Float> quotesForSymbols = new HashMap<>();

        for (final String symbol : symbols) {
            try {
                Map<String, HistoricalStockPrice> priceMap = stockPrices.get(symbol);
                HistoricalStockPrice historicalStockPrice = priceMap.get(todayStr);

                if (historicalStockPrice == null) {
                    continue;
                }

                switch (State.instance().getThisEvent()) {
                    case OPEN: {
                        quotesForSymbols.put(symbol, historicalStockPrice.getOpenPrice());
                        break;
                    }
                    case CLOSE: {
                        quotesForSymbols.put(symbol, historicalStockPrice.getClosePrice());
                        break;
                    }
                    default:
                        break;
                }
            } catch (Exception e) {
                log.warn("{}", e);
                continue;
            }
        }

        return quotesForSymbols;
    }*/

    private void cacheSymbols(final Set<Symbol> symbols) {
        final List<Future<?>> executorFutures = new ArrayList<>(numberOfThreads);
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        final ConcurrentLinkedQueue<Symbol> stringQueue = new ConcurrentLinkedQueue<>(symbols);

        for (int i = 0; i < numberOfThreads; ++i) {
            executorFutures.add(executorService.submit(() -> {
                while (!stringQueue.isEmpty()) {
                    final Symbol symbol = stringQueue.poll();
                    if (symbol == null) {
                        continue;
                    }
                    try {
                        stockPrices.get(symbol);
                    } catch (ExecutionException e) {
                        log.warn("{}", e);
                    }
                }
            }));
        }

        try {
            for (final Future<?> demandScoreFuture : executorFutures) {
                demandScoreFuture.get();
            }
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            log.warn(e.getMessage());
        }

        executorService.shutdown();
    }

    public Optional<Float> getQuote(final Symbol symbol) {
        final String todayStr = State.getInstance().todayStr();

        try {
            Map<String, HistoricalStockPrice> priceMap = stockPrices.get(symbol);
            HistoricalStockPrice historicalStockPrice = priceMap.get(todayStr);

            if (historicalStockPrice == null) {
                return Optional.empty();
            }

            switch (State.getInstance().getThisEvent()) {
                case OPEN: {
                    return Optional.of(historicalStockPrice.getOpenPrice());
                }
                case CLOSE: {
                    return Optional.of(historicalStockPrice.getClosePrice());
                }
                default:
                    break;
            }
        } catch (ExecutionException e) {
            log.warn("{}", e);
        }
        return Optional.empty();
    }

    @Override
    public Map<Symbol, Float> retrieveQuotesForSymbols(final Collection<Symbol> symbols) {
        final Set<Symbol> symbolsToGet = new HashSet<>(symbols);

        symbolsToGet.removeAll(symbolsToIgnore);

        Set<Symbol> uncachedSymbols = Sets.difference(symbolsToGet, symbolsCached);
        if (uncachedSymbols.size() > 0) {
            cacheSymbols(uncachedSymbols);
        }

        final Map<Symbol, Float> symbolsToQuotes = new HashMap<>();

        for (final Symbol symbol : symbolsToGet) {
            Optional<Float> optionalQuote = getQuote(symbol);
            optionalQuote.ifPresent(aFloat -> symbolsToQuotes.put(symbol, aFloat));
        }

        /*if (symbolsToQuotes.size() == symbols.size()) {
            return symbolsToQuotes;
        }

        Set<Symbol> symbolsWithoutQuotes = Sets.difference(symbols, symbolsToQuotes.keySet());
        if (symbolsWithoutQuotes.size() > 0) {
            log.warn("{} symbols with no quote", symbolsWithoutQuotes.size());
        }*/

        return symbolsToQuotes;
    }

}
