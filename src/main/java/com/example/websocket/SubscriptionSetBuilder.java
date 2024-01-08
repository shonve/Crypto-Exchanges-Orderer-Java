package com.example.websocket;

// default interval for order book stream is left upon exchange
// although some exchanges support specifying the interval, but some do not. So, assume default
// interval on exchange side


public interface SubscriptionSetBuilder {

    SubscriptionSetBuilder addOrderBookStream(String symbol, int level);
    SubscriptionSetBuilder  addTradeStream(String symbol);

    SubscriptionSetBuilder  removeOrderBookStream(String symbol, int level);
    SubscriptionSetBuilder  removeTradeStream(String symbol);

}
