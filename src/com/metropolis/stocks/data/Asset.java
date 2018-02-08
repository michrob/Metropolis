package com.metropolis.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@AllArgsConstructor
public class Asset {
    private Symbol symbol;
    private int shares;
    private DateTime buyDate;
    private float pricePerShare;

    public void add(final int shares) {
        this.shares += shares;
    }

    public void subtract(final int shares) {
        this.shares -= shares;
    }
}
