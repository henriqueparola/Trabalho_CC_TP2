package Server;

import Client.FolderStruct;
import Multiplex.ProtocolDemultiplexer;
import Server.Http.HttpServer;

public class Server implements Runnable{
    String[] ipsToSync;
    String folderToSync;

    public Server(String[] ipsToSync,String folderToSync){
        this.ipsToSync = ipsToSync;
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        FolderStruct fd = new FolderStruct();
        Thread t2 = new Thread(new HttpServer());
        Thread t = new Thread(new ProtocolDemultiplexer(ipsToSync,folderToSync));

        t.start();
        t2.start();

        try {
            t.join(); // only finish with all of others ips will not send requests anymore
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
