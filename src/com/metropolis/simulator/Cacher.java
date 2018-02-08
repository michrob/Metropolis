package com.metropolis.simulator;


import com.metropolis.stocks.data.Criteria;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.Market;
import com.metropolis.stocks.source.WSJ;
import com.metropolis.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class Cacher {

    private static final int numberOfThreads = 100;
    private static final Market staticMarket = Market.instance();

    private static void cacheDate(final DateTime dateTime) {
        for (final Market.MARKET market : Market.MARKET.values()) {
            for (final Criteria criteria : Criteria.values()) {
                List<Symbol> symbols = WSJ.getSymbols(market, criteria, dateTime);
                staticMarket.retrieveQuotesForSymbols(symbols);
            }
        }
        log.info("Cached date {}", dateTime);
    }

    public static void cacheSimulationData() {
        final Set<DateTime> datesToCache = new HashSet<>();
        for (DateTime date = Config.SIM_START_DATE; date.isBefore(Config.SIM_END_DATE); date = date.plusDays(1)) {
            datesToCache.add(date);
        }

        final List<Future<?>> executorFutures = new ArrayList<>(numberOfThreads);
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        final ConcurrentLinkedQueue<DateTime> dateTimes = new ConcurrentLinkedQueue<>(datesToCache);

        for (int i = 0; i < numberOfThreads; ++i) {
            executorFutures.add(executorService.submit(() -> {
                while (!dateTimes.isEmpty()) {
                    final DateTime dateTime = dateTimes.poll();
                    if (dateTime == null) {
                        continue;
                    }
                    cacheDate(dateTime);
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

    public static void main(final String[] args) {
        cacheSimulationData();
    }
}
