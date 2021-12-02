package Server;

import Connection.ConnectionFrame;
import Connection.ReliableConnection;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        // OPCODE de ACK
        byte[] bufAck = new byte[2];

        Path path = Paths.get("./" + folderToSync);

        Stream<Path> walk = null;
        try {
            walk = Files.walk(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Path> paths;
        paths = walk.filter(Files::isRegularFile).collect(Collectors.toList());

        byte[] data = new byte[0];
        try {
            data = serialize(paths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String data2 = new String(data, StandardCharsets.UTF_8);
        //System.out.printf("Array Result: " + data2);

        try {
            ReliableConnection rb = new ReliableConnection(destAdress,destPort);
            rb.send(data);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static byte[] serialize(List <Path> paths) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(ba);

        dos.writeInt(paths.size());
        for (Path path : paths){
            dos.writeUTF(path.toString().substring(folderToSync.length() + 3));
        }
        dos.close();
        return ba.toByteArray();
    }

}
