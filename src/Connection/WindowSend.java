package Connection;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class WindowSend implements Callable<Integer> {
    private ReliableConnection con;
    private Window window;
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    public WindowSend(Window win, DatagramSocket socket, ReliableConnection con) {
        this.window = win;
        this.socket = socket;
        this.con = con;
    }
    public Integer call() throws IOException, TimeoutException {

        SecurityFrame frameIn = con.rdtRcvPckt();
        if (Thread.interrupted())
            con.socket.setSoTimeout(1);
        ConnectionFrame dataFrame = ConnectionFrame.deserealize(frameIn.data);

        if(con.notCorrupt(frameIn) && isAck(dataFrame)) {
            return dataFrame.tag;
        }
        else return window.base;
    }




    private boolean isAck(ConnectionFrame dataFrame) {
        int seq = dataFrame.tag;
        return seq > window.base &&  seq <= window.base + window.N && dataFrame.dataLen == 0;
    }

}
