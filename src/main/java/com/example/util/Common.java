package com.example.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class Common {
    public static final List<String> EXCHANGES = Arrays.asList(
                                        "Binance",
                                        "Bybit",
                                        "Coinbase",
                                        "Kraken");

    public static final Map<String, ExchangeInfo> exchangesInfo = exchanges();

    public static final List<String> SYMBOLS = Arrays.asList(
                                                "BTCBNB",
                                                "BNBBTC",
                                                "ALGOUSDT",
                                                "ETCUSDT");

    public static final List<Integer> LEVELS = Arrays.asList(1, 5, 10);

    private static Map<String, ExchangeInfo> exchanges() {
        Map<String, ExchangeInfo> exchangesInfo = new HashMap<String, ExchangeInfo>();

        ExchangeInfo binance = ExchangeInfo.newExchangeInfoBuilder()
                                            .protocol("wss")
                                            .mainnetHost("data-stream.binance.vision")
                                            .testnetHost("")
                                            .version("")
                                            .build();

        ExchangeInfo bybit = ExchangeInfo.newExchangeInfoBuilder()
                                          .protocol("wss")
                                          .mainnetHost("stream.bybit.com")
                                          .testnetHost("stream-testnet.bybit.com")
                                          .version("v5")
                                          .build();
        
        ExchangeInfo coinbase = ExchangeInfo.newExchangeInfoBuilder()
                                            .protocol("wss")
                                            .mainnetHost("")
                                            .testnetHost("")
                                            .version("")
                                            .build();
                                        
         ExchangeInfo kraken = ExchangeInfo.newExchangeInfoBuilder()
                                            .protocol("wss")
                                            .mainnetHost("")
                                            .testnetHost("")
                                            .version("")
                                            .build();
            
        exchangesInfo.put("Binance", binance);
        exchangesInfo.put("Bybit", bybit);
        exchangesInfo.put("Coinbase", coinbase);
        exchangesInfo.put("Kraken", kraken);
        return exchangesInfo;
    }

    public static ExchangeInfo getExchangeInfo(String exchange) {
        return exchangesInfo.get(exchange);
    }
}