import Client.Client;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Security.Authentication;
import Server.Server;
import Client.FolderStruct;

import java.io.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {

        int numberOfIps;
        int startIp;
        String folderToSync;

        ProtocolLogger2 p = ProtocolLogger2.getInstance();
        if (args[0].equals("--logs")){
            p.setActive();
            folderToSync = args[1];
            startIp = 2;
            numberOfIps = args.length - 2;
        }else{
            numberOfIps = args.length - 1;
            folderToSync = args[0];
            startIp = 1;
        }

        // Get all Ip's to sync
        String[] ipsToSync = new String[numberOfIps];
        for (int i = 0; i < numberOfIps; i++){
            ipsToSync[i] = args[startIp + i];
        }

        // Get credencials
        Scanner sc= new Scanner(System.in); //System.in is a standard input stream.
        System.out.println("> senha: ");
        String str= sc.nextLine(); //reads string
        Authentication auth = Authentication.getInstance();
        auth.setPasswordInsert(str);

        FolderStruct fd = FolderStruct.getInstance();
        fd.addMyList(folderToSync);

        Thread tServer = new Thread(new Server(ipsToSync,folderToSync));
        Thread tClient = new Thread(new Client(ipsToSync,folderToSync));

        long start = System.currentTimeMillis();
        tServer.start();
        tClient.start();

        try {
            tServer.join(); // only finish with all of others ips will not send requests anymore
            tClient.join(); // only finish when I will not request for anyone nothing more
            long end = System.currentTimeMillis();
            p.loggerInfo("sync complete - time: " + (end - start)/1000 + " seconds");
            System.out.println( "> sync "+"\u001B[32m"+ "complete!" + "\u001B[0m" + " time: " + (end - start)/1000 + " seconds");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
