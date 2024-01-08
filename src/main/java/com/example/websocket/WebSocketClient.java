package com.example.websocket;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface WebSocketClient {

    public interface WebSocketClientBuilder {

        public enum Exchange {
            BYBIT,
            BINANCE,
            COINBASE,
            KRAKEN;
        }

        public enum Network {
            MAINNET,
            TESTNET;
        }

        public enum Channel {
            PUBLIC;
        }

        public enum Market {
            SPOT,
            INVERSE,
            LINEAR,
            OPTION;
        }


        WebSocketClientBuilder exchange(Exchange exchange);
        WebSocketClientBuilder network(Network network);
        WebSocketClientBuilder channel(Channel channel);
        WebSocketClientBuilder market(Market market);
        WebSocketClient build();

    }

    //public boolean subscribeToOrderBookStream(String symbol, int level);
    //public boolean unSubscribeToOrderBookStream(String symbol, int level);
    //public boolean subscribeToTradeStream(String symbol);
    //public boolean unSubscribeToTradeStream(String symbol);
    
    /*
    public CompletableFuture<WebSocketClient> subscribeToOrderBookStream(String symbol, int level);
    public CompletableFuture<WebSocketClient> unSubscribeToOrderBookStream(String symbol, int level);
    public CompletableFuture<WebSocketClient> subscribeToTradeStream(String symbol);
    public CompletableFuture<WebSocketClient> unSubscribeToTradeStream(String symbol);
    */
}