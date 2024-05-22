package com.example.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

public final class Subscribe {
    private static ByteBuffer binanceSubscribe(Set<String> params) throws IOException {
        ByteBuffer result = Message.binanceMessage("SUBSCRIBE", params);
        return result;
        //return new String(result.array());
    }

    private static ByteBuffer bybitSubscribe(Set<String> args) throws IOException {
        ByteBuffer result = Message.bybitMessage("subscribe", args);
        return result;
        //return new String(result.array());
    }

    private static ByteBuffer coinbaseSubscribe(Set<String> params) {
        return null;
    }

    private static ByteBuffer krakenSubscribe(Set<String> params) {
        return null;
    }


    public static ByteBuffer of(String exchange, Set<String> params) throws IOException {
        ByteBuffer message = null;
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