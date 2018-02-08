package com.metropolis.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Symbol implements Serializable {

    private String ticker;
    private String market;

    @Override
    public String toString() {
        return market + ":" + ticker;
    }
}
