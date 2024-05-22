package com.example.binance;


import java.io.FileInputStream;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import com.example.exchange.ExchangeJson;
import com.example.json.Json;


final class BinanceJson extends ExchangeJson {    
    // use absolute path to binance dir to access some testing functions such as get24hPercentageSupportedSymbols
    private static final String baseDir = "";
    public BinanceJson() {
        super(baseDir);
    }

    public String[] get24hPercentageSupportedSymbols(float threshold, float max_threshold) {
        String[] symbols;
        try(FileInputStream in = new FileInputStream(String.format("%s/publicInfo/tickers.txt", baseDir))) {
            JsonArray jsonArray = Json.newJsonArray(in);
            symbols = get24hPercentageSupportedSymbols(threshold, max_threshold, jsonArray);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return symbols;
    }

    public String getSymbol(JsonObject jsonTicker) {
        String symbol = "";
        try {
            symbol = jsonTicker.getString("symbol");
        }
        catch(Exception e) {
            e.printStackTrace();
            return "";
        }
        return symbol;
    }

    public float get24HChange(JsonObject jsonTicker) {
        float change = 0;
        try {
            //float high = Float.parseFloat(jsonTicker.getString("highPrice"));
            //float low = Float.parseFloat(jsonTicker.getString("lowPrice"));
            //change = (high-low)/low;
            change = Float.parseFloat(jsonTicker.getString("priceChangePercent"))/100;
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return change;
    }

    public float getLastPrice(JsonObject jsonTicker) {
        float change = 0;
        try {
            change = Float.parseFloat(jsonTicker.getString("lastPrice"));
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return change;
    }

    public float getVolume(JsonObject jsonTicker) {
        float volume = 0;
        try {
            volume = Float.parseFloat(jsonTicker.getString("volume"));
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return volume;
    }

}