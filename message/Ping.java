package com.example.message;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.example.util.Utils;

public final class Ping {

    private static ByteBuffer binancePing() throws IOException {
        return Message.binanceMessage("PING", null);
    }

    private static ByteBuffer bybitPing() throws IOException {
        return Message.bybitMessage("ping", null);
    }

    private static ByteBuffer coinbasePing() {
        return null;
    }

    private static ByteBuffer krakenPing() {
        return null;
    }

    public static ByteBuffer of(String exchange) throws IOException {
        if (!Utils.isExchangeSupported(exchange)) {
            throw new MessageParsingException(String.format("Exchange %s is not supported\n", exchange));
        }
        ByteBuffer message = null;
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