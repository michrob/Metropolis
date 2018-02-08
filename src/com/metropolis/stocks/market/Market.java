package com.metropolis.stocks.market;

import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.impl.HistoricalMarket;
import com.metropolis.stocks.market.impl.RealTimeMarket;
import com.metropolis.util.Config;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class Market {

    private static Market theInstance;

    public static Market instance() {
        if (theInstance != null) {
            return theInstance;
        }
        if (Config.isProduction()) {
            theInstance = new RealTimeMarket();
        } else {
            theInstance = new HistoricalMarket();
        }
        return theInstance;
    }

    public abstract Map<Symbol, Float> retrieveQuotesForSymbols(final Collection<Symbol> symbols);

    public enum MARKET {

        //ARCA("ARCA"),
        NYSE("NYSE"),
        NASDAQ("NASDAQ");

        @Getter
        private String text;

        MARKET(final String text) {
            this.text = text;
        }

        public static MARKET fromString(final String text) {
            if (text != null) {
                for (final MARKET mark : MARKET.values()) {
                    if (text.equalsIgnoreCase(mark.getText())) {
                        return mark;
                    }
                }
            }
            return null;
        }
    }

}
