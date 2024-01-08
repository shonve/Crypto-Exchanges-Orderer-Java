package com.example.websocket;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;

class SubscriptionSetBuilderImpl implements SubscriptionSetBuilder {
    private final Set<String> subscriptions = new HashSet<>();
    private final BiFunction<String, Integer, String> orderBookStreamGenerator;
    private final Function<String, String> tradeStreamGenerator;

    public SubscriptionSetBuilderImpl(
        BiFunction<String, Integer, String> orderBookStreamGenerator,
        Function<String, String> tradeStreamGenerator
        ) {
            this.orderBookStreamGenerator = orderBookStreamGenerator;
            this.tradeStreamGenerator = tradeStreamGenerator;
        }

    @Override
    public SubscriptionSetBuilder addOrderBookStream(String symbol, int level) {
        subscriptions.add(orderBookStreamGenerator.apply(symbol, level));
        return this;
    }

    @Override
    public SubscriptionSetBuilder addTradeStream(String symbol) {
        subscriptions.add(tradeStreamGenerator.apply(symbol));
        return this;
    }

    @Override
    public SubscriptionSetBuilder removeOrderBookStream(String symbol, int level) {
        String subscription = orderBookStreamGenerator.apply(symbol, level);
        if (subscriptions.contains(subscription)) {
            subscriptions.remove(subscription);
        }
        return this;
    }

    @Override
    public SubscriptionSetBuilder removeTradeStream(String symbol) {
        String subscription = tradeStreamGenerator.apply(symbol);
        if (subscriptions.contains(subscription)) {
            subscriptions.remove(subscription);
        }
        return this;
    }

    public SubscriptionSet build() {
        return new SubscriptionSet(
                    Collections.unmodifiableSet(subscriptions),
                    orderBookStreamGenerator,
                    tradeStreamGenerator);
    }

}