package Server;

import Client.FolderStruct;
import Client.MetaData;
import Connection.ReliableConnection;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Multiplex.ProtocolFrame;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructReply implements Runnable {
    private InetAddress destAdress;
    private int destPort;
    static private String folderToSync;

    public StructReply(InetAddress destAdress, int destPort, String folderToSync) throws SocketException {
        this.folderToSync = folderToSync;
        this.destAdress = destAdress;
        this.destPort = destPort;
    }

    @Override
    public void run() {
        ProtocolLogger2 pl = ProtocolLogger2.getInstance();
        FolderStruct fs = FolderStruct.getInstance();
        List<MetaData> metaDataPaths = fs.getMyFolder();
        byte[] data = new byte[0];

        try {
            data = serialize(metaDataPaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ReliableConnection rb = new ReliableConnection(this.destAdress,this.destPort);
            ProtocolFrame pf = new ProtocolFrame((byte)0x2,data.length,data);
            pl.loggerInfo("Enviando estrutura de pastas para o " + destAdress);
            try{
                rb.send(pf.serialize());
            }finally {
                rb.close();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            //System.out.println("Send deu Timeout. Undefined behaviour. (struct reply)");
        }
    }
    /*
    serialize a List of paths
     */
    static byte[] serialize(List <MetaData> metaPaths) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(ba);

        dos.writeInt(metaPaths.size());

        for (MetaData metaPath : metaPaths){
            dos.write(metaPath.serialize());
        }

        dos.close();
        return ba.toByteArray();
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

    /*
    Get the MetaData from a path
     */
    private MetaData getMetaData(Path path){
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            return new MetaData(
                    path.toString().substring(folderToSync.length() + 3),
                    attr.size(),
                    attr.creationTime().toMillis(),
                    attr.lastModifiedTime().toMillis()
            );
        }catch (IOException e){
        }
        return null;
    }

}
