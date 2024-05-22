package com.example.bitget;


import java.io.FileInputStream;
import java.io.PrintStream;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import com.example.exchange.ExchangeJson;
import com.example.json.Json;


final class BitgetJson extends ExchangeJson {
    private final static PrintStream out = System.out;
     // use absolute path to binance dir to access some testing functions such as get24hPercentageSupportedSymbols
    private static final String baseDir = "";
    
    public BitgetJson() {
        super(baseDir);
    }

    public String[] get24hPercentageSupportedSymbols(float threshold, float max_threshold) {
        String[] symbols;
        try(FileInputStream in = new FileInputStream(String.format("%s/publicInfo/tickers.txt", baseDir))) {
            JsonArray jsonArray = Json.newJsonObject(in).getJsonArray("data");
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
            float high = Float.parseFloat(jsonTicker.getString("high24h"));
            float low = Float.parseFloat(jsonTicker.getString("low24h"));
            change = (high-low)/low;
            //change = Float.parseFloat(jsonTicker.getString("change24h"));
            //System.out.printf("change: %s\n", change);
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return change;
    }

    public float getLastPrice(JsonObject jsonTicker) {
        float price = 0;
        try {
            price = Float.parseFloat(jsonTicker.getString("lastPr"));
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return price;
    }

    public float getVolume(JsonObject jsonTicker) {
        float volume = 0;
        try {
            volume = Float.parseFloat(jsonTicker.getString("usdtVolume"));
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return volume;
    }

}