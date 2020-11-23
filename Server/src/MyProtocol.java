import algo.DES;
import algo.RSA;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class MyProtocol
{
    static String primitDecodat = "C:\\Users\\perep\\Desktop\\serverprimitDecodat.txt";
    static String primitCodat = "C:\\Users\\perep\\Desktop\\serverprimitCodat.txt";
    static String serverResponseClear = "C:\\Users\\perep\\Desktop\\serverResponseClearChat.txt";
    static String serverResponseCipher = "C:\\Users\\perep\\Desktop\\serverResponseCipherChat.txt";
    private RSA rsa = new RSA();
    private DES des;
    private final Chat chat = Chat.getInstance();
    private final PrintWriter writer;
    int chatKey;
    boolean inChat;
    String userName;

    public MyProtocol(PrintWriter writer)
    {
        this.writer = writer;
    }

    public String processInput(String input) throws Exception
    {
        if(!inChat)
        {
            String output;

            if (input == null)
            {
                //Creating KeyPair generator object
                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");
                //Initializing the key pair generator
                keyPairGen.initialize(2048);
                //Generate the pair of keys
                KeyPair pair = keyPairGen.generateKeyPair();
                //Getting the private key from the key pair
                PrivateKey privKey = pair.getPrivate();
                //Creating a Signature object
                Signature sign = Signature.getInstance("SHA256withDSA");
                //Initialize the signature
                sign.initSign(privKey);
                String message = "Server: Hello client " + rsa.getPublicKey() + " " + rsa.getN();
                byte[] bytes = message.getBytes();
                //Adding data to the signature
                sign.update(bytes);
                //Calculating the signature
                byte[] signature = sign.sign();
                String encodedKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
                System.out.println("DSA Signature: " + Base64.getEncoder().encodeToString(signature));
                System.out.println("RSA Public Key: " + rsa.getPublicKey());

                return message + " /" + Base64.getEncoder().encodeToString(signature) + "&" + encodedKey;
            }

            if(input.contains("Key "))  //client has send algo.DES key
            {
                String message = input.replace("Key ", "");
                byte[] decode = Base64.getDecoder().decode(message);
                String b = new String(rsa.decrypt(decode));

                System.out.println("DES Key: " + b);
                byte[] decodedKey = Base64.getDecoder().decode(b);

                SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");
                des = new DES(originalKey);
                return "Server: Hello client!";
            }

            if (input.matches("Enter chat [0-9]+"))      // Request to enter a specific room
            {
                int i = input.lastIndexOf(" ");
                String key = input.substring(i + 1);
                output = enterRoom(Integer.parseInt(key));
                return output;
            }

            if (input.matches("^Username [A-Za-z0-9]+$"))   //Request to choose a username
            {
                int i = input.indexOf(" ");
                userName = input.substring(i + 1);
                Chat.getUsers().put(userName, writer);
                Chat.getUsersKey().put(writer, des);
                return "  Server: Username accepted";
            }

            if(input.matches("^Text [A-Za-z0-9]+:.+"))       //Request to send a message to a specific user
            {
                String receiver = input.substring(input.indexOf(" ") + 1, input.indexOf(":"));
                if(Chat.getUsers().containsKey(receiver))   // If this username exists
                {
                    String message = input.substring(input.indexOf(":") + 1);
                    sendMessage(message, Chat.getUsers().get(receiver));
                    return "  Server: Message sent!";
                }
                else return "  Server: This username doesn't exist!";
            }

            switch (input)
            {
                case "Bye":
                    output = "Bye!";
                    break;
                case "Create chat":
                    output = "Chat created, Key: ";
                    output += createRoom();
                    break;
                case "Get last chat":
                    output = chatKey + "";
                    break;
                default:
                    output = "Error in request";
                    break;
            }

            return "  Server: " + output;
        }
        else
        {
            if(input.equals("Exit!"))
                return "  Server: Bye!";

            if(input.equals("Exit chat"))
            {
                Chat.getRooms().get(chatKey).remove(writer);
                sendMessage(" has left the chat!");
                inChat = false;
                return "  Server: You left the chat!";
            }
            try
            {
                sendMessage(input);
                return "  Sent";
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return "";
    }

    private void sendMessage(String message, PrintWriter writer)  // in private
    {
        try
        {
            writer.println(encrypt(userName + ":" + message, Chat.getUsersKey().get(writer)));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        writer.flush();
    }

    private void sendMessage(String input) throws IOException          // in chat
    {
        ArrayList<PrintWriter> members;
        members = Chat.getRooms().get(chatKey);
        for(PrintWriter writer: members)
        {
            if(writer != this.writer)
            {
                DES des = Chat.getUsersKey().get(writer);
                writer.println(encrypt(userName + ":" + input, des));
                writer.flush();
            }
        }
    }

    public String encrypt(String input, DES userDes) throws IOException
    {
        //write to file
        FileWriter myWriter = new FileWriter(serverResponseClear);
        myWriter.write(input);
        myWriter.close();

        // encrypt output line
        userDes.encrypt(new FileInputStream(serverResponseClear), new FileOutputStream(serverResponseCipher));
        File file = new File(serverResponseClear);
        file.delete();

        //read from file
        Path path = Paths.get(serverResponseCipher);
        byte[] bytes = null;
        bytes = Files.readAllBytes(path);

        String outputLine = Base64.getEncoder().encodeToString(bytes);
        File file1 = new File(serverResponseCipher);
        file1.delete();
        return outputLine;
    }

    private String enterRoom(int key)
    {
        if(Chat.getRooms().containsKey(key))       //daca exista asa cheie in hashMap
        {
            Chat.getRooms().get(key).add(writer);
            this.chatKey = key;
            inChat = true;
            try
            {
                sendMessage(" has entered the chat");
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
            return "  Server: This key is invalid!";
        return "  Server: You entered the chat";
    }

    private String createRoom()
    {
        ArrayList<PrintWriter> members = new ArrayList<>();
        members.add(writer);

        int key = (int)(Math.random() * ((200 - 100) + 1)) + 100;
        while(Chat.getRooms().containsKey(key))
            key = (int)(Math.random() * ((200 - 100) + 1)) + 100;
        Chat.getRooms().put(key, members);
        this.chatKey = key;
        inChat = true;
        return key + "";
    }

    public DES getDES()
    {
        return des;
    }

    public String decrypt(String inputLine) throws FileNotFoundException
    {
        //write to file
        byte[] decode = Base64.getDecoder().decode(inputLine);
        try (FileOutputStream stream = new FileOutputStream(primitCodat))
        {
            stream.write(decode);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // decrypt fromUser
        try
        {
            des.decrypt(new FileInputStream(primitCodat), new FileOutputStream(primitDecodat));
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        //read from file
        File myObj = new File(primitDecodat);
        Scanner myReader = new Scanner(myObj);
        inputLine = myReader.nextLine();                // mesaj decriptat de la user*/
        myObj.delete();
        return inputLine;
    }
}