package com.example.proxy;

import java.io.PrintStream;
import java.net.InetAddress;

public class Proxy {
    public static PrintStream out = System.out;
    public static String[] testIpAddresses = {
        "64.18.0.0",
        "64.233.160.0",
        "66.102.0.0",
        "66.249.80.0",
        "72.14.192.0",
        "74.125.0.0",
        "173.194.0.0",
        "207.126.144.0",
        "209.85.128.0",
        "216.58.208.0",
        "216.239.32.0"
    };

    public static void test() {
        for(String ipAddress: testIpAddresses) {
            InetAddress proxyAddress = proxy(ipAddress);
            String hostname = proxyAddress.getHostName();
            out.printf("Hostname is: %s\n", hostname);
        }
    }

    public static InetAddress proxy(String rawAddress) {
        byte[] tokens = new byte[4];
        int j = 0;
        while(!rawAddress.equals("") && rawAddress.indexOf('.') != -1) {
            int index = rawAddress.indexOf('.');
            int value = Integer.parseInt(rawAddress.substring(0, index));
            value = value > 127 ? -128 + (value - 128) : value;
            tokens[j++] = Byte.parseByte(Integer.toString(value));
            rawAddress = rawAddress.substring(index+1);
        }
        int value = Integer.parseInt(rawAddress);
        value = value > 127 ? -128 + (value - 128) : value;
        tokens[j++] = Byte.parseByte(Integer.toString(value));
        
        int address;
        address  = tokens[3] & 0xFF;
        address |= ((tokens[2] << 8) & 0xFF00);
        address |= ((tokens[1] << 16) & 0xFF0000);
        address |= ((tokens[0] << 24) & 0xFF000000);
        out.printf("Address is: %d\n", address);
        try {
            return InetAddress.getByAddress(tokens);
        } catch(Exception e) {
            return null;
        }
    }
}