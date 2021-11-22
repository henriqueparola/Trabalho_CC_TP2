package Server;

import java.awt.*;
import java.net.*;
import java.io.*;
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
    private String folderToSync;

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

        try {
            DatagramSocket datagramSocket = new DatagramSocket(); // Porta de transferência de dados que vai simular "comexão" com o cliente
            for (Path p : paths){
                // TODO Otimizar obordagem. Inicialmente estamos a enviar um path por pacote.
                // TODO Adicionar meta-dados.
                DatagramPacket datagramPacketAnswer = new DatagramPacket(
                        p.toString().substring(folderToSync.length() + 3).getBytes(StandardCharsets.UTF_8),
                        p.toString().substring(folderToSync.length() + 3).getBytes(StandardCharsets.UTF_8).length,
                        this.destAdress,
                        this.destPort
                );
                datagramSocket.send(datagramPacketAnswer);

                // TODO Controlo de erros será aqui
                datagramSocket.receive(new DatagramPacket(
                        bufAck,
                        bufAck.length
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
