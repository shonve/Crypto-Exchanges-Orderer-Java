package com.example.websocket;

import java.nio.ByteBuffer;


public interface StreamWriter {

    public boolean writeText(CharSequence message, boolean last);
    public boolean writeBinary(ByteBuffer message, boolean last);

}