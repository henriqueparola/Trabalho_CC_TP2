package Client;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

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

                String data = new String(syncAnswerPacket.getData(), StandardCharsets.UTF_8);
                System.out.println(data);

                socket.send(new DatagramPacket(
                        bufAck,
                        bufAck.length,
                        InetAddress.getByName(ipToSync),
                        syncAnswerPacket.getPort()) // Atenção aqui porque não é mais a porta de atendimento que está a ser usada
                );
            }

        }catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
