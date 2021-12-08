package Server;

import Connection.ReliableConnection;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Multiplex.ProtocolFrame;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;

public class FileReply implements Runnable{
    private InetAddress destAdress;
    private int destPort;
    private String folderToSync;
    private String filePath;

    public FileReply(InetAddress destAdress, int destPort, String folderToSync, String filePath) throws SocketException {
        this.filePath = filePath;
        this.destAdress = destAdress;
        this.destPort = destPort;
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        try {
            ProtocolLogger2 pl = ProtocolLogger2.getInstance();
            InputStream is = new FileInputStream(folderToSync + "/" +filePath);

            int bytesToRead = 4096 * 100; // 4096 Kb
            byte[] data = new byte[bytesToRead];

            ReliableConnection rb = new ReliableConnection(this.destAdress,this.destPort);

            // TODO while(is.read(data) > 0){
            is.read(data);
            ProtocolFrame frame = new ProtocolFrame((byte) 0x2,data.length,data);

            pl.loggerInfo("Enviando ficheiro " + filePath + " para o " + destAdress);
            rb.send(frame.serialize());
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
