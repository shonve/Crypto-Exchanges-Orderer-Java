package com.example;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.time.Instant;
import java.util.Map;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;


import com.example.codec.Hex;
import com.example.spi.NetProvider;
import com.example.support.Request;
import com.example.support.Response;
import com.example.test.Test;
import com.example.util.FilterBody;
import com.example.websocket.Client;


public class Main {
    static PrintStream out = System.out;
    static String apiKey = "6exXjwoIU9VIkQACWk";
    static String secretKey = "p6IoTbIJtEGpeEXSxxwtKUFucnZ5U9SEq9N9";
    static final String HMAC_SHA256_ALGORITHM = "Hmacsha256";


    public static void main(String[] args) {
        //apiTest();
        //wssTest();
        //connectToWebsocket();
        //testHttpURLProvider();

        //websocketTest();
        test();
    }
    
    static void apiTest() {
        String protocol = "https";
        String host = "api.bybit.com";
        int port = -1;
        //String query = "category=spot&symbol=ALGOUSDT&limit=1";
        String query = "category=spot&symbol=ALGOUSDT&interval=1&limit=10";
        //String file = "/v5/market/orderbook?" + query;
        String file = "/v5/market/kline?" + query;
        
        URL url = null;
        try {
            url = new URL(protocol, host, port, file);
        } catch(Exception e) {  
            e.printStackTrace();
        }
        Assert.notNull(url, "Url must not be null");
        out.printf("Protocol: %s\nHost: %s\nPath: %s\nQuery: %s\nURL String: %s\n", url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), url.toString());
        String timestamp = Long.toString(Instant.now().toEpochMilli());
        String recvWindow = "5000";
        String signature = null;
        try {
            Mac mac = null;
            mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            Assert.notNull(mac, "Mac must not be null");
            Key key = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA256_ALGORITHM);
            Assert.notNull(key, "Key must not be null");
            mac.init(key);
            final String message = timestamp + apiKey + recvWindow + query;
            signature = Hex.encode(mac.doFinal(message.getBytes()));
            Assert.notNull(signature, "Signature must not be null");
        } catch(Exception e) {
            e.printStackTrace();
        }
        out.printf("Signature: %s\nSignature length: %d\n", signature, signature.length());
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
        } catch(Exception e) {
            e.printStackTrace();
        }
        Assert.notNull(connection, "Connection must not be null");
        Request request = new Request(connection);
        request.addHeader("X-BAPI-SIGN", signature);
        request.addHeader("X-BAPI-TIMESTAMP", timestamp);
        request.addHeader("X-BAPI-API-KEY", apiKey);
        request.addHeader("X-BAPI-RECV-WINDOW", recvWindow);
        request.setMethod("GET");
        Response response = request.executeInternal();
        //request.close();
        Assert.notNull(response, "Response must not be null");
        //out.printf("Response Headers: %s\n", response.getHeaders().toString());
        try {
            out.printf("Response code: %s\n", response.getStatusCode().toString());
            HttpHeaders headers = response.getHeaders();
            out.printf("Content Length: %s\n", Long.valueOf(headers.getContentLength()));
            InputStream body = response.getBody();
            Assert.notNull(body, "Body must not be null");
            while(body.available() > 0) {
                out.printf("Body: %s\n", FilterBody.read(body));
            }
            out.printf("Timestamp: %s\n", Long.valueOf(Instant.now().toEpochMilli()));
            
        }
        catch(Exception e) {
            // ignore
            e.printStackTrace();
        } finally {
            response.close();
        }
    }

    static void wssTest() {
        Client.createClient();
        Client.testJson();
    }

    static void connectToWebsocket() {
        String protocol = "https";
        String host = "stream.bybit.com";
        int port = -1;
        String path = "/v5/public/spot";
        //String query = "category=spot&symbol=ALGOUSDT&limit=1";
        //String query = "category=spot&symbol=ALGOUSDT&interval=1&limit=10";
        //String file = "/v5/market/orderbook?" + query;
        String file = path;
        
        URL url = null;
        try {
            url = new URL(protocol, host, port, file);
        } catch(Exception e) {  
            e.printStackTrace();
        }
        Assert.notNull(url, "Url must not be null");
        out.printf("Protocol: %s\nHost: %s\nPath: %s\nQuery: %s\nURL String: %s\n", url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), url.toString());
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
            connection.addRequestProperty("Connection", "Upgrade");
            connection.addRequestProperty("Upgrade", "websocket");
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            for(Map.Entry<String, List<String>> entry: headerFields.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                out.printf("Field: %s\tValues: ", key);
                for(String value: values) {
                    out.printf("%s\t", value);
                }
                out.printf("\n");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        Assert.notNull(connection, "Connection must not be null");
        Request request = new Request(connection);
        //request.addHeader("Connection", "keep-alive");
        //request.addHeader("Connection", "Upgrade");
        //request.addHeader("Upgrade", "websocket");
        //request.addHeader("Upgrade", "websocket");
        //request.addHeader("Connection", "Upgrade");
        request.setMethod("GET");
        Response response = request.executeInternal();
        //request.close();
        Assert.notNull(response, "Response must not be null");
        //out.printf("Response Headers: %s\n", response.getHeaders().toString());
        try {
            out.printf("Response code: %s\n", response.getStatusCode().toString());
            HttpHeaders headers = response.getHeaders();
            out.printf("Content Length: %s\n", Long.valueOf(headers.getContentLength()));
            InputStream body = response.getBody();
            Assert.notNull(body, "Body must not be null");
            while(body.available() > 0) {
                out.printf("Body: %s\n", FilterBody.read(body));
            }
            out.printf("Timestamp: %s\n", Long.valueOf(Instant.now().toEpochMilli()));
            
        }
        catch(Exception e) {
            // ignore
            e.printStackTrace();
        } finally {
            request.close();
            response.close();
        }
    }

    static void testHttpURLProvider() {
        NetProvider.urlStreamProvider();
    }

    static void websocketTest() {
        //Client.createClient();
    }

    static void test() {
        Test.testTime();
    }
}