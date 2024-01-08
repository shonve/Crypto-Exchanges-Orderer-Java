package com.example.spi;

import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.net.http.HttpClient;
import java.net.URLStreamHandler;


public class NetProvider {
    private static PrintStream out = System.out;
    private static String protocol = "https";
    private static String host = "google.com";

    public static void provider() {
        ServiceLoader<HttpURLConnection> connections = ServiceLoader.load(HttpURLConnection.class);
        Iterator<HttpURLConnection> it = connections.iterator();
        int count = 0;
        while(it.hasNext()) {
            count ++;
            HttpURLConnection connection = it.next();
        }
        out.printf("Number of HttpURLConnection providers: %d\n", count);
    }

    public static void urlStreamProvider() {
        String className = "sun.net.www.protocol.http.Handler";
        String httpConnectionClass = "sun.net.www.protocol.http.HttpURLConnection";
        try {
            Class<?> cls = Class.forName(className);
            out.printf("URLStreamHandler class: %s\n", cls.getName());
            URLStreamHandler handler = (URLStreamHandler)cls.getDeclaredConstructor().newInstance();
            URL url = new URL(protocol, host, -1, "", handler);
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                out.printf("Security manager is not null\n");
            }
            //HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}