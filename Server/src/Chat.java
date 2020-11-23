
import algo.DES;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Chat
{
    private static volatile Chat instance;
    private static volatile HashMap<Integer, ArrayList<PrintWriter>> rooms;
    private static volatile HashMap<String, PrintWriter> users;
    private static volatile HashMap<PrintWriter, DES> usersKey;

    private Chat()
    {
        rooms = new HashMap<Integer, ArrayList<PrintWriter>>();
        users = new HashMap<String, PrintWriter>();
        usersKey = new HashMap<PrintWriter, DES>();
    }

    public static Chat getInstance()
    {
        if(instance == null)
        {
            synchronized (Chat.class)
            {
                if (instance == null)
                    instance = new Chat();
            }
        }
        return instance;
    }

    public static HashMap<Integer, ArrayList<PrintWriter>> getRooms()
    {
        return rooms;
    }

    public static HashMap<String, PrintWriter> getUsers()
    {
        return users;
    }

    public static HashMap<PrintWriter, DES> getUsersKey()
    {
        return usersKey;
    }
}