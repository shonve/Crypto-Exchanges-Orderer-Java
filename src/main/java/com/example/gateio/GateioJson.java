package com.example.gateio;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import com.example.exchange.ExchangeJson;
import com.example.json.Json;


public final class GateioJson extends ExchangeJson {
    private static final String baseDir = "";
    
    public GateioJson() {
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

    public Map<String, String> updatePositionSymbols() {
        Map<String, String> supportedSymbols = new HashMap<>();
        try {
            FileInputStream lastTickersStream = new FileInputStream(String.format("%s/publicInfo/last_tickers.txt", baseDir));
            JsonArray lastTickers = Json.newJsonArray(lastTickersStream);
            Map<String, Float> lastTickersMap = new HashMap<>();
            for(int i = 0; i < lastTickers.size(); i ++) {
                JsonObject ticker = lastTickers.getJsonObject(i);
                String symbol = ticker.getString("currency_pair").replace("_", "");
                Float price = Float.parseFloat(ticker.getString("last"));
                lastTickersMap.put(symbol, price);
            }
            lastTickers = null;
            lastTickersStream.close();
            FileInputStream tickersStream = new FileInputStream(String.format("%s/publicInfo/tickers.txt", baseDir));
            JsonArray tickers = Json.newJsonArray(tickersStream);

            for(int i = 0; i < tickers.size(); i ++) {
                JsonObject ticker = tickers.getJsonObject(i);
                String symbol = ticker.getString("currency_pair").replace("_", "");
                Float price = Float.parseFloat(ticker.getString("last"));
                if (!lastTickersMap.containsKey(symbol)) {
                    continue;
                }
                Float lastPrice = lastTickersMap.get(symbol);
                float change = (price-lastPrice)/lastPrice;
                if (symbol.equals("GALA5SUSDT")) {
                    System.out.printf("symbol: %s, lastPrice: %s, price: %s, change: %s\n", symbol, lastPrice, price, change);
                }
                if (change > 0.1 || change < -0.4) {
                    supportedSymbols.put(symbol, String.valueOf(change));
                }
            }

        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return supportedSymbols;
    }

    public void buildSymbols() {
        try(FileInputStream in = new FileInputStream(String.format("%s/publicInfo/tickers.txt", baseDir))) {
            JsonArray jsonArray = Json.newJsonArray(in);
            buildSymbols(jsonArray);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getRawSymbol(String symbol) {
        String[] symbols = new String[]{symbol};
        return toRaw(symbols).get(symbol);
    }

    @Override
    public String getRawSymbol(JsonObject jsonTicker) {
        String symbol = "";
        try {
            symbol = jsonTicker.getString("currency_pair");
        }
        catch(Exception e) {
            e.printStackTrace();
            return "";
        }
        return symbol;
    }

    public String getSymbol(JsonObject jsonTicker) {
        String symbol = "";
        try {
            symbol = jsonTicker.getString("currency_pair");
        }
        catch(Exception e) {
            e.printStackTrace();
            return "";
        }
        return symbol.replace("_", "");
    }

    public float get24HChange(JsonObject jsonTicker) {
        float change = 0;
        try {
            //float high = Float.parseFloat(jsonTicker.getString("high_24h"));
            //float low = Float.parseFloat(jsonTicker.getString("low_24h"));
            //change = (high-low)/low;
            change = Float.parseFloat(jsonTicker.getString("change_percentage"))/100;

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
            change = Float.parseFloat(jsonTicker.getString("last"));
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
            volume = Float.parseFloat(jsonTicker.getString("quote_volume"));
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return volume;
    }

    public String getTicker(String response, String symbol) {
        JsonArray data = Json.newJsonArray(response);
        for(int i = 0; i < data.size(); i ++) {
            JsonObject jsonObject = data.getJsonObject(i);
            if (jsonObject.getString("currency_pair").equals(symbol)) {
                return jsonObject.toString();
            }
        }
        return "";
    }
    

    @Override
    public String getSymbol(String rawSymbol) {
        return rawSymbol.replace("_", "").replace("\"", "");
    }

    public String orderId(String orderString) {
        try {
            String orderId = Json.newJsonObject(orderString).getString("id");
            return orderId;
        }
        catch(Exception e) {
            // ignore
        }
        return null;
    }

    public String orderStatus(String orderString) {
        return Json.newJsonObject(orderString).getString("status");
    }

}