package com.example.gateio;


import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;

import javax.crypto.Mac;

import org.springframework.util.Assert;

import com.example.codec.Digest;
import com.example.codec.Hex;
import com.example.exchange.Exchange;
import com.example.json.Json;

import jakarta.json.JsonObject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObjectBuilder;


public class Gateio extends Exchange {
    private final String SIGNATURE_ALGORITHM = "Hmacsha512";
    private final String apiKey;
    private final String secretKey;
    private final Mac mac;
    private final GateioJson gateioJson = new GateioJson();

    public Gateio(String apiKey, String secretKey) {
        super("gateio", "https", "api.gateio.ws", -1);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.mac = getMac(this.SIGNATURE_ALGORITHM, this.secretKey);
        Assert.notNull(mac, "Mac must not be null");
    }

    public String currencyInfo(String currency) {
        // timestamp + method + requestPath + queryString(or body if method is POST)
        String pathParams = String.format("/%s", currency);
        return tokenInfo(pathParams, "");
    }

    public String ticker(String currencyPair) {
        //currencyPair = gateioJson.getRawSymbol(currencyPair);
        String query = String.format("currency_pair=%s", currencyPair);
        String response = tickerInfo(query);
        System.out.printf("%s\n", response);
        return gateioJson.getTicker(response, currencyPair);
    }

    public String balance(String coin) {
        String query = String.format("currency=%s", coin);
        String response = walletBalance(query);
        return gateioJson.jsonArray(response).getJsonObject(0).getString("available");
    }

    public String getOrder(String basecoin, String quotecoin, String orderId) {
        String currencyPair = basecoin + "_" + quotecoin;
        return getOrder(currencyPair, orderId);
    }
    
    public String getOrder(String currencyPair, String orderId) {
        String query = String.format("curency_pair=%s&orderId=%s", currencyPair, orderId);
        return getOrder(query);
    }

    private Builder request(URI uri, String signature, String timestamp, int timeout) {
        Builder builder = HttpRequest.newBuilder();
        builder.header("KEY", apiKey);
        builder.header("SIGN", signature);
        builder.header("TIMESTAMP", timestamp);
        builder.uri(uri);
        builder.timeout(Duration.ofSeconds(timeout));
        builder.version(HttpClient.Version.HTTP_1_1);
        return builder;
    }

    private HttpRequest buildRequest(String method, URI uri, String queryString, String jsonBody, int timeout) {
        if (!(method == "GET" || method == "POST" || method == "DELETE")) {
            return null;
        }
        String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        //String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timestamp()));
        String message = message(method, jsonBody, queryString, timestamp);
        if (message == "") {
            return null;
        }
        String signature = sign(message);
        if (signature == null) {
            return null;
        }
        Builder builder = request(uri, signature, timestamp, timeout);
        if (method == "GET") {
            builder.GET();
        } else if (method == "POST") {
            builder.header("Accept", "application/json");
            builder.header("Content-Type", "application/json");
            builder.POST(BodyPublishers.ofString(jsonBody));
        } else {
            builder.DELETE();
        }
        return builder.build();
    }    

    public HttpRequest buildGETRequest(URI uri, String queryString, int timeout) {
        return buildRequest("GET", uri, queryString, "", timeout);
    }

    public HttpRequest buildPOSTRequest(URI uri, String path, String bodyJson, int timeout) {
        return buildRequest("POST", uri, path, bodyJson, timeout);        
    }

    public HttpRequest buildDELETERequest(URI uri, String queryString, int timeout) {
        return buildRequest("DELETE", uri, queryString, "", timeout);
    }

    public String[] get24hPercentageAllSymbols(float threshold, float max_threshold) {
        String[] symbols;
        try {
            symbols = gateioJson.get24hPercentageSupportedSymbols(threshold, max_threshold);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return symbols;
    }

    private String message(String method, String payload, String path, String timestamp) {
        String sha512Payload = sha512(payload);
        if (sha512Payload == "") {
            return "";
        }
        if (!(method == "GET" || method == "POST" || method == "DELETE")) {
            return "";
        }
        //System.out.printf("method: %s\npath: %s\npayload: %s\ntimestamp: %s\n", method, path, sha512Payload, timestamp);
        String message = method + "\n" +
                         path + "\n" +
                         sha512Payload + "\n" +
                         timestamp;
        
        return message;
                         
    }

    private String sha512(String payload) {
        byte[] digest = Digest.sha512(payload.getBytes());
        if (digest == null) {
            return "";
        }
        return Hex.encode(digest);
    }

    private String sign(String message) {
        return Hex.encode(mac.doFinal(message.getBytes()));
    }

    public String login(String message) {
        return sign(message);
    }

    public void buildSymbols() {
        gateioJson.buildSymbols();
    }

    public Map<String, String> updatePositionSymbols() {
        return gateioJson.updatePositionSymbols();
    }

    public void updatePrices(String date) {
        String[] symbols = gateioJson.getSymbols(date);
        if (symbols == null) {
            return;
        }
        Map<String, String> rawSymbols = gateioJson.toRaw(symbols);
        Map<String, JsonObject> tickers = new HashMap<>();
        for(Map.Entry<String, String> entry: rawSymbols.entrySet()) {
            //String ticker = "";
            String ticker = ticker(entry.getValue());
            if (ticker == "") {
                continue;
            }
            tickers.put(entry.getKey(), gateioJson.jsonArray(ticker).getJsonObject(0));
        }
        gateioJson.updatePrices(tickers, date);
    }

    public String getResult(String response) {
        return "";
    }

    public String openOrder(String basecoin, String quotecoin, String side, String price, String qty) {
        Map<String, String> body = new HashMap<>();
        String symbol = basecoin + "_" + quotecoin;
        body.put("account", "spot");
        body.put("currency_pair", symbol);
        body.put("side", side);
        body.put("type", "limit");
        body.put("amount", qty);
        body.put("price", price);
        String bodyJson = gateioJson.getJson(body);
        //System.out.printf("body: %s\n", bodyJson);
        return openOrder(bodyJson);
    }

    public String orderId(String response) {
        return gateioJson.orderId(response);
    }

    public String cancelOrder(String currencyPair, String orderId) {
        String query = String.format("currency_pair=%s", currencyPair);
        return cancelOrder("DELETE", "", query, orderId);
    }

    public String closeOrder(String basecoin, String quotecoin, String orderId) {
        String currencyPair = basecoin + "_" + quotecoin;
        String result = cancelOrder(currencyPair, orderId);
        if (result == null) {
            return null;
        }
        //System.out.println(result);
        JsonObject resultJson = Json.newJsonObject(result);
        //if (resultJson.getString("statusCode").equals("200")) {
        //    return true;
        //}
        return resultJson.getString("response");
    }

    public long timestamp() {
        String response = serverTime();
        return Long.valueOf(gateioJson.jsonObject(response).getJsonNumber("server_time").toString());
    }

}