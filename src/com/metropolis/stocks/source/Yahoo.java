package com.metropolis.stocks.source;

import com.metropolis.stocks.data.HistoricalStockPrice;
import com.metropolis.stocks.data.State;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.util.Config;
import com.metropolis.util.Webs;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Slf4j
public class Yahoo implements PriceProvider {

    public Yahoo() {
    }

    @Override
    public Map<String, HistoricalStockPrice> getPriceCache(final Symbol symbol,
                                                           final DateTime startDate,
                                                           final DateTime endDate) throws IOException {
        String pageUrl = "http://ichart.finance.yahoo.com/table.csv?ignore=.csv";
        pageUrl += "&a=" + String.valueOf(startDate.getMonthOfYear() - 1) +
                   "&b=" + String.valueOf(startDate.getDayOfMonth()) +
                   "&c=" + String.valueOf(startDate.getYear());

        pageUrl += "&d=" + String.valueOf(endDate.getMonthOfYear() - 1) +
                   "&e=" + String.valueOf(endDate.getDayOfMonth()) +
                   "&f=" + String.valueOf(endDate.getYear());

        pageUrl += "&g=h&s=" + symbol.getTicker();

        Map<String, HistoricalStockPrice> priceMap = new HashMap<>();

        String pageContents = Webs.getWebPage(new URL(pageUrl));

        String[] lines = pageContents.split("\n");
        for (final String line : lines) {
            if (line.length() == 0 || Character.isLetter(line.charAt(0))) {
                continue;
            }
            String[] values = line.split(",");
            String csv_date = values[0];
            String csv_open = values[1];
            String csv_close = values[4];

            float open_price = Float.valueOf(csv_open);
            float close_price = Float.valueOf(csv_close);

            priceMap.put(csv_date, new HistoricalStockPrice(open_price, close_price));
        }

        return priceMap;
    }

}
