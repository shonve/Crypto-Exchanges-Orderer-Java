package com.example.okx;


import java.io.PrintStream;
import java.net.URL;
import java.net.URI;
import java.security.Key;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;



import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Assert;

import com.example.codec.Hex;
import com.example.exchange.Exchange;
import com.example.json.Json;
import com.example.util.Utils;

import jakarta.json.JsonArray;
import jakarta.json.JsonObjectBuilder;


public class Okx extends Exchange {
    private static final PrintStream out = System.out;
    private final String exchange = "okx";
    private final Json json = new Json(exchange);
    private final String SIGNATURE_ALGORITHM = "Hmacsha256";
    private final String apiKey;
    private final String secretKey;
    private final String passphrase;
    private final Mac mac;
    private final HttpClient client;
    private final Base64.Encoder encoder  = Base64.getEncoder();

    public Okx(String apiKey, String secretKey, String passphrase) {
        super("okx", "https", "www.okx.com", -1);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.passphrase = passphrase;
        this.mac = getMac();
        Assert.notNull(mac, "Mac must not be null");
        this.client = HttpClient.newHttpClient();
    }

    public String currencyInfo(String currency) {
        // timestamp + method + requestPath + queryString(or body if method is POST)
        String endPoint = Utils.getEndPoint(this.exchange, "currencies");
        String query = String.format("ccy=%s", currency);
        return requestGET(endPoint, query);
    }

    private Mac getMac() {
        Mac mac = null;
         try {
            mac = Mac.getInstance(this.SIGNATURE_ALGORITHM);
            Assert.notNull(mac, "Mac must not be null");
            Key key = new SecretKeySpec(this.secretKey.getBytes(), this.SIGNATURE_ALGORITHM);
            Assert.notNull(key, "Key must not be null");
            mac.init(key);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return mac;
    }

    public HttpRequest request(URI uri, String signature, String timestamp, String method) {
        Builder builder = HttpRequest.newBuilder();
        builder.header("OK-ACCESS-KEY", apiKey);
        builder.header("OK-ACCESS-SIGN", signature);
        builder.header("OK-ACCESS-PASSPHRASE", passphrase);
        builder.header("OK-ACCESS-TIMESTAMP", timestamp);
        if (method == "GET") {
            builder.GET();
        } else if (method == "POST") {
            builder.POST(null);
        }
        builder.uri(uri);
        builder.version(HttpClient.Version.HTTP_1_1);
        //builder.timeout(Duration.ofSeconds(1));
        return builder.build();
    }

    public String requestGET(String endPoint, String query) {
        String file = endPoint;
        if (query != "") {
            file += "?" + query;
        }
        URL url = getURL(file);
        String method = "GET";
        String timestamp = Instant.ofEpochMilli(System.currentTimeMillis()).toString();
        out.printf("time: %s\n", timestamp);
        // message = timestamp + method + file + body
        String message = timestamp + method + file;
        String signature = sign(message);
        Assert.notNull(signature, "Signature must not be null");
        HttpResponse<String> response;
        try {
            out.printf("uri: %s\n", url.toURI().toString());
            HttpRequest request = request(url.toURI(), signature, timestamp, method);
            response = send(request);
        }
        catch(Exception e) {
            e.printStackTrace();
            return "";
        }
        return response.body();

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

    private String sign(String message) {
        return encoder.encodeToString(mac.doFinal(message.getBytes()));
    }

    public String login() {
        String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        String method = "GET";
        String requestPath = "/users/self/verify";
        String msg = timestamp + method + requestPath;
        return loginRequest(timestamp, sign(msg));
    }

    private String loginRequest(String timestamp, String sign) {
        JsonObjectBuilder builder = Json.newJsonObjectBuilder();
        JsonArray args = Json.newJsonArrayBuilder().add(
            Json.newJsonObjectBuilder()
            .add("apiKey", apiKey)
            .add("passphrase", passphrase)
            .add("timestamp", timestamp)
            .add("sign", sign).build()
        ).build();

        String request = builder.add("op", "login").add("args", args).build().toString();

        return request;
    }

    private HttpResponse<String> send(HttpRequest request) {
        HttpResponse<String> response;
        try {
            out.printf("Sending request to client\n");
            response = this.client.send(request, new DefaultBodyHandler());
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }


    private class DefaultBodyHandler implements BodyHandler<String> {
        private final BodySubscriber<String> bodySubscriber;

        DefaultBodyHandler(){
            this.bodySubscriber = BodySubscribers.ofString(StandardCharsets.UTF_8);
        }

        @Override
        public BodySubscriber<String> apply(ResponseInfo responseInfo) {
            out.print("Response Received\n");
            out.printf("Status code %d\n", responseInfo.statusCode());
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
}