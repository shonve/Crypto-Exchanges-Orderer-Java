package com.example.binance;


import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;

import javax.crypto.Mac;

import org.springframework.util.Assert;

import com.example.codec.Hex;
import com.example.exchange.Exchange;

import jakarta.json.JsonObject;


public class Binance extends Exchange {
    private final String SIGNATURE_ALGORITHM = "Hmacsha256";
    private final String apiKey;
    private final String secretKey;
    private final Mac mac;

    private final BinanceJson binanceJson = new BinanceJson();

    public Binance(String apiKey, String secretKey) {
        super("binance", "https", "api.binance.com", -1);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.mac = getMac(this.SIGNATURE_ALGORITHM, this.secretKey);
        Assert.notNull(mac, "Mac must not be null");
    }

    public String coinInfo(String coin) {
        String query = String.format("coin=%s", coin);
        return tokenInfo(query);
    }

    public String ticker(String symbol) {
        String query = String.format("symbol=%s", symbol);
        return tickerInfo(query);
    }

    public String tickers(String symbols) {
        String query = String.format("symbols=%s", symbols);
        return tickerInfo(query);
    }

    private Builder request(URI uri, String signature, String timestamp) {
        Builder builder = HttpRequest.newBuilder();
        builder.uri(uri);
        builder.version(HttpClient.Version.HTTP_1_1);
        return builder;
    }

    private HttpRequest buildRequest(String method, URI uri, String queryString, String jsonBody) {
        if (!(method == "GET" || method == "POST")) {
            return null;
        }
        Builder builder = request(uri, "", "");
        if (method == "GET") {
            builder.GET();
        } else {
            builder.POST(BodyPublishers.ofString(jsonBody));
        }
        return builder.build();
    }

    public HttpRequest buildGETRequest(URI uri, String queryString, int timeout) {
        return buildRequest("GET", uri, queryString, "");
    }

    public HttpRequest buildPOSTRequest(URI uri, String path, String bodyJson, int timeout) {
        return buildRequest("POST", uri, path, bodyJson);        
    }

    public HttpRequest buildDELETERequest(URI uri, String queryString, int timeout) {
        return buildRequest("DELETE", uri, queryString, "");        
    }


    private String sign(String message) {
        return Hex.encode(mac.doFinal(message.getBytes()));
    }

    public String[] get24hPercentageAllSymbols(float threshold, float max_threshold) {
        String[] symbols;
        try {
            symbols = binanceJson.get24hPercentageSupportedSymbols(threshold, max_threshold);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return symbols;
    }

    public void updatePrices(String date) {
        String[] symbols = binanceJson.getSymbols(date);
        if (symbols == null) {
            return;
        }
        Map<String, JsonObject> tickers = new HashMap<>();
        for(String symbol: symbols) {
            String ticker = ticker(symbol);
            if (ticker == "") {
                continue;
            }
            tickers.put(symbol, binanceJson.jsonObject(ticker));
        }
        binanceJson.updatePrices(tickers, date);
    }

    public String getResult(String response) {
        return "";
    }

}