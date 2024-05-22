package com.example.exchange;

import java.io.PrintStream;
import java.net.URL;
import java.net.URI;
import java.security.Key;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jakarta.json.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Assert;

import com.example.json.Json;
import com.example.util.Utils;


public abstract class Exchange {
    static final PrintStream out = System.out;
    protected final String exchange;
    protected final String protocol;
    protected final String host;
    protected final int port;
    protected final HttpClient client;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final Sender sender = new Sender();

    private final int maxTries = 10;

    public Exchange(String exchange, String protocol, String host, int port) {
        this.exchange = exchange;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.client = HttpClient.newHttpClient();
    }

    public Mac getMac(String signatureAlgorithm, String secretKey) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(signatureAlgorithm);
            Assert.notNull(mac, "Mac must not be null");
            Key key = new SecretKeySpec(secretKey.getBytes(), signatureAlgorithm);
            Assert.notNull(key, "Key must not be null");
            mac.init(key);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return mac;
    }

    public String tokenInfo(String query) {
        String endPoint = Utils.getEndPoint(exchange, "tokenInfo");
        return response(requestGET(endPoint, query))
    }

    public String tokenInfo(String pathParams, String query) {
        String endPoint = Utils.getEndPoint(exchange, "tokenInfo");
        endPoint += pathParams;
        return response(requestGET(endPoint, query));
    }

    public String tickerInfo(String query) {
        String endPoint = Utils.getEndPoint(exchange, "tickers");
        return response(requestGET(endPoint, query));
    }

    public abstract String openOrder(String basecoin, String quotecoin, String side, String price, String qty);

    public abstract String closeOrder(String basecoin, String quotecoin, String orderId);

    public String openOrder(String bodyJson) {
        String endPoint = Utils.getEndPoint(exchange, "createOrder");
        return response(requestPOST(endPoint, bodyJson));
    }

    public abstract String orderId(String response);

    public String cancelOrder(String method, String bodyJson, String query, String orderId) {
        String endPoint = Utils.getEndPoint(exchange, "cancelOrder");
        if (method.equals("POST")) {
            return response(requestPOST(endPoint, bodyJson));
        } else if (method.equals("DELETE")) {
            if (exchange.equals("gateio")) {
                endPoint += String.format("/%s", orderId);
                JsonObject result = sender.send(requestDELETE(endPoint, query));
                if (result == null) {
                    return null;
                }
                /*
                if (result.getString("statusCode").equals("201")) {
                    return result.getString("response");
                }
                */
                return result.toString();
            }
            return response(requestDELETE(endPoint, query));
        }
        return null;
    }

    public abstract String getOrder(String basecoin, String quotecoin, String orderId);

    public String getOrder(String query) {
        String endPoint = Utils.getEndPoint(exchange, "getOrders");
        if (exchange.equals("gateio")) {
            String[] tokens = query.split("&");
            String orderId = tokens[1].split("=")[1];
            endPoint = String.format("%s/%s", endPoint, orderId);
            query = tokens[0];
        }
        System.out.printf("exchange: %s, endPoint: %s, query: %s\n", exchange, endPoint, query);
        return response(requestGET(endPoint, query));
    }

    /*
    public String getOrder(String query, String orderId) {
        String endPoint = Utils.getEndPoint(exchange, "getOrders");
        endPoint += String.format("/%s", orderId);
        return requestGET(endPoint, query);
    }
    */

    public String walletBalance(String query) {
        String endPoint = Utils.getEndPoint(exchange, "walletBalance");
        return response(requestGET(endPoint, query));
    }

    public String coinBalance(String query) {
        String endPoint = Utils.getEndPoint(exchange, "coinBalance");
        return response(requestGET(endPoint, query));
    }

    public String serverTime() {
        String endPoint = Utils.getEndPoint(exchange, "serverTime");
        return response(request(endPoint));
    }

    public String response(HttpRequest request) {
        JsonObject result = sender.send(request);
        if (result != null) {
            String response = result.getString("response");
            System.out.println(response);
            return response;
            //return result.getString("response");
        }
        return null;
    }

    public int statusCode(String result) {
        return Integer.parseInt(Json.newJsonObject(result).getString("statusCode"));
    }

    public HttpRequest request(String endPoint) {
        HttpRequest request = null;
        try {
            URI uri = getURL(endPoint).toURI();
            request = HttpRequest.newBuilder().uri(uri).GET().version(HttpClient.Version.HTTP_1_1).build();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return request;
    }

    public HttpRequest requestGET(String endPoint, String query) {
        String file = endPoint;
        if (query != "") {
            file += "?" + query;
        }
        URL url = getURL(file);
        String path = query;
        String response = null;
        try {
            if (exchange == "bitget") {
                path = file;
            } 
            if (exchange == "gateio") {
                path = endPoint + "\n" + query;
            }
            HttpRequest request = buildGETRequest(url.toURI(), path, 2);
            //out.printf("URI: %s\n", url.toURI().toString());
            return request;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpRequest requestPOST(String endPoint, String bodyJson) {
        URL url = getURL(endPoint);
        String response = null;
        String path = "";
        try { 
            if (exchange == "gateio") {
                path = endPoint + "\n" + "";
            }
            HttpRequest request = buildPOSTRequest(url.toURI(), path, bodyJson, 5);
            return request;
            //out.printf("URI: %s\n", url.toURI().toString());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpRequest requestDELETE(String endPoint, String query) {
        String file = endPoint;
        if (query != "") {
            file += "?" + query;
        }
        URL url = getURL(file);
        String path = query;
        String response = null;
        try {
            if (exchange == "bitget") {
                path = file;
            } 
            if (exchange == "gateio") {
                path = endPoint + "\n" + query;
            }
            HttpRequest request = buildDELETERequest(url.toURI(), path, 5);
            return request;    
        
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private URL getURL(String file) {
        URL url = null;
        try {
            url = new URL(this.protocol, this.host, port, file);
        } catch(Exception e) {  
            e.printStackTrace();
            return null;
        }
        return url;
    }

    private final class Sender {
        
        public JsonObject send(HttpRequest request) {
            try {
                DefaultBodyHandler bodyHandler = new DefaultBodyHandler();
                String response = client.send(request, bodyHandler).body();
                int statusCode = bodyHandler.statusCode;
                JsonObject result = Json.newJsonObjectBuilder().add("statusCode", String.valueOf(statusCode)).add("response", response).build();
                return result; 
            }
            catch(Exception e) {
                //e.printStackTrace();
                return null;
            }
        }
    }

    private class DefaultBodyHandler implements BodyHandler<String> {
        private final BodySubscriber<String> bodySubscriber;
        public int statusCode = 0;

        DefaultBodyHandler(){
            this.bodySubscriber = BodySubscribers.ofString(StandardCharsets.UTF_8);
        }

        @Override
        public BodySubscriber<String> apply(ResponseInfo responseInfo) {
            //out.print("Response Received\n");
            statusCode = responseInfo.statusCode();
            //out.printf("Status code %d\n", responseInfo.statusCode());
            /*
            Map<String, List<String>> headers = responseInfo.headers().map();
            headers.forEach((header, value) -> {
                if (value instanceof List<String> values) {
                    out.printf("Header: %s, Values: %s\n", header, values.toString());
                }
            });
            */
            return this.bodySubscriber;
        }
    } 

    public abstract HttpRequest buildGETRequest(URI uri, String queryString, int timeout);
    public abstract HttpRequest buildPOSTRequest(URI uri, String path, String bodyJson, int timeout);
    public abstract HttpRequest buildDELETERequest(URI uri, String queryString, int timeout);

    public abstract String getResult(String response);

    public void shutdownExecutor() {
        executor.shutdown();
    }

}