package Connection;

import Security.Authentication;
import Security.CreatePassword;
import Security.RequestInterceptor;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.getDefaultUncaughtExceptionHandler;
import static java.lang.Thread.sleep;

public class ReliableConnection {
    public DatagramSocket socket;
    private final int MTU = 1418;
    public InetAddress peerAddress;
    public int peerPort;
    private int seq;
    private Window window = new Window(17);
    private int nextSeqNum;
    private int base;
    private String key = Authentication.getInstance().getPasswordInsert();

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
        this.nextSeqNum = 0;
        this.base = 0;
    }

    public ReliableConnection(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.peerAddress = null;
        this.peerPort = -1;
        this.seq = 0;
        this.nextSeqNum = 0;
        this.base = 0;
    }

    public void windowSend(byte[] data) {

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
        final int windows = 3;
        // para saber o tamanho lido da stream
        int size = 0;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future;

        boolean flag = true;
        Callable<Integer> callable = new WindowSend(this.window, this.socket, this);

        int j = 0;
        // Não esquecer de quando acabar o ciclo.
        while(flag) {
            int base = window.base;
            // Encher janela com dados para enviar
            for(int i = window.nextSeqNum; window.nextSeqNum - base < window.N && (size = dis.read(dataOut)) > 0; i++) {
                byte[] packetOut = makeOut(size, dataOut, i);
                window.addData(i, packetOut);
            }

            if (window.empty()) {
               executor.shutdownNow();
               break;
            }
            future = executor.submit(callable);
            for (int i = base; i < window.nextSeqNum; i++) {
                byte[] out = window.windowData.get(i-window.base);
                udtSendPckt(out);
            }



            try {
                Integer acked = future.get(100, TimeUnit.MILLISECONDS);

                if (acked != window.base)
                    window.update(acked);

            } catch (InterruptedException | ExecutionException | TimeoutException e)  {
                future.cancel(true);
                socket.setSoTimeout(0);
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

public boolean notCorrupt(SecurityFrame frame) {
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
    public void udtSendPckt(byte[] data) throws IOException {
        DatagramPacket outPacket = new DatagramPacket(data,
                data.length,
                this.peerAddress,
                this.peerPort);
        socket.send(outPacket);
    }

    public SecurityFrame rdtRcvPckt() throws IOException {
        byte dataIn[] = new byte[SecurityFrame.MTU];
        DatagramPacket inPacket = new DatagramPacket(dataIn, dataIn.length);
        socket.receive(inPacket);

        if(this.peerAddress == null && this.peerPort == -1){
            this.peerAddress = inPacket.getAddress();
            this.peerPort = inPacket.getPort();
        }
        while(!sameRecipient(inPacket.getAddress(), inPacket.getPort())) {
            socket.receive(inPacket);
        }

        SecurityFrame inFrame = SecurityFrame.deserialize(inPacket.getData());

        return inFrame;
    }

    public byte[] receive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos  = new DataOutputStream(new BufferedOutputStream(baos));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future;


        boolean flag = true;
        SecurityFrame securityFrame;
        ConnectionFrame inFrame;

        while (flag) {

            socket.setSoTimeout(0);
            for(int i = 0; i < window.N; i++) {
                try {
                    securityFrame = rdtRcvPckt();
                    inFrame = ConnectionFrame.deserealize(securityFrame.data);
                    if (notCorrupt(securityFrame))
                        window.receive(inFrame.tag, inFrame.data);

                    if (i == 0) socket.setSoTimeout(3);
                    byte[] data;
                    while((data = window.retrieve()) != null) {
                        if (data.length < this.MTU) {
                            flag = false;
                        }
                        dos.write(data);
                    }
                } catch (SocketTimeoutException e) {
                }

            }
            socket.setSoTimeout(0);
            sendAck();
        }

        //Caso se queira suportar sends e receives alternados.
        //Uma vez que o nextSeqNum só é utilizado nos métodos de send
        window.nextSeqNum = window.base;
        baos.close();
        dos.close();
        return baos.toByteArray();
    }

    private void sendAck() throws IOException {
        byte[] frameOut = makeOut(0, null, window.base);
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
