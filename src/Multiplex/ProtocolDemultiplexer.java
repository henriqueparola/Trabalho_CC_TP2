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
import java.util.*;

public class ProtocolDemultiplexer implements Runnable {
    String[] ipsToSync;
    String folderToSync;
    Set<String> syncRequests = new HashSet<>();
    Map<String, Set<String>> filesAsked = new HashMap<>();

    public ProtocolDemultiplexer(String[] ipsToSync,String folderToSync) {
        this.ipsToSync = ipsToSync;
        this.folderToSync = folderToSync;
    }
    private void addFile(String ip, String file) {
        Set<String> set;
       if (filesAsked.containsKey(ip))
           set = filesAsked.get(ip);
       else {
           set = new HashSet<>();
           filesAsked.put(ip, set);
       }

       set.add(file);
    }

    private boolean fileAsked(String ip, String file) {
        if (filesAsked.containsKey(ip)) {
            Set<String> set = filesAsked.get(ip);
            if (set.contains(file)) return true;
        }
        return false;
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
                        if(!syncRequests.contains(rb.peerAddress.getHostAddress())) {
                            syncRequests.add(rb.peerAddress.getHostAddress());
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
                        String filePath = new String(pf.data, StandardCharsets.UTF_8);
                        if (!fileAsked(rb.peerAddress.getHostAddress(), filePath)) {
                            addFile(rb.peerAddress.getHostAddress(), filePath);
                            Thread t2 = new Thread(new FileReply(
                                    rb.peerAddress,
                                    rb.peerPort,
                                    folderToSync,
                                    filePath
                            ));
                            t2.start();
                        }

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
