package com.example.bybit;

import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.exchange.ExchangeJson;
import com.example.json.Json;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;


public final class BybitJson extends ExchangeJson {
    private static final String baseDir = "";

    public BybitJson() {
        super(baseDir);
    }

    public String[] get24hPercentageSupportedSymbols(float threshold, float max_threshold) {
        String[] symbols;
        try(FileInputStream in = new FileInputStream(String.format("%s/publicInfo/tickers.txt", baseDir))) {
            JsonArray jsonArray = Json.newJsonObject(in).getJsonObject("result").getJsonArray("list");
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
            float high = Float.parseFloat(jsonTicker.getString("highPrice24h"));
            float low = Float.parseFloat(jsonTicker.getString("lowPrice24h"));
            change = (high-low)/low;
            //change = Float.parseFloat(jsonTicker.getString("price24hPcnt"));
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
        String volume;
        try {
            volume = jsonTicker.getString("turnover24h");
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
        return Float.parseFloat(volume);
    }

    public String getBalance(String response, String coin) {
        JsonArray coins = Json.newJsonObject(response).getJsonObject("result").getJsonArray("list").getJsonObject(0).getJsonArray("coin");
        for(int i = 0; i < coins.size(); i ++) {
            JsonObject jsonObject = coins.getJsonObject(i);
            if (jsonObject.getString("coin").equals(coin)) {
                return jsonObject.getString("walletBalance");
            }
        }
        return "";
    }

    public String orderId(String response) {
        try {
            String orderId = Json.newJsonObject(response).getJsonObject("result").getString("orderId");
            return orderId;
        }
        catch(Exception e) {
            // ignore
        }
        return null;
    }

    public String getOrderId(String orderString) {
        JsonObject jsonObject = Json.newJsonObject(orderString);
        return jsonObject.getString("orderId");
    }

    public String getOrderStatus(String orderString) {
        JsonObject jsonObject = Json.newJsonObject(orderString);
        return jsonObject.getString("orderStatus");
    }

    public String getOrderQty(String orderString) {
        JsonObject jsonObject = Json.newJsonObject(orderString);
        return jsonObject.getString("qty");
    }

    public String getOrderPrice(String orderString) {
        JsonObject jsonObject = Json.newJsonObject(orderString);
        return jsonObject.getString("price");
    }

    public String getOrder(String response, String orderId) {
        JsonArray data = Json.newJsonObject(response).getJsonObject("result").getJsonArray("list");
        for(int i = 0; i < data.size(); i ++) {
            JsonObject jsonObject = data.getJsonObject(i);
            if (jsonObject.getString("orderId").equals(orderId)) {
                return jsonObject.toString();
            }
        }
        return "";
    }

    public String getTicker(String response, String symbol) {
        JsonArray data = Json.newJsonObject(response).getJsonObject("result").getJsonArray("list");
        for(int i = 0; i < data.size(); i ++) {
            JsonObject jsonObject = data.getJsonObject(i);
            if (jsonObject.getString("symbol").equals(symbol)) {
                return jsonObject.toString();
            }
        }
        return "";
    }

    public String getResult(String response) {
        JsonObject jsonObject = Json.newJsonObject(response).getJsonObject("result");
        return jsonObject.toString();
    }
    
}