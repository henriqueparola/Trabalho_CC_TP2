package Multiplex;

import Client.FolderStruct;
import Connection.ReliableConnection;
import Server.StructReply;

import java.io.IOException;
import java.net.SocketException;

public class ProtocolDemultiplexer implements Runnable {
    FolderStruct fd;
    String folderToSync;

    public ProtocolDemultiplexer(FolderStruct fd,String folderToSync) {
        this.fd = fd;
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        boolean running = true;
        try {
            while (running) {
                ReliableConnection rb = new ReliableConnection(5000);
                byte[] data = rb.receive();
                ProtocolFrame pf = ProtocolFrame.deserialize(data);
                switch (pf.opcode){
                    case 0x0:
                        Thread t = new Thread(new StructReply(rb.peerAddress,rb.peerPort,folderToSync));
                        t.start();
                    case 0x1:
                        // TODO  File Reply
                }

                rb.socket.close();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
