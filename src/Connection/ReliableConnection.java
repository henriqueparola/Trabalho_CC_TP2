package Connection;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.*;

public class ReliableConnection {
    private DatagramSocket socket;
    private final int MTU = 1400; // TODO: Verificar o valor certo
    private InetAddress peerAddress;
    private int peerPort;
    private int seq;

    public ReliableConnection(InetAddress inetPeer1, int portPeer1) throws SocketException {
        this.peerAddress = inetPeer1;
        this.peerPort = portPeer1;
        this.socket = new DatagramSocket(5000);
        this.seq = 0;
    }
    public void send(byte[] data) throws IOException {
        // Se data for maior que MTU é necessário fazer a divisão por vários pacotes
        // Como agora estou a fazer stop and wait preciso de receber um ack para cada pacote enviado
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
       DataInputStream  dis = new DataInputStream(new BufferedInputStream(bais));
        byte dataOut[] = new byte[MTU];
        // Recebe-se um Frame e não dados
        ConnectionFrame frameIn;
        // Para saber se se pode saltar o ciclo de tentativas
        boolean received = false;
        // Máximas tentativas de timeout
        final int maxTries = 5;
        // para saber o tamanho lido da stream
        int size = 0;
        Callable<ConnectionFrame> callable = new Callable<ConnectionFrame>() {
            @Override
            public ConnectionFrame call() throws Exception {
                return rdtRcvPckt();
            }
        };



        ExecutorService executor = Executors.newSingleThreadExecutor();

        while ((size = dis.read(dataOut)) != -1) {
            udtSendPckt(size, dataOut, this.seq);
            System.out.println("ciclo");
            while (!received) {
                Future<ConnectionFrame> future = executor.submit(callable);
                try {
                    frameIn = future.get();
                    if (notCurrupt(frameIn) && isAck(frameIn, this.seq + 1)) {
                        received = true;
                        System.out.println("received = " + received);
                        this.seq++;
                    }
                } catch (InterruptedException | ExecutionException e) {

                }finally {
                    future.cancel(true);
                }

            }

            received = false;

            if(size < MTU) {
                bais.close();
                dis.close();
            }

        }

        System.out.println("hjhjhjh");
        executor.shutdownNow();

    }

    private boolean sameRecipient(InetAddress address, int port) {
        return address.equals(this.peerAddress) && this.peerPort == port;
    }

    private boolean isAck(ConnectionFrame frame, int seq) {
        //return frame.tag == seq && frame.dataLen == 0;
        return true;
    }

    private boolean notCurrupt(ConnectionFrame frame) {
        return true;
    }

    private void udtSendPckt(int size, byte[] data, int seq) throws IOException {
        ConnectionFrame outFrame = new ConnectionFrame(seq, size, data);


        byte[] frameOut;
        frameOut = outFrame.serialize();
        outFrame = ConnectionFrame.deserealize(frameOut);
        System.out.println("size" + outFrame.dataLen);
        frameOut = outFrame.serialize();
        DatagramPacket outPacket = new DatagramPacket(frameOut,
                                                    frameOut.length,
                                                    this.peerAddress,
                                                    this.peerPort);
        socket.send(outPacket);
    }

    private ConnectionFrame rdtRcvPckt() throws IOException {
        byte dataIn[] = new byte[ConnectionFrame.MTU];
        DatagramPacket inPacket = new DatagramPacket(dataIn, dataIn.length);
        socket.receive(inPacket);

        while(!sameRecipient(inPacket.getAddress(), inPacket.getPort()))
            socket.receive(inPacket);

        ConnectionFrame inFrame = ConnectionFrame.deserealize(inPacket.getData());
        return inFrame;
    }

    public byte[] receive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(MTU);
        DataOutputStream dos  = new DataOutputStream(new BufferedOutputStream(baos));

        boolean flag = true;
        ConnectionFrame inFrame;

        while (flag) {
            inFrame = rdtRcvPckt();
            System.out.println("Recebi pacote " + inFrame.dataLen + "this.MTU = " + this.MTU);
            if (inFrame.dataLen < this.MTU) flag = false;

            if (notCurrupt(inFrame) && validSeq(inFrame)) {
                System.out.println("wrote");
                dos.write(inFrame.data);
                this.seq++;
            }
        }
            sendAck();

        dos.close();
        baos.close();
        return baos.toByteArray();
    }

    private void sendAck() throws IOException {
        ConnectionFrame ackFrame = new ConnectionFrame(this.seq, 0, null);
        System.out.println("Mandei ack");
        byte[] dataOut = ackFrame.serialize();
        DatagramPacket outPacket = new DatagramPacket(dataOut,
                                                    dataOut.length,
                                                    this.peerAddress,
                                                    this.peerPort);
        socket.send(outPacket);
    }

    private boolean validSeq(ConnectionFrame inFrame) {
        return true;
    }


}
