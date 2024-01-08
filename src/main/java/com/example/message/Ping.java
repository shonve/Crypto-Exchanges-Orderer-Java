package com.example.message;

import java.nio.ByteBuffer;

import org.springframework.util.Assert;

import com.example.util.Utils;

public final class Ping {

    private static ByteBuffer binancePing() {
        return Message.binanceMessage("PING", null);
    }

    private static ByteBuffer bybitPing() {
        return ByteBuffer.wrap(Message.bybitMessage("ping", null).array());
    }

    private static ByteBuffer coinbasePing() {
        return null;
    }

    private static ByteBuffer krakenPing() {
        return null;
    }

    public static ByteBuffer of(String exchange) {
        Assert.state(Utils.isExchangeSupported(exchange), String.format("Exchange %s is not supported", exchange));
        ByteBuffer message;
        switch(exchange) {
            case "Binance":
                message = binancePing();
                break;

            case "Bybit":
                message = bybitPing();
                break;

            case "Coinbase":
                message = coinbasePing();
                break;

            case "Kraken":
                message = krakenPing();
                break;

            default:
                message = null;
                break;
        }
        return message;
    }

}