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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeoutException;

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

            int port;
            try {
                rb.send(pf2.serialize());
                port = rb.socket.getLocalPort();
            }finally {
                rb.close();
            }

            ReliableConnection rb2 = new ReliableConnection(port);
            File file = new File(folderToSync + "/" + fileMetaData.getFilePath());
            file.createNewFile();

            OutputStream os = new FileOutputStream(file);
            ProtocolFrame pf = null;

            int blocoFicheiro = 0;
            int len = 0;
            if (fileMetaData.getSize() > 0)  {
                do {
                    byte[] dataReceive = rb2.receive();

                    pf = ProtocolFrame.deserialize(dataReceive);
                    os.write(pf.data);
                    pl.loggerInfo("Recebido bloco " + blocoFicheiro++ + " do ficheiro " + fileMetaData.getFilePath());
                    len += pf.datLength;

                } while(len < fileMetaData.size);
            }

            file.setLastModified(fileMetaData.getModified());
            Files.setAttribute(
                    Path.of(folderToSync + "/" + fileMetaData.getFilePath()),
                    "creationTime",
                    FileTime.fromMillis(fileMetaData.creationDate)
            );

            rb2.close();
            os.close();
            pl.loggerInfo("Recebido ficheiro " + fileMetaData.getFilePath());
            FolderStruct fd = FolderStruct.getInstance();
            fd.changeState(ipToSync, fileMetaData.getFilePath());

            if(fd.checkIfFinToIp(ipToSync) == true){
                // this was the last file to request to this ip, so
                // I have to send FIN package
                ReliableConnection rb3 = new ReliableConnection(InetAddress.getByName(ipToSync), 5000);
                ProtocolFrame pf3 = new ProtocolFrame((byte) 0x3, 0, null);
                pl.loggerInfo("Enviando FIN ao " + ipToSync);

                try {
                    rb3.send(pf3.serialize());
                }finally {
                    rb3.close();
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            //System.out.println("Send deu Timeout. Undefined Behaviour. (file request)");
        }
    }
}
