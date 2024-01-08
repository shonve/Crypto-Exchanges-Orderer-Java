package com.example.util;

import java.util.HashSet;

import org.springframework.util.Assert;

public final class Utils {  
    private static final HashSet<String> EXCHANGES_SUPPORTED = new HashSet<>(Common.EXCHANGES);
    private static final HashSet<String> SYMBOLS_SUPPORTED = new HashSet<>(Common.SYMBOLS);
    private static final HashSet<Integer> LEVELS_SUPPORTED = new HashSet<>(Common.LEVELS);

    public static boolean isExchangeSupported(String exchange) {
        return EXCHANGES_SUPPORTED.contains(exchange);
    }

    public static boolean isSymbolSupported(String symbol) {
        return SYMBOLS_SUPPORTED.contains(symbol);
    }

    public static boolean isLevelSupported(int level) {
        return LEVELS_SUPPORTED.contains(level);
    }

    public static String getExchangeProtocol(String exchange) {
        Assert.state(EXCHANGES_SUPPORTED.contains(exchange), String.format("Exchange %s is not supported\n", exchange));
        return Common.getExchangeInfo(exchange).protocol();
    }

    public static String getExchangeMainnetHost(String exchange) {
        Assert.state(EXCHANGES_SUPPORTED.contains(exchange), String.format("Exchange %s is not supported\n", exchange));
        return Common.getExchangeInfo(exchange).mainnetHost();
    }

    public static String getExchangeTestnetHost(String exchange) {
        Assert.state(EXCHANGES_SUPPORTED.contains(exchange), String.format("Exchange %s is not supported\n", exchange));
        return Common.getExchangeInfo(exchange).testnetHost();
    }

    public static String getExchangeVersion(String exchange) {
        Assert.state(EXCHANGES_SUPPORTED.contains(exchange), String.format("Exchange %s is not supported\n", exchange));
        return Common.getExchangeInfo(exchange).version();
    }
}