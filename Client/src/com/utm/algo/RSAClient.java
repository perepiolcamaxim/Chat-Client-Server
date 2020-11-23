package com.utm.algo;

import java.math.BigInteger;
import java.util.Random;

public class RSAClient
{
    private BigInteger p;
    private BigInteger q;
    private BigInteger N;
    private BigInteger phi;
    private BigInteger e;
    private BigInteger d;
    private int        bitlength = 1024;
    private Random     r;

    // Cripteaza mesajul
    public byte[] encrypt(byte[] message)
    {
        return (new BigInteger(message)).modPow(e, N).toByteArray();
    }

    // Decripteaza mesajul
    public byte[] decrypt(byte[] message)
    {
        return (new BigInteger(message)).modPow(d, N).toByteArray();
    }

    private static String bytesToString(byte[] encrypted)
    {
        String test = "";
        for (byte b : encrypted)
        {
            test += Byte.toString(b);
        }
        return test;
    }

    public void setPublicKey(BigInteger rsaPublicKey)
    {
        e = rsaPublicKey;
    }
    public void setN(BigInteger N)
    {
        this.N = N;
    }
}