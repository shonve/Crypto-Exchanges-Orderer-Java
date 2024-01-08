package com.example.message;

import java.nio.ByteBuffer;
import java.util.Set;

public final class Subscribe {
    private static CharSequence binanceSubscribe(Set<String> params) {
        ByteBuffer result = Message.binanceMessage("SUBSCRIBE", params);
        return result.asCharBuffer();
    }

    private static CharSequence bybitSubscribe(Set<String> args) {
        ByteBuffer result = Message.bybitMessage("subscribe", args);
        return result.asCharBuffer();
    }

    private static CharSequence coinbaseSubscribe(Set<String> params) {
        return null;
    }

    private static CharSequence krakenSubscribe(Set<String> params) {
        return null;
    }


    public static CharSequence of(String exchange, Set<String> params) {
        CharSequence message;
        switch (exchange) {
            case "Binance":
                message = binanceSubscribe(params);
                break;
            
            case "Bybit":
                message = bybitSubscribe(params);
                break;
            
            case "Coinbase":
                message = coinbaseSubscribe(params);
                break;
            
            case "Kraken":
                message = krakenSubscribe(params);
                break;
            
            default:
                message = null;
                break;
        }
        return message;
    }


}