package com.example.codec;

import java.security.MessageDigest;

public class Digest {

    public static byte[] sha512(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return md.digest(data);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}