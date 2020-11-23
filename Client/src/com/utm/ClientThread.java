package com.utm;

import com.utm.algo.DESClient;
import com.utm.algo.RSAClient;

import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

class ClientThread implements Runnable
{
    private final BufferedReader reader;
    private final PrintWriter writer;

    static String clientPrimitCodat = "C:\\Users\\perep\\Desktop\\clientPrimitCodat.txt";
    static String clientPrimitDecodat = "C:\\Users\\perep\\Desktop\\clientPrimitDecodat.txt";
    private final DESClient des = new DESClient();
    private final RSAClient rsa = new RSAClient();

    ClientThread(BufferedReader reader, PrintWriter writer)
    {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void run()
    {
        String fromServer;
        while (true)
        {
            try
            {
                if ((fromServer = reader.readLine()) != null)
                {
                    if (fromServer.contains("Server: Hello client "))
                    {
                        // verify signature
                        String messageToVerify = fromServer;
                        String FinalMessageToVerify = fromServer.substring(0, fromServer.lastIndexOf(" "));
                        String signature = messageToVerify.substring(messageToVerify.indexOf("/") + 1, messageToVerify.indexOf("&"));

                        System.out.println("DSA Signature: " + signature);

                        byte[] decode = Base64.getDecoder().decode(signature);
                        String publicKey = messageToVerify.substring(messageToVerify.indexOf("&") + 1);
                        byte[] encoded = Base64.getDecoder().decode(publicKey);
                        KeyFactory kf = KeyFactory.getInstance("DSA");
                        PublicKey pubKey = (PublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
                        Signature sign = Signature.getInstance("SHA256withDSA");
                        //Initializing the signature
                        sign.initVerify(pubKey);
                        sign.update(FinalMessageToVerify.getBytes());
                        //Verifying the signature

                        if (sign.verify(decode))
                        {
                            fromServer = FinalMessageToVerify.replace("Server: Hello client ", "");

                            BigInteger rsaPublicKey = new BigInteger(fromServer.substring(0, fromServer.indexOf(" ")));

                            System.out.println("RSA Public Key: " + rsaPublicKey);

                            BigInteger rsaN = new BigInteger(fromServer.substring(fromServer.lastIndexOf(" ") + 1));

                            rsa.setPublicKey(rsaPublicKey);
                            rsa.setN(rsaN);
                            SecretKey key = des.getKey();
                            Client.des = des;

                            String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded()); // FdwL7F2X2Y8=

                            System.out.println("DES Key: " + encodedKey);

                            byte[] rsabytes = rsa.encrypt(encodedKey.getBytes()); // bitii criptati care trebuies transmisi
                            String s = Base64.getEncoder().encodeToString(rsabytes);// stringul ce trebuie trimis

                            writer.println("Key " + s);
                            writer.flush();
                        } else
                        {
                            //System.out.println("Server is not secure!");
                            return;
                        }
                    } else
                    {
                        //write to file
                        byte[] decode = Base64.getDecoder().decode(fromServer);
                        try (FileOutputStream stream = new FileOutputStream(clientPrimitCodat))
                        {
                            stream.write(decode);
                        }

                        // decrypt fromUser
                        des.decrypt(new FileInputStream(clientPrimitCodat), new FileOutputStream(clientPrimitDecodat));

                        //read from file
                        File myObj = new File(clientPrimitDecodat);
                        Scanner myReader = new Scanner(myObj);
                        fromServer = myReader.nextLine();

                        myObj.delete();

                        System.out.println(fromServer);
                        Client.messageServer = fromServer;
                    }
                }
            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e)
            {
                e.printStackTrace();
            }
        }
    }
}