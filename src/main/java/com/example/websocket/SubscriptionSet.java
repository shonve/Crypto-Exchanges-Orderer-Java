package com.example.websocket;

import java.util.Set;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;


final class SubscriptionSet extends HashSet<String> {
    private final HashSet<String> subscriptions;
    private final BiFunction<String, Integer, String> orderBookStreamGenerator;
    private final Function<String, String> tradeStreamGenerator;

    public SubscriptionSet(
        Set<String> immutableSubscriptionSet,
        BiFunction<String, Integer, String> orderBookStreamGenerator,
        Function<String, String> tradeStreamGenerator
        ) {
        subscriptions = (HashSet<String>)immutableSubscriptionSet;
        this.orderBookStreamGenerator = orderBookStreamGenerator;
        this.tradeStreamGenerator = tradeStreamGenerator;
    }

    public String getOrderBookStreamSubscription(String symbol, int level) {
        String subscription = orderBookStreamGenerator.apply(symbol, level);
        if (subscriptions.contains(subscription)) {
            return subscription;
        }
        return null;
    }

    public String getTradeStreamSubscription(String symbol) {
        String subscription = tradeStreamGenerator.apply(symbol);
        if (subscriptions.contains(subscription)) {
            return subscription;
        }
        return null;
    }
}