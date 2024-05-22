package com.example.message.trade;


import org.springframework.util.Assert;

import jakarta.json.JsonObject;

import com.example.json.Json;

public class BinanceTrade extends Trade {
    private final long eventTimestamp;
    private final int buyerOrderId;
    private final int sellerOrderId;
    private final boolean isMarketMaker;
    private final boolean ignore;

    /*
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
  */

    public BinanceTrade(String payload) {
        JsonObject jsonObject = Json.newJsonObject(payload);
        Assert.state(jsonObject.getString("e") == "trade", "The event is not binance trade");
        this.name = "BinanceTrade";
        this.timestamp = jsonObject.getJsonNumber("T").longValue();
        this.symbol = jsonObject.getString("s");
        this.size = jsonObject.getString("q");
        this.price = jsonObject.getString("p");
        this.id = jsonObject.getJsonNumber("t").intValue();
        this.eventTimestamp = jsonObject.getJsonNumber("E").longValue();
        this.buyerOrderId = jsonObject.getJsonNumber("b").intValue();
        this.sellerOrderId = jsonObject.getJsonNumber("a").intValue();
        this.isMarketMaker = jsonObject.getBoolean("m");
        this.ignore = jsonObject.getBoolean("M"); 

    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public int getBuyerOrderId() {
        return buyerOrderId;
    }

    public int getSellerOrderId() {
        return sellerOrderId;
    }

    public boolean isMarketMaker() {
        return isMarketMaker;
    }

    public boolean ignore() {
        return ignore;
    }
}