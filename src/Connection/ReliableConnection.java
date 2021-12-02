package Connection;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ReliableConnection {
    private DatagramSocket socket;
    private final int MTU = 1400; // TODO: Verificar o valor certo
    private InetAddress peerSenderAddress;
    private InetAddress peerReceiverAddress;
    private int peerSenderPort;
    private int peerReceiverPort;
    private int seq;

    public ReliableConnection(InetAddress inetPeer1, InetAddress inetPeer2, int portPeer1, int portPeer2) throws SocketException {
        this.peerSenderAddress = inetPeer1;
        this.peerReceiverAddress = inetPeer2;
        this.peerSenderPort = portPeer1;
        this.peerReceiverPort = portPeer2;
        this.socket = new DatagramSocket();
        this.seq = 0;
    }
    public void send(byte[] data) throws IOException {
        // Se data for maior que MTU é necessário fazer a divisão por vários pacotes
        // Como agora estou a fazer stop and wait preciso de receber um ack para cada pacote enviado
       DataInputStream  dis = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));


        byte dataOut[] = new byte[MTU];
        // Recebe-se um Frame e não dados
        byte dataIn[] = new byte[MTU + 4];
        byte frameOut[];


        while (dis.read(dataOut) != -1) {
            ConnectionFrame outFrame = new ConnectionFrame(seq++, dataOut);
            frameOut = outFrame.serialize();
            DatagramPacket outPacket = new DatagramPacket(frameOut,
                                                        frameOut.length,
                                                        this.peerReceiverAddress,
                                                        this.peerReceiverPort);
            socket.send(outPacket);


            DatagramPacket inPacket = new DatagramPacket(dataIn, dataIn.length);
            socket.receive(inPacket);
            ConnectionFrame inFrame = ConnectionFrame.deserealize(inPacket.getData());
            //Sei que recebi um ack, no entanto convém verificar se data é null ou se data size = 0;
            //Como é stopwait não preciso de saber qual é o numero da seq
        }
    }

    public byte[] receive() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(MTU);
        byte dataIn[] = new byte[MTU + 4];
        // provavelmente mudar isto
        byte dataOut[] = new  byte[0];
        DatagramPacket inPacket = new DatagramPacket(dataIn, dataIn.length);
        boolean flag = true;
        while (flag) {
            socket.receive(inPacket);
            ConnectionFrame inFrame = ConnectionFrame.deserealize(inPacket.getData());
            if (inFrame.data.length == 0) flag = false;
            else {
                bos.write(inFrame.data);
                ConnectionFrame outFrame = new ConnectionFrame(0, dataOut);
                byte []frameOut = outFrame.serialize();
                DatagramPacket outPacket = new DatagramPacket(frameOut,
                                                            frameOut.length,
                                                            this.peerSenderAddress,
                                                            this.peerSenderPort);
            }
        }
        return bos.toByteArray();
    }


}
