package com.example.kraken;


import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.net.URL;
import java.net.URI;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;


import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;



import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Assert;

import com.example.codec.Digest;
import com.example.codec.Hex;
import com.example.exchange.Exchange;
import com.example.json.Json;
import com.example.util.Utils;


public class Kraken extends Exchange {
    private static final PrintStream out = System.out;
    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final String exchange = "kraken";
    private final Json json = new Json(exchange);
    private final String SIGNATURE_ALGORITHM = "Hmacsha512";
    private final String krakenSecret;
    private final String apiKey;
    private final String secretKey;
    private final Mac mac;
    private final HttpClient client;

    private final KrakenJson krakenJson = new KrakenJson();

    public Kraken(String apiKey, String krakenSecret) {
        super("kraken", "https", "api.kraken.com", -1);
        this.apiKey = apiKey;
        this.krakenSecret = krakenSecret;
        this.secretKey = getSecretKey();
        this.mac = getMac();
        Assert.notNull(mac, "Mac must not be null");
        this.client = HttpClient.newHttpClient();
    }

    public String getSecretKey() {
        byte[] secretKeyBytes = decoder.decode(krakenSecret);
        out.printf("Secret Key Bytes: %s\n", secretKeyBytes.length);
        return new String(secretKeyBytes);
    }
    
    public String assetInfo(String asset) {
        // timestamp + method + requestPath + queryString(or body if method is POST)
        String endPoint = Utils.getEndPoint(this.exchange, "assets");
        String query = String.format("asset=%s", asset);
        return requestGET(endPoint, query);
    }

    public String balance() {
        String endPoint = Utils.getEndPoint(this.exchange, "balance");
        String pathParams = "";
        String nonce = UUID.randomUUID().toString();
        return requestPOST(endPoint, pathParams, nonce);
    }

    public String ticker(String symbol) {
        String endPoint = Utils.getEndPoint(this.exchange, "tickers");
        String query = String.format("pair=%s", symbol);
        String body = "";
        try {
            body = requestGET(endPoint, query);
        }
        catch(Exception e) {
            e.printStackTrace();
            return body;
        }
        if (body != null){
            krakenJson.insertTicker(symbol, body);
        }
        return body;
    }

    private Mac getMac() {
        Mac mac = null;
         try {
            mac = Mac.getInstance(this.SIGNATURE_ALGORITHM);
            Assert.notNull(mac, "Mac must not be null");
            Key key = new SecretKeySpec(decoder.decode(krakenSecret), this.SIGNATURE_ALGORITHM);
            Assert.notNull(key, "Key must not be null");
            mac.init(key);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return mac;
    }

    public HttpRequest request(URI uri, String signature, String timestamp, String method, String payload) {
        Builder builder = HttpRequest.newBuilder();
        builder.header("API-Key", apiKey);
        builder.header("API-Sign", signature);
        //builder.header("TIMESTAMP", timestamp);
        if (method == "GET") {
            builder.GET();
        } else if (method == "POST") {
            if(payload != "") {
                builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
            }
            builder.POST(BodyPublishers.ofString(payload));
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
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String payload = String.format("nonce=%s", timestamp);
        String method = "GET";
        byte[] message = message(endPoint, timestamp, payload);
        String signature = encoder.encodeToString(mac.doFinal(message));
        Assert.notNull(signature, "Signature must not be null");
        HttpResponse<String> response;
        try {
            HttpRequest request = request(url.toURI(), signature, timestamp, method, "");
            response = send(request);
        }
        catch(Exception e) {
            e.printStackTrace();
            return "";
        }
        return response.body();
    }

    public String getSignature() {
        /*
         * 
         *  postdata = urllib.parse.urlencode(data)
            encoded = (str(data['nonce']) + postdata).encode()
            message = urlpath.encode() + hashlib.sha256(encoded).digest()

            mac = hmac.new(base64.b64decode(secret), message, hashlib.sha512)
            sigdigest = base64.b64encode(mac.digest())
            return sigdigest.decode()
         */
        String path = "/0/private/Balance";
        String payload = "12414.5375495nonce=12414.5375495";
        /*
        for(int i = 0; i < payloadDigest.length; i ++) {
            out.printf("%s, ", payloadDigest[i]&0xFF);
        }
        */
        
        byte[] payloadDigest = Digest.sha256(payload.getBytes());
        ByteBuffer messageDigest = ByteBuffer.allocate(payloadDigest.length + path.length());
        messageDigest.put(path.getBytes());
        messageDigest.put(payloadDigest);
        /*
        byte[] messageDigestArr = messageDigest.array();
        for(int i = 0; i < messageDigestArr.length; i ++) {
            out.printf("%s, ", messageDigestArr[i]&0xFF);
        }
        */
        byte[] macSignDigest = mac.doFinal(messageDigest.array());
        out.printf("MacSignDigest: %s\n", macSignDigest.length);
        for(int i = 0; i < macSignDigest.length; i ++) {
            out.printf("%s, ", macSignDigest[i]&0xFF);
        }
        String signature = encoder.encodeToString(macSignDigest);
        out.printf("Signature: %s\n", signature);
        return signature;

    }

    public String requestPOST(String endPoint, String pathParams, String postData) {
        String file = endPoint;
        if (pathParams != "") {
            file += "/" + pathParams;
        }
        URL url = getURL(file);
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String payload = "nonce=" + timestamp;
        if (postData != "") {
            payload += "&" + postData;
        }
        String method = "POST";
        byte[] message = message(endPoint, timestamp, payload);
        String signature = encoder.encodeToString(mac.doFinal(message));
        Assert.notNull(signature, "Signature must not be null");
        HttpResponse<String> response;
        try {
            HttpRequest request = request(url.toURI(), signature, timestamp, method, payload);
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

    private byte[] message(String endPoint, String timestamp, String payload) {
        byte[] payloadDigest = Digest.sha256((timestamp+payload).getBytes());
        ByteBuffer messageDigest = ByteBuffer.allocate(endPoint.length() + payloadDigest.length);
        messageDigest.put(endPoint.getBytes());
        messageDigest.put(payloadDigest);
        return messageDigest.array();
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