package com.example.server;


import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import org.springframework.util.Assert;


public class Server {
    private final String host = "localhost";
    private final int port;
    private final ServerSocket server;
    

    public Server(int port) {
        this.port = port;
        server = getServer();
        Assert.notNull(server, "server must not be null");
    }

    private final ServerSocket getServer() {
        ServerSocket server;
        try {
            server = new ServerSocket();
            SocketAddress addr = new InetSocketAddress(host, port);
            server.bind(addr);
        }
        catch(Exception e) {
            //e.printStackTrace();  ignore
            return null;
        }
        return server;
    }

    public Socket acceptConnection(int timeoutMillis) {
        Socket socket = null;
        try{
            //System.out.printf("[Server] Waiting for connection\n");
            server.setSoTimeout(timeoutMillis);
            socket = server.accept();
            System.out.println("new client connected");
        }
        catch(Exception e) {
            //e.printStackTrace();  ignore
            //System.out.printf("Accept timed out\n");
            return null;
        }
        return socket;

    }

    public void close() {
        try {
            server.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}