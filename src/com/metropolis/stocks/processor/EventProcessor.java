package com.metropolis.stocks.processor;

import com.metropolis.stocks.data.Event;
import com.metropolis.stocks.strategy.Strategy;

import java.util.List;

public interface EventProcessor {

    <T extends Strategy> void processEvent(final Event event, final List<T> strategies);

}
