package com.example.websocket;

import java.net.URL;
import java.util.HashSet;

import org.springframework.util.Assert;

import com.example.message.Ping;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Exchange;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Network;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Channel;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Market;
import com.example.util.Utils;


final class BinanceWebSocketClient extends AbstractWebSocketClient {  
    // base wss end point to receive market data
    // wss://data-stream.binance.vision

    // subscribe to a trade stream
    // symbol@trade -> BND@trade
    /* request = {
        "method": "SUBSCRIBE",
        "params": [
            symbol@trade
        ],
        "id": ${any unique 64-bit in}
    } */
    // btcusd@trade


    // partial order book 
    // symbol@depth<level>@time     (Levels supported: 5, 10, and 20) (time: 1000ms or 100ms)
    // btcusd@depth<10>@1000      
    
    // binance trade
    /*
     * {
        "e": "trade",       // Event type
        "E": 1672515782136, // Event time
        "s": "BNBBTC",      // Symbol
        "t": 12345,         // Trade ID
        "p": "0.001",       // Price
        "q": "100",         // Quantity
        "b": 88,            // Buyer order ID
        "a": 50,            // Seller order ID
        "T": 1672515782136, // Trade time
        "m": true,          // Is the buyer the market maker?
        "M": true           // Ignore
        }
     */

    public BinanceWebSocketClient(BuilderImpl builder, WebSocketClientFacade facade) {
        this.name = "Binance";
        this.protocol = Utils.getExchangeProtocol(this.name);
        this.mainnetHost = Utils.getExchangeMainnetHost(this.name);
        this.testnetHost = Utils.getExchangeTestnetHost(this.name);
        this.version = Utils.getExchangeVersion(this.name);
        this.facade = facade;
        this.url = constructURL(builder);

    }

    private URL constructURL(BuilderImpl builder) {
        // Binance wss url supports SPOT market by default, and subscriptions are used
        // to subscribe to public and private streams.
        URL url = null;
        try {
            url = new URL(this.protocol, this.mainnetHost, this.DEFAULT_PORT, "/");
        } 
        catch(Exception e) {
            e.printStackTrace();
        }
        Assert.notNull(url, "url must not be null\n");
        return url;
    }


    @Override
    public String orderBookStream(String symbol, int level) {
        Assert.state(Utils.isSymbolSupported(symbol), String.format("Symbol %s is not supported\n", symbol));
        Assert.state(Utils.isLevelSupported(level), String.format("Level %d is not supported\n", level));
        return String.format("orderbook.%d.%s", level, symbol);
    }

    @Override
    public String tradeStream(String symbol) {
        Assert.state(Utils.isSymbolSupported(symbol), String.format("Symbol %s is not supported\n", symbol));
        return String.format("publicTrade.%s", symbol);
    }

}