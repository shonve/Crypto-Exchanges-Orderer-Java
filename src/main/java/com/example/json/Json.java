package com.example.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map;

import org.springframework.util.Assert;

import java.io.StringReader;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

import com.example.util.Utils;


public final class Json {
    private static final JsonProvider provider = JsonProvider.provider();
    private JsonObject jsonObject;
    private final String exchange;

    public Json(String exchange) {
        this.exchange = exchange;
    }

    public Map<String, Map<String, String>> withdrawFees() {
        String filename = String.format("./%s/withdraws.txt", exchange);
        Map<String, Map<String, String>> withdrawFees = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            for(;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                JsonObject jsonObject = provider.createReader(new StringReader(line)).readObject();
                String coin = jsonObject.getString("coin");
                withdrawFees.put(jsonObject.getString("coin"), new HashMap<>());
                JsonArray jsonArray = jsonObject.getJsonArray("withdraws");
                for(int i = 0; i < jsonArray.size(); i ++) {
                    JsonObject item = jsonArray.getJsonObject(i);
                    withdrawFees.get(coin).put(item.getString("chain"), item.getString("withdrawFee"));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return withdrawFees;
    }
    
    private void parse(String exchange, CharSequence message) throws IOException {
        if (!Utils.isExchangeSupported(exchange)) {
            throw new IOException(String.format("Exchange %s is not supported"));
        }
        try {
            jsonObject = provider.createReader(new StringReader(message.toString())).readObject();            
        }
        catch(Exception e) {
            throw new JsonParsingException(e);
        }
    }
    
    public String getStreamName(String exchange, CharSequence message) throws IOException {
        String streamName = null;
        try {
            parse(exchange, message);
            switch (exchange) {
                case "Binance":
                    streamName = jsonObject.getString("stream");
                    break;
                
                case "Bybit":
                    streamName = jsonObject.getString("topic");
                    break;
                
                case "Coinbase":
                    break;
                
                case "Kraken":
                    break;
                
                default:
                    break;

            }
        }
        catch(Exception e) {
            throw new JsonParsingException(e);
        }
        return streamName;
    }

    public String getPayload(String exchange, CharSequence message) throws IOException {
        JsonObject payload = null;
        try {
            parse(exchange, message);
            switch (exchange) {
                case "Binance":
                    payload = jsonObject.getJsonObject("payload");
                    break;
                
                case "Bybit":
                    payload = jsonObject.getJsonObject("data");
                    break;
                
                case "Coinbase":
                    break;
                
                case "Kraken":
                    break;
                
                default:
                    break;

            }
        }
        catch(Exception e) {
            throw new JsonParsingException(e);
        }
        return payload.toString();
    }

    public static JsonObject newJsonObject(String json) {
        JsonObject jsonObject = null;
        try {
            jsonObject = provider.createReader(new StringReader(json.toString())).readObject();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public static JsonObject newJsonObject(InputStream in) {
        JsonObject jsonObject = null;
        try {
            jsonObject = provider.createReader(in).readObject();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public static JsonArray newJsonArray(InputStream in) {
        JsonArray jsonArray = null;
        try {
            jsonArray = provider.createReader(in).readArray();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return jsonArray;
    }

    public static JsonArray newJsonArray(String line) {
        JsonArray jsonArray = null;
        try {
            jsonArray = provider.createReader(new StringReader(line.toString())).readArray();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return jsonArray;
    }

    public static JsonObjectBuilder newJsonObjectBuilder() {
        return provider.createObjectBuilder();
    }

    public static JsonArrayBuilder newJsonArrayBuilder() {
        return provider.createArrayBuilder();
    }

}