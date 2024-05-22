package com.example.bybit;


import java.io.PrintStream;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;

import javax.crypto.Mac;

import org.springframework.util.Assert;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import com.example.codec.Hex;
import com.example.exchange.Exchange;
import com.example.json.Json;


public class Bybit extends Exchange {
    static final PrintStream out = System.out;
    private final String SIGNATURE_ALGORITHM = "Hmacsha256";
    private final String apiKey;
    private final String secretKey;
    private final Mac mac;
    private final BybitJson bybitJson = new BybitJson();

    private final float orderValue = 25;

    public Bybit(String apiKey, String secretKey) {
        super("bybit", "https", "api.bybit.com", -1);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.mac = getMac(this.SIGNATURE_ALGORITHM, this.secretKey);
        Assert.notNull(mac, "Mac must not be null");
    }

    public String coinInfo(String coin) {
        String query = "";
        if (coin != "") {
            query = String.format("coin=%s", coin);
        }
        return tokenInfo(query);
    }

    public String[] get24hPercentageAllSymbols(float threshold, float max_threshold) {
        String[] symbols;
        try {
            symbols = bybitJson.get24hPercentageSupportedSymbols(threshold, max_threshold);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return symbols;
    }

    public String ticker(String category, String symbol) {
        String query = String.format("category=%s&symbol=%s", category, symbol);
        String response = tickerInfo(query);
        return bybitJson.getTicker(response, symbol);
    }

    public String getWalletBalance(String coin) {
        String query = String.format("accountType=%s", "UNIFIED");
        String response = walletBalance(query);
        return bybitJson.getBalance(response, coin);
    }

    public String getCoinBalance(String coin) {
        String query = String.format("accountType=%s&coin=%s", "UNIFIED", coin);
        String response = coinBalance(query);
        return response;
    }

    public String openOrder(String basecoin, String quotecoin, String side, String price, String qty) {
        Map<String, String> body = new HashMap<>();
        String symbol = basecoin + quotecoin;
        body.put("category", "spot");
        body.put("symbol", symbol);
        body.put("side", side);
        body.put("orderType", "Limit");
        if (side.equals("buy")) {
            body.put("quoteCoin", quotecoin);
        } else {
            body.put("baseCoin", basecoin);
        }
        body.put("qty", qty);
        body.put("price", price);
        String bodyJson = bybitJson.getJson(body);
        String response = openOrder(bodyJson);
        if (response != null) {
            JsonObject responseJson = Json.newJsonObject(response);
            if (responseJson.getJsonNumber("retCode").toString().equals("0")) {
                String orderId = responseJson.getJsonObject("result").getString("orderId");
                return getOrder(symbol, orderId);
                /*
                if (orders == null) {
                    return null;
                }
                JsonArray ordersList = Json.newJsonObject(orders).getJsonObject("result").getJsonArray("list");
                for(int i = 0; i < ordersList.size(); i ++) {
                    JsonObject order = ordersList.getJsonObject(i);
                    if (order.getString("orderId").equals(orderId)) {
                        return order.toString();
                    }
                }
                */
            }
        }
        return null;
    }

    public String orderId(String response) {
        return bybitJson.orderId(response);
    }

    public String cancelOrder(String symbol, String orderId) {
        Map<String, String> body = new HashMap<>();
        body.put("category", "spot");
        body.put("symbol", symbol);
        body.put("orderId", orderId);
        String bodyJson = bybitJson.getJson(body);
        String response = cancelOrder("POST", bodyJson, "", "");
        if (response != null) {
            JsonObject responseJson = Json.newJsonObject(response);
            if(responseJson.getJsonNumber("retCode").toString().equals("0")) {
                return getOrder(symbol, orderId);
            }
        }
        return null;
    }

    public String closeOrder(String basecoin, String quotecoin, String orderId) {
        String symbol = basecoin + quotecoin;
        String result = cancelOrder(symbol, orderId);
        return result;
    }

    public String getOrders(String category) {
        String queryString = String.format("category=spot&limit=10");
        return getOrder(queryString);
    }

    public String getOrder(String basecoin, String quotecoin, String orderId) {
        String symbol = basecoin + quotecoin;
        return getOrder(symbol, orderId);
    }

    public String getOrder(String symbol, String orderId) {
        String queryString = String.format("category=spot&symbol=%s&orderId=%s&limit=1&openOnly=0", symbol, orderId);
        String response = getOrder(queryString);
        JsonObject result = Json.newJsonObject(response);
        if (!result.getJsonNumber("retCode").toString().equals("0")) {
            return null;
        }
        JsonArray ordersList = result.getJsonObject("result").getJsonArray("list");
        for(int i = 0; i < ordersList.size(); i ++) {
            JsonObject order = ordersList.getJsonObject(i);
            if (order.getString("orderId").equals(orderId)) {
                return order.toString();
            }
        }
        return null;
    }

    private Builder request(URI uri, String signature, String timestamp, String recvWindow) {
        Builder builder = HttpRequest.newBuilder();
        builder.header("X-BAPI-API-KEY", apiKey);
        builder.header("X-BAPI-SIGN", signature);
        builder.header("X-BAPI-TIMESTAMP", timestamp);
        builder.header("X-BAPI-RECV-WINDOW", recvWindow);
        builder.uri(uri);
        builder.version(HttpClient.Version.HTTP_1_1);
        return builder;
    }

    private String sign(String message) {
        return Hex.encode(mac.doFinal(message.getBytes()));
    }

    private HttpRequest buildRequest(String method, URI uri, String queryString, String jsonBody) {
        if (!(method == "GET" || method == "POST")) {
            return null;
        }
        String timestamp = String.valueOf(timestamp());
        String recvWindow = "20000";
        String message = timestamp + apiKey + recvWindow + (queryString+jsonBody);
        String signature = sign(message);
        if (signature == null) {
            return null;
        }
        Builder builder = request(uri, signature, timestamp, recvWindow);
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

    public void updatePrices(String date) {
        String[] symbols = bybitJson.getSymbols(date);
        if (symbols == null) {
            return;
        }
        Map<String, JsonObject> tickers = new HashMap<>();
        for(String symbol: symbols) {
            String ticker = ticker("spot", symbol);
            if (ticker == "") {
                continue;
            }
            tickers.put(symbol, Json.newJsonObject(ticker));
        }
        bybitJson.updatePrices(tickers, date);
    }

    public long timestamp() {
        //return System.currentTimeMillis();
        String response = serverTime();
        JsonObject result = Json.newJsonObject(response);
        if (!result.getJsonNumber("retCode").toString().equals("0")) {
            return System.currentTimeMillis();
        }
        String timeNano = result.getJsonObject("result").getString("timeNano");
        return TimeUnit.NANOSECONDS.toMillis(Long.valueOf(timeNano));
    }

    public BybitJson getBybitJson() {
        return bybitJson;
    }

    public String getResult(String response) {
        return bybitJson.getResult(response);
    }

    public String getAuthSignature(Long timestamp) {
        String expires = String.format("GET/realtime%s", timestamp);
        System.out.printf("expires: %s\n", expires);
        String message = expires;
        return sign(message);
    }

    public String getApiKey() {
        return apiKey;
    }

}