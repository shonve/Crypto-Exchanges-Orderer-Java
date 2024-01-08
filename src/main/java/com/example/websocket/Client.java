package com.example.websocket;

import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import jakarta.json.spi.JsonProvider;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;


import org.springframework.util.Assert;
import org.springframework.web.util.WebAppRootListener;

import com.example.support.Response;



public class Client {
    private static PrintStream out = System.out;
    // no need to perform authentication on public channel
    private static String httpHost = "api.bybit.com";
    private static String protocol = "wss";
    private static String host = "stream.bybit.com";
    private static String publicChannel = "/v5/public/spot";
    private static String privateChannel = "/v5/private";
    private static URL url;
    private static URI uri;


    public static void createClient() {
        try {
            ForkJoinPool.ForkJoinWorkerThreadFactory factory = ForkJoinPool.defaultForkJoinWorkerThreadFactory;
            

            //Thread currentThread = Thread.currentThread();
            //out.printf("Current Thread name: %s\n", currentThread.getName());
            //out.printf("User-Agent is: %s\n", userAgent());
            /*
            HttpClient.Builder clientBuilder = HttpClient.newBuilder();
            clientBuilder.version(HttpClient.Version.HTTP_1_1);
            HttpClient client = clientBuilder.build();
            WekSocket.Builder websocketBuilder = client.newWebSocketBuilder();
            //out.printf("Class loaded %s\n", classLoaded.getName());
            */
            //ClassLoader loader = ClassLoader.getSystemClassLoader();
            /* 
            SSLContext sslContext = SSLContext.getDefault();
            Provider provider = sslContext.getProvider();
            out.printf("SSL context provider\nInfo: %s\nName: %s\nClassname: %s\n", 
            provider.getInfo(), provider.getName(), provider.getClass().getName());
            SSLParameters parameters = sslContext.getDefaultSSLParameters();
            String[] protocols = parameters.getProtocols();
            for(String proto: protocols) {
                out.printf("Protocol is: %s\n", proto);
            }
            */

        }
        catch(Exception e) {
            e.printStackTrace();
        } 
    }

    public static void testJson() {
        try {
            JsonObjectBuilder builder = JsonProvider.provider().createObjectBuilder();
            builder.add("op", "auth");
            builder.add("signature", "null");
            out.printf("Json String: %s\n", builder.build().toString());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    static ByteBuffer ping() {
        JsonObjectBuilder builder = JsonProvider.provider().createObjectBuilder();
        builder.add("op", "ping");
        return ByteBuffer.wrap(builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }
    
    static String userAgent() {
        return "Java-http-client/" + System.getProperty("java.version");
    }
    
}