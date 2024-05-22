package com.example.exchange;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.json.Json;

import jakarta.json.JsonValue;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;


public abstract class ExchangeJson {
    private final String baseDir;
    private final ZonedDateTime dateTime = ZonedDateTime.now();
    
    public ExchangeJson(String baseDir) {
        this.baseDir = baseDir;
    }

    public String[] getAllSymbols() {
        ArrayList<String> symbols = new ArrayList<>();
        try(FileInputStream in = new FileInputStream(String.format("%s/publicInfo/symbols.txt", baseDir))) {
            JsonObject jsonObject = Json.newJsonObject(in);
            JsonArray symbolsJsonArray = jsonObject.getJsonArray("symbols");
            symbolsJsonArray.forEach(symbol -> {
                symbols.add(symbol.toString().replace("\"", ""));
            });
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return symbols.toArray(new String[0]);
    }

    public void updatePrices(Map<String, JsonObject> tickers, String date) {
        Map<String, JsonObject> currentPrices = getPrices(date);
        if (currentPrices == null) {
            return;
        }
        String currentTime = String.format("%.11s", dateTime.toLocalTime().toString());
        try {
            for(Map.Entry<String, JsonObject> entry: tickers.entrySet()) {
                String symbol = entry.getKey();
                JsonObject ticker = entry.getValue();
                if (!symbol.equals(getSymbol(ticker)) || !currentPrices.containsKey(symbol)) {
                    continue;
                }
                float lastPrice = getLastPrice(ticker);
                JsonArray prices = currentPrices.get(symbol).getJsonArray("prices");
                float lastRecordedPrice = Float.valueOf(prices.getJsonObject(prices.size() - 1).getJsonNumber("price").toString());
                float change = (lastPrice-lastRecordedPrice)/lastRecordedPrice;
                JsonObjectBuilder jsonObjectBuilder = Json.newJsonObjectBuilder();
                JsonArrayBuilder jsonArrayBuilder = Json.newJsonArrayBuilder();
                for(int j = 0; j < prices.size(); j ++) {
                    jsonArrayBuilder.add(prices.getJsonObject(j));
                }
                jsonArrayBuilder.add(Json.newJsonObjectBuilder()
                .add("price", lastPrice)
                .add("time", currentTime)
                .add("change", change)
                .build());
                currentPrices.replace(symbol, 
                    jsonObjectBuilder
                    .add("symbol", symbol)
                    .add("prices", jsonArrayBuilder.build())
                    .build()
                );
            }

            insertPrices(currentPrices, date);
        } 
        catch(Exception e) {
            e.printStackTrace();
        }      
    }

    public String[] getSymbols(String date) {
        List<String> symbols = new ArrayList<>();
        String filename = String.format("%s/change/%s/change.txt", baseDir, date);
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = null;
            for(;;) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                JsonObject jsonObject = Json.newJsonObject(line);
                symbols.add(jsonObject.getString("symbol"));
            }
        }
        catch(Exception e) {
            //e.printStackTrace();  ignore
            return null;
        }
        return symbols.toArray(new String[0]);
    }

    public Map<String, JsonObject> getPrices(String date) {
        Map<String, JsonObject> changes = new HashMap<>();
        String filename = String.format("%s/change/%s/change.txt", baseDir, date);
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = null;
            for(;;) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                JsonObject jsonObject = Json.newJsonObject(line);
                changes.put(jsonObject.getString("symbol"), jsonObject);
            }
        }
        catch(Exception e) {
            //e.printStackTrace();  ignore
            return null;
        }
        return changes;
    }

    public boolean insertPrices(Map<String, JsonObject> prices, String date) {
        //ZonedDateTime dateTime = ZonedDateTime.now();
        String filename = String.format("%s/change/%s/change.txt", baseDir, date);
        try(FileOutputStream file = new FileOutputStream(filename, false)) {
            for (Map.Entry<String, JsonObject> entry: prices.entrySet()) {
                file.write(entry.getValue().toString().getBytes());
                file.write("\r\n".getBytes());
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertAllPrices(String date, String time, JsonObject prices) {
        String filename = String.format("%s/prices/prices.txt", baseDir);
        try(FileOutputStream file = new FileOutputStream(filename, true)) {
            String result = Json.newJsonObjectBuilder().add("date", date).add("time", time).add("prices", prices).build().toString();
            file.write(result.getBytes());
            file.write("\r\n".getBytes());
        }
        catch(Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    public String[] get24hPercentageSupportedSymbols(float threshold, float max_threshold, JsonArray list) {
        String date = dateTime.toLocalDate().toString();
        Map<String, JsonObject> currentPrices = getPrices(date);
        ZonedDateTime dateTime = ZonedDateTime.now();
        String currentTime = String.format("%.11s", dateTime.toLocalTime().toString());
        ArrayList<String> symbols = new ArrayList<>();
        JsonObjectBuilder pricesBuilder = Json.newJsonObjectBuilder();
        try {
            for (int i = 0; i < list.size(); i ++) {
                JsonObject jsonTicker = list.getJsonObject(i);
                String symbol = getSymbol(jsonTicker);
                float lastPrice = getLastPrice(jsonTicker);
                pricesBuilder.add(symbol, Float.valueOf(lastPrice));
                float volume = getVolume(jsonTicker);
                float change24h = get24HChange(jsonTicker);
                float minChange = (float)-1.0;
                float minVolume = (float)2000000;
                float maxVolume = (float)10000000;
                if (change24h >= minChange && (volume > minVolume && volume < maxVolume) ) {
                    if (currentPrices.containsKey(symbol)) {
                        JsonArray prices = currentPrices.get(symbol).getJsonArray("prices");
                        float lastRecordedPrice = Float.valueOf(prices.getJsonObject(prices.size() - 1).getJsonNumber("price").toString());
                        float change = (lastPrice-lastRecordedPrice)/lastRecordedPrice;
                        JsonObjectBuilder jsonObjectBuilder = Json.newJsonObjectBuilder();
                        JsonArrayBuilder jsonArrayBuilder = Json.newJsonArrayBuilder();
                        for(int j = 0; j < prices.size(); j ++) {
                            jsonArrayBuilder.add(prices.getJsonObject(j));
                        }
                        jsonArrayBuilder.add(Json.newJsonObjectBuilder()
                        .add("price", lastPrice)
                        .add("time", currentTime)
                        .add("change", change)
                        .add("volume", volume)
                        .build());
                        currentPrices.replace(symbol, 
                            jsonObjectBuilder
                            .add("symbol", symbol)
                            .add("prices", jsonArrayBuilder.build())
                            .build()
                        );
                    } else {
                        JsonObjectBuilder jsonObjectBuilder = Json.newJsonObjectBuilder();
                        JsonArrayBuilder jsonArrayBuilder = Json.newJsonArrayBuilder();
                        jsonArrayBuilder.add(Json.newJsonObjectBuilder()
                        .add("price", lastPrice)
                        .add("time", currentTime)
                        .add("min-threshold", threshold)
                        .add("max-threshold", max_threshold)
                        .add("change", change24h)
                        .add("volume", volume)
                        );
                        currentPrices.put(symbol, jsonObjectBuilder
                            .add("symbol", symbol)
                            .add("prices", jsonArrayBuilder.build())
                            .build());
                    }
                    symbols.add(symbol);
                }
            }
            insertAllPrices(date, currentTime, pricesBuilder.build());
            insertPrices(currentPrices, date);
        } 
        catch(Exception e) {
            e.printStackTrace();
        }       

        return symbols.toArray(new String[0]);

    }

    public void buildSymbols(JsonArray tickers) {
        String filename = String.format("%s/publicInfo/symbols.txt", baseDir);
        try(FileOutputStream file = new FileOutputStream(filename, false)) {
            JsonObjectBuilder jsonObjectBuilder = Json.newJsonObjectBuilder();
            JsonArrayBuilder jsonArrayBuilder = Json.newJsonArrayBuilder();
            for(int i = 0; i < tickers.size(); i ++) {
                JsonObject jsonTicker = tickers.getJsonObject(i);
                String symbol = getRawSymbol(jsonTicker);
                jsonArrayBuilder.add(symbol);
            }
            String symbols = jsonObjectBuilder.add("symbols", jsonArrayBuilder.build()).build().toString();
            file.write(symbols.getBytes());
            file.write("\r\n".getBytes());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject jsonObject(String line) {
        return Json.newJsonObject(line);
    }

    public JsonArray jsonArray(String line) {
        return Json.newJsonArray(line);
    }

    public String getRawSymbol(JsonObject jsonTicker) {
        return "";
    }

    public String getSymbol(String rawSymbol) {
        return rawSymbol;
    }

    public Map<String, String> toRaw(String[] symbols) {
        if (symbols == null) {
            return null;
        }
        Map<String, String> rawSymbols = new HashMap<>();
        for(String symbol: symbols) {
            rawSymbols.put(symbol, "");
        }
        JsonArray allRawSymbols = getRawSymbols();
        if (allRawSymbols == null) {
            return null;
        }
        allRawSymbols.forEach(rawSymbol -> {
            String symbol = getSymbol(rawSymbol.toString());
            if (rawSymbols.containsKey(symbol)) {
                rawSymbols.replace(symbol, rawSymbol.toString().replace("\"", ""));
            }
        });
        rawSymbols.forEach((symbol, rawSymbol) -> {
            System.out.printf("%s, %s\n", symbol, rawSymbol);
        });

        return rawSymbols;
    }

    private JsonArray getRawSymbols() {
        String filename = String.format("%s/publicInfo/symbols.txt", baseDir);
        JsonArray rawSymbols = null;
        try(FileInputStream file = new FileInputStream(filename)) {
            rawSymbols = Json.newJsonObject(file).getJsonArray("symbols");
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return rawSymbols;
    }

    public abstract String getSymbol(JsonObject jsonTicker);

    public abstract float get24HChange(JsonObject jsonTicker);

    public abstract float getLastPrice(JsonObject jsonTicker);

    public abstract float getVolume(JsonObject jsonTicker);


    public String getJson(Map<String, String> entries) {
        JsonObjectBuilder builder = Json.newJsonObjectBuilder();
        for(Map.Entry<String, String> entry: entries.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build().toString();
    }

    public float getLastPrice(String tickerString) {
        JsonObject jsonTicker = Json.newJsonObject(tickerString);
        return getLastPrice(jsonTicker);
    }

}