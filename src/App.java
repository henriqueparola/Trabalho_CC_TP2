import Client.Client;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Security.Authentication;
import Server.Server;
import Client.FolderStruct;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {
    private static Map<Map.Entry<String, String>, Integer> bandwitdth = new HashMap<>() {
        {put(Map.entry("10.2.2.1", "10.3.3.1"), 1 * 1000 * 1000 * 1000);
         put(Map.entry("10.2.2.1", "10.4.4.1"), 100 * 1000 * 1000);
         put(Map.entry("10.3.3.1", "10.4.4.1"), 100 * 1000 * 1000);
        }
    };

    private static int getBand(String ip1, String ip2) {
        Integer r = 0;
        if ( (r = bandwitdth.get(Map.entry(ip1, ip2))) != null)
            return r;
        if ( (r = bandwitdth.get(Map.entry(ip2, ip1))) != null)
            return r;

        return r;
    }

    public static void main(String[] args) throws IOException {

        int numberOfIps;
        int startIp;
        String folderToSync;

        ProtocolLogger2 p = ProtocolLogger2.getInstance();
        if (args[0].equals("--logs")){
            p.setActive();
            folderToSync = args[1];
            startIp = 2;
            numberOfIps = args.length - 3;
        }else{
            numberOfIps = args.length - 2;
            folderToSync = args[0];
            startIp = 1;
        }

        // Get all Ip's to sync
        String[] ipsToSync = new String[numberOfIps];
        String myIp = args[startIp];
        for (int i = 0; i < numberOfIps; i++){
            ipsToSync[i] = args[startIp + 1 + i];
        }

        // check if folder exists (if not exists, create)
        if (!Files.exists(Path.of(folderToSync))){
            Files.createDirectories(Path.of(folderToSync));
        }

        // Get credencials
        Scanner sc= new Scanner(System.in); //System.in is a standard input stream.
        System.out.println("\u001B[33m" + "> " + "\u001B[0m" + "senha: ");
        String str= sc.nextLine(); //reads string
        Authentication auth = Authentication.getInstance();
        auth.setPasswordInsert(str);

        FolderStruct fd = FolderStruct.getInstance();
        fd.addMyList(folderToSync);

        Thread tServer = new Thread(new Server(ipsToSync,folderToSync));
        Thread tClient = new Thread(new Client(ipsToSync,folderToSync));

        tServer.start();
        tClient.start();

        try {
            tServer.join(); // only finish with all of others ips will not send requests anymore
            tClient.join(); // only finish when I will not request for anyone nothing more
            long timeSync = System.currentTimeMillis() - fd.initTime;
            long timeDischarge = fd.endDischargeTime - fd.initTime;
            double totalIdealTime = 0;
            long totalFileSize = 0;

            for (String ip : ipsToSync) {
                //System.out.println("LOCAL IP : " + myIp);
                long band = getBand(myIp, ip);
                //System.out.println("BAND : " + band);
                long filesSize = fd.getFilesSizeByIp(ip) * 8;
                totalFileSize += filesSize;
                //System.out.println("FILES_SIZE : " + filesSize);
                totalIdealTime += ((double)filesSize/(double)band);
            }

            //System.out.println("Total File Size " + totalFileSize);e);

            double bpsReal = totalFileSize / ((double) timeDischarge / 1000);
            double bpsIdeal = (totalIdealTime == 0)? Double.MAX_VALUE : totalFileSize / totalIdealTime;
            double efective = (bpsIdeal == 0)? Double.MAX_VALUE : bpsReal/bpsIdeal;

            System.out.println("\u001B[33m" + "> " + "\u001B[0m" + "sync " + "\u001B[32m"+ "complete!" + "\u001B[0m" + " time: " + timeSync + " ms");
            System.out.println("\u001B[33m" + "> " + "\u001B[0m" + "Ideal discharge time: " + totalIdealTime + " ms");
            System.out.println("\u001B[33m" + "> " + "\u001B[0m" + "Ideal Bps: " + bpsIdeal + " (bits/seg)");
            System.out.println("\u001B[33m" + "> " + "\u001B[0m" + "Real discharge time: " + timeDischarge + " ms");
            System.out.println("\u001B[33m" + "> " + "\u001B[0m" + "Real Bps: " + bpsReal + " (bits/seg)");
            System.out.println("\u001B[33m" + "> " + "\u001B[0m" + "Efficiency: " + efective);

            p.loggerInfo("Ideal Bps: " + bpsIdeal);
            p.loggerInfo("Real Bps: " + bpsReal);
            p.loggerInfo("Real discharge time: " + timeDischarge);
            p.loggerInfo("Efficiency: " + efective);
            p.loggerInfo("sync complete - time: " + timeSync + " ms");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
