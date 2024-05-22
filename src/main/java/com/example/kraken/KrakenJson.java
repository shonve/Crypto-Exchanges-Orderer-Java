package com.example.kraken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.Instant;

import com.example.json.Json;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;


final class KrakenJson {
    private static final PrintStream out = System.out;
    private static JsonObject jsonObject;
    private static final String baseDir = "";
    

    private boolean withdrawExists(String coin) {
        String filename = String.format("%s/withdraws/fees.txt", baseDir);
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            for(;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                JsonObject jsonObject = Json.newJsonObject(line);
                if (jsonObject.getString("coin").equals(coin)) {
                    return true;
                }
            }
            return false;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean insertWithdraw(String info) {
        String filename = String.format("%s/withdraws/fees.txt", baseDir);
        try(FileOutputStream file = new FileOutputStream(filename, true)) {
            file.write(info.getBytes());
            file.write("\r\n".getBytes());
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addWithdraw(String coin, String body) {
        if (withdrawExists(coin)) {
            return false;
        }
        JsonArray data = Json.newJsonObject(body).getJsonArray("data");
        if(!data.getJsonObject(0).getString("coin").equals(coin)) {
            return false;
        }
        JsonObjectBuilder objBuilder = Json.newJsonObjectBuilder();
        JsonArrayBuilder arrBuilder = Json.newJsonArrayBuilder();
        objBuilder.add("coin", coin);
        JsonArray chains = data.getJsonObject(0).getJsonArray("chains");
        for(int i = 0; i < chains.size(); i ++) {
            JsonObject info = chains.getJsonObject(i);
            String chain = info.getString("chain");
            String withdrawFee = info.getString("withdrawFee");
            JsonObjectBuilder objectBuilder2 = Json.newJsonObjectBuilder();
            objectBuilder2.add("chain", chain);
            objectBuilder2.add("withdrawFee", withdrawFee);
            arrBuilder.add(objectBuilder2.build());
        }
        objBuilder.add("withdraws", arrBuilder.build());
        String info = objBuilder.build().toString();
        return insertWithdraw(info);
    }
    /*
     * 
     * {"error":[],"result":{"NEARUSD":{"a":["2.97400","1426","1426.000"],"b":["2.97300","806","806.000"],"c":["2.98000","0.15892618"],"v":["166529.58750630","574103.70059128"],"p":["2.97787","2.94811"],"t":[592,1754],"l":["2.95700","2.88000"],"h":["3.00200","3.00200"],"o":"2.96600"}}}
     */
    public boolean insertTicker(String symbol, String body) {
        String filename = String.format("%s/tickers/%s.txt", baseDir, symbol);
        JsonArray data = Json.newJsonObject(body).getJsonObject("result").getJsonObject(symbol).getJsonArray("c");
        String lastPrice = data.getString(0);
        // {"exchange": "Binance", "event": "ticker", "symbol": "NEARUSDT", "last_price": "2.98700000", "timestamp": 1705804135866}
        JsonObjectBuilder objBuilder = Json.newJsonObjectBuilder();
        objBuilder.add("exchange", "Kraken");
        objBuilder.add("event", "ticker");
        objBuilder.add("symbol", symbol);
        objBuilder.add("last_price", lastPrice);
        objBuilder.add("timestamp", Instant.now().toEpochMilli());
        String info = objBuilder.build().toString();
        try(FileOutputStream file = new FileOutputStream(filename, true)) {
            file.write(info.getBytes());
            file.write("\r\n".getBytes());
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}