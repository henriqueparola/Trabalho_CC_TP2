package Server;

import Client.FolderStruct;
import Logger.ProtocolLogger;
import Multiplex.ProtocolDemultiplexer;

public class Server implements Runnable{
    String folderToSync;

    public Server(String folderToSync){
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        FolderStruct fd = new FolderStruct();
        // TODO Thread Http
        Thread t = new Thread(new ProtocolDemultiplexer(fd,folderToSync));
        t.start();
    }
}
