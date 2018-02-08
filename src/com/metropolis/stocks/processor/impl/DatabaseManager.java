package com.metropolis.stocks.processor.impl;

import com.metropolis.stocks.account.Account;
import com.metropolis.stocks.data.Event;
import com.metropolis.stocks.processor.EventProcessor;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.util.Cache;
import com.metropolis.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DatabaseManager implements EventProcessor {

    @Override
    public <T extends Strategy> void processEvent(final Event event, final List<T> strategies) {
        if (event.equals(Event.PERSIST)) {
            for (final Strategy strategy : strategies) {
                Account account = strategy.getAccount();
                Optional<String> jsonInString = JSONUtil.serializeObject(account);
                jsonInString.ifPresent(string -> Cache.putString(strategy.getName(), string));
            }
        }
    }
}
