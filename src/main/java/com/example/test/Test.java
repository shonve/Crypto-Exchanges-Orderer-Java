package com.example.test;

import java.io.PrintStream;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executor;

import org.springframework.util.Assert;

public class Test {
    static PrintStream out = System.out;

    private static class TestSingleton {

    }

    public static void testTime() {
        //Long MAX_LONG = Long.MAX_VALUE;
        //TimeUnit nano = TimeUnit.NANOSECONDS;
        /*
        TimeUnit micro = TimeUnit.MICROSECONDS;
        TimeUnit milli = TimeUnit.MILLISECONDS;
        TimeUnit sec = TimeUnit.SECONDS;
        nano.print();
        out.printf("nano MAX_LONG: %d\n", nano.addAndGet());
        micro.print();
        out.printf("micro MAX_LONG: %d\n", micro.addAndGet());
        milli.print();
        out.printf("milli MAX_LONG: %d\n", milli.addAndGet());
        sec.print();
        out.printf("sec MAX_LONG: %d\n", sec.addAndGet());
        */
        TimeUnit nano = TimeUnit.NANOSECONDS;
        TimeUnit sec = TimeUnit.SECONDS;

        Instant now = Instant.now();
        out.printf("Timestamp: %d\n", now.toEpochMilli());
        Long instantSec = now.getEpochSecond(); //+ now.getNano();
        int instantNano = now.getNano();
        long timestamp = sec.toMillis(instantSec) + nano.toMillis(instantNano);

        out.printf("Timestamp: %d\n", timestamp);
    }

    public static void test() {
        Executor e = Runnable::run;
        Assert.notNull(e, "Executor must not be null");
        out.printf("Executor classname: %s\n", e.getClass().getName());
        TestSingleton singleton = new TestSingleton();
        HttpClient client = HttpClientImpl.newHttpClient();
        if (client instanceof HttpClientImpl impl) {
            impl.clientImpl();
            //HttpClientImpl.client2Ref.get().print();
        }
        if (client instanceof HttpClient) {
            out.printf("Client is instance of HttpClient\n");
        }
        if (client instanceof HttpClientImpl) {
            out.printf("Client is instance of HttpClientImpl\n");
        }
    }
}