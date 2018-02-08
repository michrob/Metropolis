package com.metropolis.stocks.source;


import com.metropolis.stocks.data.HistoricalStockPrice;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.util.Dates;
import com.metropolis.util.Webs;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Google implements PriceProvider {

    public Google() {
    }

    @Override
    public Map<String, HistoricalStockPrice> getPriceCache(final Symbol symbol,
                                                           final DateTime startDate,
                                                           final DateTime endDate) throws IOException {
        String pageUrl = "https://www.google.com/finance/historical?q=";
        pageUrl += symbol.toString() + "&output=csv&histperiod=daily&startdate=";
        pageUrl += String.format("%s+%d+%d&", startDate.toString("MMM"), startDate.getDayOfMonth(), startDate.getYear());
        pageUrl += "&enddate=" + String.format("%s+%d+%d&", endDate.toString("MMM"), endDate.getDayOfMonth(), endDate.getYear());
        pageUrl += "&output=csv";

        //http://www.google.com/finance/historical?q=GOOG&histperiod=daily&startdate=Apr+1+2011&enddate=Apr+15+2016&output=csv

        Map<String, HistoricalStockPrice> priceMap = new HashMap<>();

        String pageContents = Webs.getWebPage(new URL(pageUrl));

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MMM-yy");

        String[] lines = pageContents.split("\n");
        for (final String line : lines) {
            if (line.length() <= 0) {
                continue;
            }
            if (line.length() > 1 && Character.isLetter(line.charAt(1))) {
                continue;
            }

            String[] values = line.split(",");
            String csv_date = values[0];
            String csv_open = values[1];
            String csv_close = values[4];

            float open_price = 0f;
            float close_price = 0f;

            try {
                open_price = Float.valueOf(csv_open);
                close_price = Float.valueOf(csv_close);
            } catch (Exception ex) {
                continue;
            }

            DateTime newDate = formatter.parseDateTime(csv_date);
            String dateStr = Dates.dateToString(newDate);

            priceMap.put(dateStr, new HistoricalStockPrice(open_price, close_price));
        }

        return priceMap;
    }

    //https://www.google.com/finance/historical?q=NASDAQ:AFFX&output=csv
}
