package com.example.websocket;

import java.nio.ByteBuffer;

import com.example.websocket.AbstractWebSocketClient.Message;


public interface StreamReader extends Runnable {

    public boolean textAvailable();
    public boolean binaryAvailable();
    public Message<CharSequence> readText();
    public Message<ByteBuffer> readBinary();



}