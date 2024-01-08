package com.example.websocket;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;


import com.example.websocket.AbstractWebSocketClient.Message;

final class StreamHandler implements StreamWriter, StreamReader {

    private final int capacity = 8096;  // factory can store 8096 bytes
    private final Queue<Message<CharSequence>> textMessageQueue = new ArrayDeque<>();
    private final Queue<Message<ByteBuffer>> binaryMessageQueue = new ArrayDeque<>();
    private int textLimit = 0;
    private int binaryLimit = 0;

    public boolean writeText(CharSequence message, boolean last) {
        int bytesStored = binaryLimit + textLimit;
        if (bytesStored + message.length() > capacity) {
            return false;
        }
        textLimit += message.length();
        textMessageQueue.add(new Message<CharSequence>(message, last));
        return true;
    }

    public boolean writeBinary(ByteBuffer message, boolean last) {
        int bytesStored = binaryLimit + textLimit;
        if (bytesStored + message.limit() > capacity) {
            return false;
        }
        binaryLimit += message.limit();
        binaryMessageQueue.add(new Message<ByteBuffer>(message, last));
        return true;
    }

    public boolean textAvailable() {
        return textMessageQueue.size() > 0;
    }

    public boolean binaryAvailable() {
        return binaryMessageQueue.size() > 0;
    }

    public Message<CharSequence> readText() {
        if (textMessageQueue.size() > 0) {
            Message<CharSequence> message = textMessageQueue.remove();
            textLimit -= message.getMessage().length();
            return message;
        }
        return null;
    }

    public Message<ByteBuffer> readBinary() {
        if (binaryMessageQueue.size() > 0) {
            Message<ByteBuffer> message = binaryMessageQueue.remove();
            binaryLimit -= message.getMessage().limit();
            return message;
        }
        return null;
    }

    public void run() {
        // handle the stream messages
        handleQueueMessages();
    }

    // assume only text messages
    private void handleQueueMessages() {
        String currentTextStream = null;

        for(;;) {
            if (textAvailable()) {
                                
            }
        }
    }
}