package Client;

import Connection.ReliableConnection;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Multiplex.ProtocolFrame;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class StructRequest implements Runnable{
    private String ipToSync;
    FolderStruct folderStruct = FolderStruct.getInstance();

    StructRequest(String ipToSync){
        this.ipToSync = ipToSync;
    }

    @Override
    public void run() {
        try {
            ProtocolLogger2 pl = ProtocolLogger2.getInstance();
            ReliableConnection rb = new ReliableConnection(InetAddress.getByName(ipToSync), 5000);
            ProtocolFrame pf2 = new ProtocolFrame((byte) 0x0,0,null);

            pl.loggerInfo("Requisitando estrutura de pastas do " + ipToSync);
            int port;
            try {
                rb.send(pf2.serialize());
                port = rb.socket.getLocalPort();
            }finally {
                rb.close();
            }

            ReliableConnection rb2 = new ReliableConnection(port);
            byte[] data = rb2.receive();
            pl.loggerInfo("Recebida a estrutura de pastas do " + ipToSync);

            ProtocolFrame pf = ProtocolFrame.deserialize(data);
            List<MetaData> list = deserialize(pf.data);

            folderStruct.addList(ipToSync,list);
            rb2.close();
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (TimeoutException e) {
            //System.out.println("Send deu Timeout. Undefined Behaviour. (struct request)");
        }
    }

    static List<MetaData> deserialize(byte[] bytes) {
        List<MetaData> metaDataPaths = new ArrayList<>();
        ByteArrayInputStream ba = new ByteArrayInputStream(bytes);
        DataInputStream dos = new DataInputStream(ba);

        try{
            int quant = dos.readInt();
            for(int i = 0; i < quant; i++){
                MetaData metaData = new MetaData();
                metaData.deserialize(dos);
                metaDataPaths.add(metaData);
            }
            dos.close();
        }catch (IOException e){
        }
        return metaDataPaths;
    }
}
