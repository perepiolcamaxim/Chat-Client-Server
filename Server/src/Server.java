import java.io.IOException;
import java.net.*;
import java.net.Socket;

public class Server
{
    void start()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(5000);

            while (true)
            {
                Socket socket = serverSocket.accept();
                ServerThreadHelper helper = new ServerThreadHelper(socket);
                Thread thread = new Thread(helper);
                thread.start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        new Server().start();
    }
}
