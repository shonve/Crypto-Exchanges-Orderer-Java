package com.example.websocket;


import org.springframework.util.Assert;

class BuilderImpl implements WebSocketClient.WebSocketClientBuilder {
    private Exchange exchange;
    private Network network;
    private Channel channel;
    private Market market;

    public BuilderImpl exchange(Exchange exchange) {
        boolean status = exchange == Exchange.BYBIT ||
                         exchange == Exchange.BINANCE ||
                         exchange == Exchange.COINBASE ||
                         exchange == Exchange.KRAKEN;
        
        Assert.state(status, "Exchange must be: Bybit, Binance, Coinbase, or Kraken\n");
        this.exchange = exchange;
        return this;
    }

    public BuilderImpl network(Network network) {
        boolean status = network == Network.MAINNET || network == Network.TESTNET;
        Assert.state(status, "Network must be Mainnet or Testnet\n");
        this.network = network;
        return this;
    }

    public BuilderImpl channel(Channel channel) {
        boolean status = channel == Channel.PUBLIC;
        Assert.state(status, "Only public channel is supported");
        this.channel = channel;
        return this;
    }

    public BuilderImpl market(Market market) {
        boolean status = market == Market.SPOT ||
                         market == Market.LINEAR ||
                         market == Market.INVERSE || 
                         market == Market.OPTION;
                    
        Assert.state(status, "Market must be: spot, linear, inverse, or option\n");
        this.market = market;
        return this;    
    }

    public Exchange exchange() {
        return exchange;
    }

    public Network network() {
        return network;
    }

    public Channel channel() {
        return channel;
    }

    public Market market() {
        return market;
    }

    public WebSocketClient build() {
        return WebSocketClientImpl.newWebSocketClient(this);
    }
}