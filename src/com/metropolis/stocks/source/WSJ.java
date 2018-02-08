package com.metropolis.stocks.source;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.metropolis.stocks.data.Criteria;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.Market;
import com.metropolis.util.Webs;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
public class WSJ {

    private static final ImmutableMap<String, ImmutableMap<String, String>> URL_MAP = ImmutableMap.of(
            "gainer", ImmutableMap.of("NYSE", "http://online.wsj.com/mdc/public/page/2_3021-gainnyse-gainer",
                                      "NASDAQ", "http://online.wsj.com/mdc/public/page/2_3021-gainnnm-gainer",
                                      "ARCA", "http://online.wsj.com/mdc/public/page/2_3021-gainarca-gainer"),

            "loser", ImmutableMap.of("NYSE", "http://online.wsj.com/mdc/public/page/2_3021-losenyse-loser",
                                     "NASDAQ", "http://online.wsj.com/mdc/public/page/2_3021-losennm-loser",
                                     "ARCA", "http://online.wsj.com/mdc/public/page/2_3021-losearca-loser"),

            "volume", ImmutableMap.of("NYSE", "http://online.wsj.com/mdc/public/page/2_3021-volpctnyse-volumes",
                                      "NASDAQ", "http://online.wsj.com/mdc/public/page/2_3021-volpctnnm-volumes",
                                      "ARCA", "http://online.wsj.com/mdc/public/page/2_3021-volpctarca-volumes"));

    private static final LoadingCache<URL, List<String>> symbolCache =
            CacheBuilder.newBuilder()
                        .build(new CacheLoader<URL, List<String>>() {
                            public List<String> load(final URL url) {
                                String pageContents = null;
                                try {
                                    pageContents = Webs.getWebPage(url);
                                } catch (IOException e) {
                                    log.warn("IOException retrieving trackedSymbols! {}", url);
                                }
                                return parseSymbolsFromPage(pageContents);
                            }
                        });

    private static Map<String, Float> maximumSeenPrice = new HashMap<>();
    private static final LoadingCache<URL, Map<String, Float>> quotesCache =
            CacheBuilder.newBuilder()
                        .build(new CacheLoader<URL, Map<String, Float>>() {
                            public Map<String, Float> load(final URL url) {
                                String pageContents = null;
                                try {
                                    pageContents = Webs.getWebPage(url);
                                } catch (IOException e) {
                                    log.warn("IOException retrieving trackedSymbols! {}", url);
                                }
                                final Map<String, Float> quotes = parseQuotesFromPage(pageContents);
                                for (final Map.Entry<String, Float> entry : quotes.entrySet()) {
                                    if (!maximumSeenPrice.containsKey(entry.getKey())) {
                                        maximumSeenPrice.put(entry.getKey(), entry.getValue());
                                        continue;
                                    }
                                    if (maximumSeenPrice.get(entry.getKey()) < entry.getValue()) {
                                        maximumSeenPrice.put(entry.getKey(), entry.getValue());
                                    }

                                }
                                return quotes;
                            }
                        });

    private static Optional<URL> constructURL(final String criteria, final String market, final DateTime dateTime) {
        String dateStr = String.format("%s%02d%02d",
                                       dateTime.year().get(), dateTime.monthOfYear().get(), dateTime.dayOfMonth().get());
        try {
            return Optional.of(new URL(URL_MAP.get(criteria).get(market) + "-" + dateStr + ".html"));
        } catch (MalformedURLException e) {
            log.warn("Exception constructing URL! {} {} {}", criteria, market, dateStr);
        }
        return Optional.empty();
    }

    private static List<String> parseSymbolsFromPage(final String pageContents) {
        List<String> symbolsOnPage = new ArrayList<>();

        final String[] lines = pageContents.split("\n");
        for (final String line : lines) {
            if (line.contains("symbol=")) {
                String symbol = line.split("=")[2].split("\"")[0];
                symbolsOnPage.add(symbol);
            }
        }

        return symbolsOnPage;
    }

    private static Map<String, Float> parseQuotesFromPage(final String pageContents) {
        Map<String, Float> quotesOnPage = new HashMap<>();

        final String[] lines = pageContents.split("\n");

        String lastSymbol = null;
        for (final String line : lines) {
            if (line.contains("symbol=")) {
                String symbol = line.split("=")[2].split("\"")[0];
                lastSymbol = symbol;
            }
            if (line.contains("td class=")) {
                if (lastSymbol == null) {
                    continue;
                }
                String[] entries = line.split("<|>");
                String price = entries[2];
                if (price.charAt(0) == '$') {
                    price = price.substring(1);
                }
                price = price.replace(",", "");
                quotesOnPage.put(lastSymbol, Float.valueOf(price));
                lastSymbol = null;
            }
        }

        return quotesOnPage;
    }

    public static List<Symbol> getSymbols(final Market.MARKET market, final Criteria criteria, final DateTime date) {
        List<Symbol> symbolsOnPage = new ArrayList<>();

        Optional<URL> url = constructURL(criteria.toString(), market.getText(), date);

        if (!url.isPresent()) {
            return symbolsOnPage;
        }

        try {
            quotesCache.get(url.get());
        } catch (ExecutionException e) {
            log.warn("{}", e);
        }

        List<String> rawSymbols = new ArrayList<>();

        try {
            rawSymbols = symbolCache.get(url.get());
        } catch (ExecutionException e) {
            log.warn("{}", e);
        }

        for (final String ticker : rawSymbols) {
            symbolsOnPage.add(new Symbol(ticker, market.getText()));
        }

        return symbolsOnPage;
    }

    public static Float getMaxPrice(final String symbol) {
        if (maximumSeenPrice.containsKey(symbol)) {
            return maximumSeenPrice.get(symbol);
        }

        double averagePrice = maximumSeenPrice.values().stream().mapToDouble(Float::doubleValue).average().orElse(10d);
        return (float) averagePrice;
    }

}
