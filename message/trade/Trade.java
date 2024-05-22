package com.example.message.trade;

abstract public class Trade {
    String name;
    long timestamp;
    String symbol;
    String size;
    String price;
    int id;

    /*
    "e": "trade",       // Event type
    "E": 1672515782136, // Event time
    "s": "BNBBTC",      // Symbol   symbol
    "t": 12345,         // Trade ID id
    "p": "0.001",       // Price    price
    "q": "100",         // Quantity size
    "b": 88,            // Buyer order ID   
    "a": 50,            // Seller order ID
    "T": 1672515782136, // Trade time   timestamp
    "m": true,          // Is the buyer the market maker?
    "M": true           // Ignore 
    */
 
    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSize() {
        return size;
    }

    public String getPrice() {
        return price;
    }

    public int getId() {
        return id;
    }
}