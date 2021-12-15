package Connection;

import Security.CreatePassword;
import Security.RequestInterceptor;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class ReliableConnection {
    public DatagramSocket socket;
    private final int MTU = 1300;
    public InetAddress peerAddress;
    public int peerPort;
    private int seq;
    private String key = "key";

    public ReliableConnection(int port,InetAddress inetPeer1, int portPeer1) throws SocketException {
        this.peerAddress = inetPeer1;
        this.peerPort = portPeer1;
        this.socket = new DatagramSocket(port);
        this.seq = 0;
    }

    public ReliableConnection(InetAddress inetPeer1, int portPeer1) throws SocketException {
        this.peerAddress = inetPeer1;
        this.peerPort = portPeer1;
        this.seq = 0;
        this.socket = new DatagramSocket();
    }

    public ReliableConnection(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.peerAddress = null;
        this.peerPort = -1;
        this.seq = 0;
    }

    public void send(byte[] data) throws IOException {
        // Se data for maior que MTU é necessário fazer a divisão por vários pacotes
        // Como agora estou a fazer stop and wait preciso de receber um ack para cada pacote enviado
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream  dis = new DataInputStream(new BufferedInputStream(bais));
        byte dataOut[] = new byte[MTU];
        // Recebe-se um Frame e não dados
        SecurityFrame frameIn;
        ConnectionFrame dataFrame;
        // Para saber se se pode saltar o ciclo de tentativas
        boolean received = false;
        // Máximas tentativas de timeout
        final int maxTries = 5;
        // para saber o tamanho lido da stream
        int size = 0;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<SecurityFrame> future;
        Callable<SecurityFrame> callable = new Callable<SecurityFrame>() {
            @Override
            public SecurityFrame call() throws IOException {
                SecurityFrame r = rdtRcvPckt();
                if (Thread.interrupted()) {
                    socket.setSoTimeout(1);
                }
                return r;
            }
        };

        while ((size = dis.read(dataOut)) > 0) {
            while (!received) {
                udtSendPckt(size, dataOut, this.seq);
                future = executor.submit(callable);
                try {

                    frameIn = future.get(25, TimeUnit.MILLISECONDS);
                    dataFrame = ConnectionFrame.deserealize(frameIn.data);
                    if (notCorrupt(frameIn) && isAck(dataFrame, this.seq + 1)) {
                        received = true;
                        this.seq++;
                    }

                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    future.cancel(true);
                    socket.setSoTimeout(0);
                }

            }

            received = false;

            if(size < MTU) {
                executor.shutdownNow();
            }
        }
        bais.close();
        dis.close();
    }

    private boolean sameRecipient(InetAddress address, int port) {
        return address.equals(this.peerAddress) && this.peerPort == port;
    }

    private boolean isAck(ConnectionFrame frame, int seq) {
        return frame.tag == seq && frame.dataLen == 0;
    }

private boolean notCorrupt(SecurityFrame frame) {
    return CreatePassword.checkAuthenticated(this.key, frame.data, frame.hashMac );
}

public byte[] makeOut(int size, byte[]data, int seq) throws IOException{
        ConnectionFrame outFrame = new ConnectionFrame(seq, size, data);
        byte[] frameOut;
        frameOut = outFrame.serialize();
        byte[] securityOut = null;

        byte[] hash = CreatePassword.createDigest(this.key, frameOut);
        SecurityFrame securityFrame = new SecurityFrame(hash, frameOut.length, frameOut);
        securityOut = securityFrame.serialize();


        return securityOut;
    }
    private void udtSendPckt(int size, byte[] data, int seq) throws IOException {
        byte[] frameOut = makeOut(size, data, seq);
        DatagramPacket outPacket = new DatagramPacket(frameOut,
                                                    frameOut.length,
                                                    this.peerAddress,
                                                    this.peerPort);
        socket.send(outPacket);
    }

    private SecurityFrame rdtRcvPckt() throws IOException {
        byte dataIn[] = new byte[SecurityFrame.MTU];
        DatagramPacket inPacket = new DatagramPacket(dataIn, dataIn.length);
        socket.receive(inPacket);

        if(this.peerAddress == null && this.peerPort == -1){
            this.peerAddress = inPacket.getAddress();
            this.peerPort = inPacket.getPort();
        }
        while(!sameRecipient(inPacket.getAddress(), inPacket.getPort()))
            socket.receive(inPacket);

        SecurityFrame inFrame = SecurityFrame.deserialize(inPacket.getData());

        return inFrame;
    }

    public byte[] receive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos  = new DataOutputStream(new BufferedOutputStream(baos));

        boolean flag = true;
        SecurityFrame securityFrame;
        ConnectionFrame inFrame;

        while (flag) {
            securityFrame = rdtRcvPckt();
            inFrame = ConnectionFrame.deserealize(securityFrame.data);

            if (inFrame.dataLen < this.MTU) flag = false;

            if (notCorrupt(securityFrame) && validSeq(inFrame)) {
                dos.write(inFrame.data);
                this.seq++;
            }

            sendAck();
        }

        baos.close();
        dos.close();
        return baos.toByteArray();
    }

    private void sendAck() throws IOException {
        byte[] frameOut = makeOut(0, null, this.seq);
        //ConnectionFrame ackFrame = new ConnectionFrame(this.seq, 0, null);
        //byte[] dataOut = ackFrame.serialize();
        DatagramPacket outPacket = new DatagramPacket(frameOut,
                                                    frameOut.length,
                                                    this.peerAddress,
                                                    this.peerPort);
        socket.send(outPacket);
    }

    private boolean validSeq(ConnectionFrame inFrame) {
        return inFrame.tag == this.seq;
    }

    public void close(){
        this.socket.close();
    }
}
