package com.metropolis.util;

public class MathUtil {

    public static final float decimalIncrease(final float oldNum, final float newNum) {
        float increase = newNum - oldNum;
        return increase / oldNum;
    }


    public static final float decimalDecrease(final float oldNum, final float newNum) {
        float decrease = oldNum - newNum;
        return decrease / oldNum;
    }
}
