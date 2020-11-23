import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;

public class ServerThreadHelper implements Runnable
{
    Socket socket;
    public ServerThreadHelper(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine, outputLine;

            MyProtocol myProtocol = new MyProtocol(writer);

            outputLine = myProtocol.processInput(null);  // se transmite public key rsa la client {Server: Hello client 23112313 12313113}
            writer.println(outputLine);
            writer.flush();

            while ((inputLine = reader.readLine()) != null)       // atita timp cit clientul trimite pachete
            {
                if (!inputLine.contains("Key "))      // daca nu contine key inseamna ca vine mesaj criptat
                    inputLine = myProtocol.decrypt(inputLine);

                outputLine = myProtocol.processInput(inputLine);    // proceseaza raspunsul
                outputLine = myProtocol.encrypt(outputLine, myProtocol.getDES());

                writer.println(outputLine);
                writer.flush();

                if (outputLine.equals("  Server: Bye!"))
                    break;
            }

            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}