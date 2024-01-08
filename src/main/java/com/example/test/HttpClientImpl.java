package com.example.test;

import java.io.PrintStream;
import java.lang.ref.WeakReference;

import org.springframework.util.Assert;

public class HttpClientImpl implements HttpClient {
    static PrintStream out = System.out;
    public static WeakReference<HttpClient2Impl> client2Ref;

    private static class HttpClient2Impl {
        HttpClient2Impl client2;

        HttpClient2Impl create() {
            Assert.isNull(client2, "HttpClient2Impl is not null");
            client2 = this;
            return client2;
        }

        public void print() {
            out.printf("I am HttpClient2Impl\n");
        }
    }

    public static HttpClient newHttpClient() {
        HttpClient2Impl client2 = new HttpClient2Impl();
        client2Ref = new WeakReference<>(client2.create());
        return new HttpClientImpl();
    }

    public void clientImpl() {
        out.printf("I am clientImpl\n");
    }
}