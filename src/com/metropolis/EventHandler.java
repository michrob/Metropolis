package com.metropolis;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.common.collect.ImmutableList;
import com.metropolis.stocks.data.Event;
import com.metropolis.stocks.data.State;
import com.metropolis.stocks.processor.EventProcessor;
import com.metropolis.stocks.processor.impl.DatabaseManager;
import com.metropolis.stocks.processor.impl.StrategyExecutor;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.util.Dates;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class EventHandler implements RequestHandler<SNSEvent, Object> {

    private static List<EventProcessor> eventProcessors = ImmutableList.of(new DatabaseManager(),
                                                                           new StrategyExecutor());

    public static <T extends Strategy> void dispatchEvent(final Event event, final List<T> strategies) {
        if (Dates.isWeekend(State.getInstance().today()) ||
            Dates.isHoliday(State.getInstance().today())) {
            return;
        }

        State.getInstance().setThisEvent(event);
        for (final EventProcessor eventProcessor : eventProcessors) {
            eventProcessor.processEvent(event, strategies);
        }
    }

    public Object handleRequest(final SNSEvent request, final Context context) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Invocation started: " + timeStamp);

        String eventStr = request.getRecords().get(0).getSNS().getMessage();

        context.getLogger().log(eventStr);

        /*
        Event event = Event.fromString(eventStr);
        if (event != null) {
            dispatchEvent(event);
        }
        dispatchEvent(Event.PERSIST);
        */

        timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Invocation completed: " + timeStamp);
        return null;
    }

}
