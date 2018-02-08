package com.metropolis.stocks.source;

import com.metropolis.stocks.data.HistoricalStockPrice;
import com.metropolis.stocks.data.Symbol;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Map;

public interface PriceProvider {

    public Map<String, HistoricalStockPrice> getPriceCache(final Symbol symbol,
                                                           final DateTime startDate,
                                                           final DateTime endDate) throws IOException;

}
