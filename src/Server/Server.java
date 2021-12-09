package Server;

import Client.FolderStruct;
import Multiplex.ProtocolDemultiplexer;
import Server.Http.HttpServer;

public class Server implements Runnable{
    String folderToSync;

    public Server(String folderToSync){
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        FolderStruct fd = new FolderStruct();
        Thread t2 = new Thread(new HttpServer(fd));
        Thread t = new Thread(new ProtocolDemultiplexer(fd,folderToSync));

        t.start();
        t2.start();
    }
}
