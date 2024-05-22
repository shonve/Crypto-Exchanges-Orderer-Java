package com.example.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class Common {
    public static final List<String> EXCHANGES = Arrays.asList(
                                        "binance",
                                        "bitget",
                                        "bitstamp",
                                        "bybit",
                                        "coinbase",
                                        "gateio",
                                        "kraken",
                                        "kucoin",
                                        "mexc",
                                        "okx");

    private static final Map<String, Map<String, String>> endPoints = endPoints();
    
    public static String getEndPoint(String exchange, String name) {
        return endPoints.get(exchange).get(name);
    }

    private static Map<String, Map<String, String>> endPoints() {
        Map<String, Map<String, String>> endPoints = new HashMap<>();
        endPoints.put("bitget", bitgetEndPoints());
        endPoints.put("bybit", bybitEndPoints());
        endPoints.put("bitstamp", bitstampEndPoints());
        endPoints.put("coinbase", coinbaseEndPoints());
        endPoints.put("gateio", gateioEndPoints());
        endPoints.put("kraken", krakenEndPoints());
        endPoints.put("kucoin", kucoinEndPoints());
        endPoints.put("mexc", mexcEndPoints());
        endPoints.put("okx", okxEndPoints());
        endPoints.put("binance", binanceEndPoints());
        return endPoints;
    }

    private static Map<String, String> bybitEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("tickers", "/v5/market/tickers");
        endPoints.put("coinInfo", "/v5/asset/coin/query-info");
        endPoints.put("balance", "/v5/account/wallet-balance");
        endPoints.put("createOrder", "/v5/order/create");
        endPoints.put("cancelOrder", "/v5/order/cancel");
        endPoints.put("getOrders", "/v5/order/realtime");
        endPoints.put("walletBalance", "/v5/account/wallet-balance");
        endPoints.put("coinBalance", "/v5/asset/transfer/query-account-coin-balance");
        endPoints.put("serverTime", "/v5/market/time");
        return endPoints;
    }

    private static Map<String, String> bitgetEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("tickers", "/api/v2/spot/market/tickers");
        endPoints.put("coinInfo", "/api/v2/spot/public/coins");
        return endPoints;
    }

    private static Map<String, String> bitstampEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("withdrawalFeeCoin", "/api/v2/fees/withdrawal");
        endPoints.put("withdrawalFeeAll", "/api/v2/fees/withdrawal");
        return endPoints;
    }
    
    private static Map<String, String> coinbaseEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("products", "/api/v3/brokerage/products");
        //endPoints.put("withdrawalFeeAll", "/api/v2/fees/withdrawal");
        return endPoints;
    }

    private static Map<String, String> gateioEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("currencies", "/api/v4/spot/currencies");
        endPoints.put("tickers", "/api/v4/spot/tickers");
        endPoints.put("walletBalance", "/api/v4/spot/accounts");
        endPoints.put("createOrder", "/api/v4/spot/orders");
        endPoints.put("cancelOrder", "/api/v4/spot/orders");
        endPoints.put("getOrders", "/api/v4/spot/orders");
        endPoints.put("serverTime", "/api/v4/spot/time");
        return endPoints;
    }

    private static Map<String, String> krakenEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("assets", "/0/public/Assets");
        endPoints.put("tickers", "/0/public/Ticker");
        endPoints.put("balance", "/0/private/Balance");
        return endPoints;
    }

    private static Map<String, String> kucoinEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("currencies", "/api/v3/currencies");
        endPoints.put("accountInfo", "/api/v2/user-info");
        return endPoints;
    }

    private static Map<String, String> mexcEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("accountInfo", "/api/v3/account");
        return endPoints;
    }

    private static Map<String, String> okxEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("currencies", "/api/v5/asset/currencies");
        return endPoints;
    }

    private static Map<String, String> binanceEndPoints() {
        Map<String, String> endPoints = new HashMap<>();
        endPoints.put("tickers", "/api/v3/ticker/tradingDay");
        return endPoints;
    }
}