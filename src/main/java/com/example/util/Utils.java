package com.example.util;

import java.util.HashSet;

import org.springframework.util.Assert;

public final class Utils {  
    private static final HashSet<String> EXCHANGES_SUPPORTED = new HashSet<>(Common.EXCHANGES);

    public static boolean isExchangeSupported(String exchange) {
        return EXCHANGES_SUPPORTED.contains(exchange);
    }  

    public static String getEndPoint(String exchange, String name) {
        Assert.state(EXCHANGES_SUPPORTED.contains(exchange), String.format("Exchange %s is not supported\n", exchange));
        return Common.getEndPoint(exchange, name);
    }
}