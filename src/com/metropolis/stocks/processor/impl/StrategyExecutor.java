package com.metropolis.stocks.processor.impl;

import com.metropolis.stocks.data.Event;
import com.metropolis.stocks.data.State;
import com.metropolis.stocks.processor.EventProcessor;
import com.metropolis.stocks.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class StrategyExecutor implements EventProcessor {

    private static final int numberOfThreads = Runtime.getRuntime().availableProcessors();

    @Override
    public <T extends Strategy> void processEvent(final Event event, final List<T> strategies) {
        log.debug("Processing event {} on date {}", event, State.getInstance().todayStr());

        final List<Future<?>> executorFutures = new ArrayList<>(numberOfThreads);
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        final ConcurrentLinkedQueue<Strategy> strategyQueue = new ConcurrentLinkedQueue<>(strategies);

        for (int i = 0; i < numberOfThreads; ++i) {
            executorFutures.add(executorService.submit(() -> {
                while (!strategyQueue.isEmpty()) {
                    final Strategy strategy = strategyQueue.poll();
                    if (strategy != null) {
                        strategy.processEvent(event);
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

}
