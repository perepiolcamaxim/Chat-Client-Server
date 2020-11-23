package com.utm.algo;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.spec.AlgorithmParameterSpec;

public class DESClient
{
    private SecretKey key;
    private  Cipher encryptCipher;
    private  Cipher decryptCipher;
    private static final byte[] iv = { 11, 22, 33, 44, 99, 88, 77, 66 };

    public DESClient()
    {
        try
        {
            key = KeyGenerator.getInstance("DES").generateKey();
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

            encryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

            decryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public  void encrypt(InputStream is, OutputStream os) throws IOException
    {
        os = new CipherOutputStream(os, encryptCipher);
        writeData(is, os);
    }

    public  void decrypt(InputStream is, OutputStream os) throws IOException
    {
        is = new CipherInputStream(is, decryptCipher);
        writeData(is, os);
    }

    private static void writeData(InputStream is, OutputStream os) throws IOException
    {
        byte[] buf = new byte[1024];
        int numRead = 0;
        while ((numRead = is.read(buf)) >= 0)
        {
            os.write(buf, 0, numRead);
        }
        os.close();
        is.close();
    }

    public SecretKey getKey()
    {
        return key;
    }
}