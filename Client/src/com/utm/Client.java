package com.utm;

import com.utm.algo.DESClient;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;

public class Client
{
    public static String messageServer = "";
    public static DESClient des;

    void connect()
    {
        Scanner myInput = new Scanner( System.in );
        try
        {
            Socket socket = new Socket("127.0.0.1", 5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            String fromUser;

            ClientThread helper = new ClientThread(reader, writer);
            Thread thread = new Thread(helper);
            thread.start();

            while (true)
            {
                if (messageServer.equals("Server: Bye"))
                    break;

                fromUser = myInput.nextLine();
                fromUser = encrypt(fromUser);

                writer.println(fromUser);
                writer.flush();
            }

            writer.close();
            reader.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String encrypt(String fromUser) throws IOException
    {
        String clearTextFile = "C:\\Users\\perep\\Desktop\\clientTrimite.txt";
        String cipherTextFile = "C:\\Users\\perep\\Desktop\\clientTrimiteCodat.txt";

        //write to file
        FileWriter myWriter = new FileWriter(clearTextFile);
        myWriter.write(fromUser);
        myWriter.close();

        // encrypt fromUser
        des.encrypt(new FileInputStream(clearTextFile), new FileOutputStream(cipherTextFile));

        File file = new File(clearTextFile);
        file.delete();

        //read from file
        Path path = Paths.get(cipherTextFile);
        byte[] bytes = null;
        bytes = Files.readAllBytes(path);

        String s = Base64.getEncoder().encodeToString(bytes);
        File file1 = new File(cipherTextFile);
        file1.delete();
        return s;
    }

    public static void main(String[] args)
    {
        new Client().connect();
    }
}