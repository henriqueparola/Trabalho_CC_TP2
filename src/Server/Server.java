package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server implements Runnable{
    String folderToSync;

    public Server(String folderToSync){this.folderToSync = folderToSync;}

    @Override
    public void run() {
        try{
            // Porta de atendimento
            DatagramSocket socket = new DatagramSocket(5000);

            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            boolean running = true;
            while (running) {
                socket.receive(packet);
                System.out.println("Recebi Sync!");
                // TODO parser do OPCODE (Sync ou Read)
                // Cada Thread Cria sua própria porta de dados para simular a ideia de "conexão" com o cliente
                // new thread multiplexer (ficheiros[])
                Thread t = new Thread(new StructReply(packet.getAddress(), packet.getPort(), folderToSync));

                t.start();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
