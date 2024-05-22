package com.example.bitstamp;


import java.io.PrintStream;
import java.net.URL;
import java.net.URI;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;



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


public class Bitstamp extends Exchange {
    private static final PrintStream out = System.out;
    private final String exchange = "bitstamp";
    private final Json json = new Json(exchange);
    private final String SIGNATURE_ALGORITHM = "Hmacsha256";
    private final String apiKey;
    private final String secretKey;
    private final String passphrase;
    private final Mac mac;
    private final HttpClient client;


    /*
     * 
     * message = 'BITSTAMP ' + api_key + \
    'POST' + \
    'www.bitstamp.net' + \
    '/api/v2/user_transactions/' + \
    '' + \
    content_type + \
    nonce + \
    timestamp + \
    'v2' + \
    payload_string
message = message.encode('utf-8')
signature = hmac.new(API_SECRET, msg=message, digestmod=hashlib.sha256).hexdigest()
headers = {
    'X-Auth': 'BITSTAMP ' + api_key,
    'X-Auth-Signature': signature,
    'X-Auth-Nonce': nonce,
    'X-Auth-Timestamp': timestamp,
    'X-Auth-Version': 'v2',
    'Content-Type': content_type
}
     */

    public Bitstamp(String apiKey, String secretKey, String passphrase) {
        super("bitstamp", "https", "www.bitstamp.net", -1);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.passphrase = passphrase;
        this.mac = getMac();
        Assert.notNull(mac, "Mac must not be null");
        this.client = HttpClient.newHttpClient();
    }

    public String withdrawalFeeCoin(String coin, String network) {
        // timestamp + method + requestPath + queryString(or body if method is POST)
        String endPoint = Utils.getEndPoint(this.exchange, "withdrawalFeeCoin");
        /*
        String file = endPoint + "?" + query;
        URL url = getURL(file);
        out.printf("Bitget coinfo uri: %s\n", url.toString());
        return "";
        */
        return requestPOST(endPoint, coin, network);
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

    public HttpRequest request(URI uri, String signature, String timestamp, String method, String body, String nonce) {
        Builder builder = HttpRequest.newBuilder();
        builder.header("X-Auth", exchange.toUpperCase() + " " + apiKey);
        builder.header("X-Auth-Signature", signature);
        builder.header("X-Auth-Nonce", nonce); 
        builder.header("X-Auth-Timestamp", timestamp);
        builder.header("X-Auth-Version", "v2");
        if (body != "") {
            builder.header("Content-Type", "application/x-www-form-urlencoded");
        }
        if (method == "GET") {
            builder.GET();
        } else if (method == "POST") {
            builder.POST(HttpRequest.BodyPublishers.ofString(body));
        }
        builder.uri(uri);
        builder.version(HttpClient.Version.HTTP_1_1);
        //builder.timeout(Duration.ofSeconds(1));
        return builder.build();
    }

    public String requestGET(String endPoint, String query) {
        String file = endPoint + "?" + query;
        URL url = getURL(file);
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String method = "GET";
        String message = timestamp + method + file;
        String signature = sign(message);
        Assert.notNull(signature, "Signature must not be null");
        HttpResponse<String> response;
        try {
            HttpRequest request = request(url.toURI(), signature, timestamp, method, "", "");
            response = send(request);
        }
        catch(Exception e) {
            e.printStackTrace();
            return "";
        }
        return response.body();

    }

    public String getAllWithdrawalFees() {
        String endPoint = Utils.getEndPoint(this.exchange, "withdrawalFeeAll");
        return requestPOST(endPoint, "", "");
    }

    public String requestPOST(String endPoint, String currency, String network) {
        String file = endPoint + "/";
        if (currency != "") {
            file += currency + "/";
        } 
        URL url = getURL(file);
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String method = "POST";
        String contentType = "application/x-www-form-urlencoded";
        String nonce = UUID.randomUUID().toString();
        String body = "";
        if (network != "") {
            body = String.format("network=%s", network);
        }
        
        String message = exchange.toUpperCase() + " " + apiKey + method + host + file;
        if (body != "") {
            message += contentType;
        }
        message += nonce + timestamp;
        if (body != "") {
            message += body;
        }
        String signature = sign(message);
        Assert.notNull(signature, "Signature must not be null");
        HttpResponse<String> response;
        try {
            out.printf("uri: %s\n", url.toURI().toString());
            HttpRequest request = request(url.toURI(), signature, timestamp, method, body, nonce);
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
        return Hex.encode(mac.doFinal(message.getBytes()));
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