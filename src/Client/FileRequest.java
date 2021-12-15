package Client;

import Connection.ReliableConnection;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Multiplex.ProtocolFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class FileRequest implements Runnable{
    private String ipToSync;
    private MetaData fileMetaData;
    private String folderToSync;
    private final int maxBlockSize = 1024 * 1024 * 2; //2 Mb

    public FileRequest(String ipToSync, MetaData fileMetaData, String folderToSync) {
        this.ipToSync = ipToSync;
        this.fileMetaData = fileMetaData;
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        try {
            ProtocolLogger2 pl = ProtocolLogger2.getInstance();
            ReliableConnection rb = new ReliableConnection(InetAddress.getByName(ipToSync), 5000);
            byte[] data = fileMetaData.getFilePath().getBytes(StandardCharsets.UTF_8);
            ProtocolFrame pf2 = new ProtocolFrame((byte) 0x1, data.length, data);
            pl.loggerInfo("Pedindo ficheiro " + fileMetaData.getFilePath() + " ao " + ipToSync);
            rb.send(pf2.serialize());

            int port = rb.socket.getLocalPort();
            rb.close();

            ReliableConnection rb2 = new ReliableConnection(port);
            File file = new File(folderToSync + "/" + fileMetaData.getFilePath());
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            ProtocolFrame pf = null;

            int blocoFicheiro = 0;
            int len = maxBlockSize;
            if (fileMetaData.getSize() > 0)  {
                do {
                    byte[] dataReceive = rb2.receive();

                    pf = ProtocolFrame.deserialize(dataReceive);
                    os.write(pf.data);
                    pl.loggerInfo("Recebido bloco + " + blocoFicheiro++ + " do ficheiro " + fileMetaData.getFilePath());
                    len = pf.datLength;

                } while(len == maxBlockSize);
            }

            rb2.close();
            os.close();
            pl.loggerInfo("Recebido ficheiro " + fileMetaData.getFilePath());
            FolderStruct fd = FolderStruct.getInstance();
            fd.changeState(ipToSync, fileMetaData.getFilePath());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
