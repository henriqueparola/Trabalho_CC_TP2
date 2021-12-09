package Multiplex;

import Client.FolderStruct;
import Connection.ReliableConnection;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Server.FileReply;
import Server.StructReply;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class ProtocolDemultiplexer implements Runnable {
    String folderToSync;

    public ProtocolDemultiplexer(String folderToSync) {
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        ProtocolLogger2 pl = ProtocolLogger2.getInstance();
        boolean running = true;
        try {
            while (running) {
                ReliableConnection rb = new ReliableConnection(5000);
                byte[] data = rb.receive();
                ProtocolFrame pf = ProtocolFrame.deserialize(data);
                switch (pf.opcode){
                    case 0x0:
                        //System.out.println("SYNC");
                        pl.loggerInfo("SYNC recebido do " + rb.peerAddress);
                        Thread t = new Thread(new StructReply(
                                rb.peerAddress,
                                rb.peerPort,
                                folderToSync
                        ));
                        t.start();
                        break;
                    case 0x1:
                        //System.out.println("READ");
                        pl.loggerInfo("READ recebido do " + rb.peerAddress);
                        Thread t2 = new Thread(new FileReply(
                                rb.peerAddress,
                                rb.peerPort,
                                folderToSync,
                                new String(pf.data, StandardCharsets.UTF_8)
                        ));
                        t2.start();
                        break;
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
