package Server;

import Connection.ReliableConnection;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Multiplex.ProtocolFrame;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

public class FileReply implements Runnable{
    private InetAddress destAdress;
    private int destPort;
    private String folderToSync;
    private String filePath;
    private final int bytesToRead = 1024  * 1024 * 2; // 2 Mb

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
            File file = new File(folderToSync + "/" + filePath);
            InputStream is = new FileInputStream(file);

            //Visto que esta fase é stop and wait o objetivo é conseguir enviar
            //uma quantidade razoavel de informação por blocos.
            //Uma vez que o send usa janelas.

            ReliableConnection rb = new ReliableConnection(this.destAdress,this.destPort);

            pl.loggerInfo("Enviando ficheiro " + filePath + " para o " + destAdress);
            int blocoFicheiro = 0;
            int size;

            try {
                if (file.length() > 0) {

                    byte[] data = new byte[bytesToRead];
                    while ((size = is.read(data)) != -1) {
                        pl.loggerInfo("Enviando bloco " + blocoFicheiro++ + " do ficheiro " + filePath + " para o " + destAdress);
                        ProtocolFrame frame = new ProtocolFrame((byte) 0x2, size, data);
                        rb.send(frame.serialize());
                    }
                }
            }finally {
                rb.close();
                is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            //System.out.println("Timeout file reply");
        }
    }
}
