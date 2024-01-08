package com.example.websocket;

import java.net.URL;
import java.util.HashSet;

import org.springframework.util.Assert;

import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Exchange;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Network;
import com.example.util.Utils;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Channel;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Market;


final class KrakenWebSocketClient extends AbstractWebSocketClient {  

    public KrakenWebSocketClient(BuilderImpl builder, WebSocketClientFacade facade) {
        this.name = "Kraken";
        this.protocol = Utils.getExchangeProtocol(this.name);
        this.mainnetHost = Utils.getExchangeMainnetHost(this.name);
        this.testnetHost = Utils.getExchangeTestnetHost(this.name);
        this.version = Utils.getExchangeVersion(this.name);
        this.facade = facade;
        this.url = constructURL(builder);
    }

    private URL constructURL(BuilderImpl builder) {
        // Bybit asks clients to specify the public or private channel, and spot, inverse, linear
        // or option market. Then connect to websocket URL, and subscribe to streams applicable
        // for the given channel
        // Bybit supports testnet websocket support too

        URL url = null;
        Network network = builder.network();
        Market market = builder.market();
        String host;
        String file;
        host = (network == Network.MAINNET) ?  this.mainnetHost: this.testnetHost;

        switch (market) {
            case SPOT:
                file = String.format("/%s/public/spot", this.version);
                break;

            case INVERSE:
                file = String.format("/%s/public/inverse", this.version);
                break;

            case LINEAR:
                file = String.format("/%s/public/linear", this.version);
                break;

            case OPTION:
                file = String.format("/%s/public/option", this.version);
                break;
            
            default:
                file = null;
                break;
        }
        Assert.notNull(file, "The path cannot be resolved\n");
        try {
            url = new URL(this.protocol, host, file);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        Assert.notNull(url, "Url cannot be resolved\n");
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