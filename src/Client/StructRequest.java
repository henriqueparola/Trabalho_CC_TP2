package Client;

import Connection.ReliableConnection;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StructRequest implements Runnable{
    private String ipToSync;
    FolderStruct folderStruct; // TODO prencher folder

    StructRequest(String ipToSync,FolderStruct folderStruct){
        this.ipToSync = ipToSync;
        this.folderStruct = folderStruct;
    }

    @Override
    public void run() {
        try{
            // OPCODE de sincronização
            byte[] buf = new byte[] {(byte)0x0, (byte)0x1};
            // OPCODE de ACK
            byte[] bufAck = new byte[]{(byte)0x0,(byte)0x2};

            // 5000 será a porta de atendimento padrão do nosso protocolo
            DatagramPacket syncPacket = new DatagramPacket(buf,buf.length,InetAddress.getByName(ipToSync),5000);
            DatagramSocket socket = new DatagramSocket();

            // Envio do pacote de sincronização para a porta de atendimento do computador a sincronizar
            socket.send(syncPacket);

            byte[] bufData = new byte[1500];
            DatagramPacket syncAnswerPacket = new DatagramPacket(bufData,bufData.length);
            while(true) {
                socket.receive(syncAnswerPacket);
                List <String> list = deserialize(syncAnswerPacket.getData());
                System.out.println(list.toString());

                ReliableConnection rb = new ReliableConnection(InetAddress.getByName(ipToSync),syncAnswerPacket.getPort());
                // Mandar Ack
            }

        }catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static List<String> deserialize(byte[] bytes) throws IOException {
        List<String> listPaths = new ArrayList<>();

        ByteArrayInputStream ba = new ByteArrayInputStream(bytes);
        DataInputStream dos = new DataInputStream(ba);

        int quant = dos.readInt();

        for(int i = 0; i < quant; i++){
            listPaths.add(dos.readUTF());
        }

        return listPaths;
    }
}
