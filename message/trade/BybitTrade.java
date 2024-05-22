package com.example.message.trade;

import org.springframework.util.Assert;

import jakarta.json.JsonObject;

import com.example.json.Json;


public class BybitTrade extends Trade {
    // T - timesstamp
    // s - symbol
    // S - Side of taker
    // v - trade size
    // p - trade price
    // L - price change direction
    // i - trade id
    // BT - isBlockTrade

    private String takerSide;
    private String priceChangeDirection;
    private boolean isBlockTrade;


    public BybitTrade(String data) {
        JsonObject jsonObject = Json.newJsonObject(data);
        // bybit payload [data] does not include event name
        this.name = "BybitTrade";
        this.timestamp = jsonObject.getJsonNumber("T").longValue();
        this.symbol = jsonObject.getString("s");
        this.size = jsonObject.getString("v");
        this.price = jsonObject.getString("p");
        this.id = jsonObject.getJsonNumber("i").intValue();
        this.takerSide = jsonObject.getString("S");
        this.priceChangeDirection = jsonObject.getString("L");
        this.isBlockTrade = jsonObject.getBoolean("BT");
    }

    public String getTakerSide() {
        return takerSide;
    }

    public String getPriceChangeDirection() {
        return priceChangeDirection;
    }

    public boolean isBlockTrade() {
        return isBlockTrade;
    }
}