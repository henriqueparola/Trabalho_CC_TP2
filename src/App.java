import Client.Client;
import Server.Server;

import java.io.*;

public class App {
    public static void main(String[] args) throws IOException {

        // Get all Ip's to sync
        String[] ipsToSync = new String[args.length - 1];
        for (int i = 0; i < args.length - 1; i++)
            ipsToSync[i] = args[i + 1];

        // Get the folder to sync
        String folderToSync = args[0];

        Thread tServer = new Thread(new Server(folderToSync));
        Thread tClient = new Thread(new Client(ipsToSync,folderToSync));

        tServer.start();
        tClient.start();
    }
}
