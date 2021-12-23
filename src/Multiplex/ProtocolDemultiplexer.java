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
    String[] ipsToSync;
    String folderToSync;

    public ProtocolDemultiplexer(String[] ipsToSync,String folderToSync) {
        this.ipsToSync = ipsToSync;
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        FolderStruct fs = FolderStruct.getInstance();
        fs.initOthersState(ipsToSync);
        ProtocolLogger2 pl = ProtocolLogger2.getInstance();

        boolean running = true;
        try {
            while (!fs.checkIfAllSendFin()) {
                ReliableConnection rb = new ReliableConnection(5000);
                byte[] data = rb.receive();
                ProtocolFrame pf = ProtocolFrame.deserialize(data);
                switch (pf.opcode){
                    case 0x0:
                        if(!fs.syncRequest.contains(rb.peerAddress.getHostAddress())) {
                            fs.syncRequest.add(rb.peerAddress.getHostAddress());
                            pl.loggerInfo("SYNC recebido do " + rb.peerAddress);
                            Thread t = new Thread(new StructReply(
                                    rb.peerAddress,
                                    rb.peerPort,
                                    folderToSync
                            ));
                            t.start();
                        }
                        break;
                    case 0x1:
                        pl.loggerInfo("READ recebido do " + rb.peerAddress);
                        Thread t2 = new Thread(new FileReply(
                                rb.peerAddress,
                                rb.peerPort,
                                folderToSync,
                                new String(pf.data, StandardCharsets.UTF_8)
                        ));
                        t2.start();
                        break;
                    case 0x3:
                        pl.loggerInfo("FIN recebido do " + rb.peerAddress);
                        fs.changeOtherIpState(rb.peerAddress.getHostAddress());
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
