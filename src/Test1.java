import Connection.ReliableConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Test1 {
    public static void main(String[] args) {
        try {
            ReliableConnection rc = new ReliableConnection(InetAddress.getByName("10.1.1.2"), 5000);
            byte[] dataIn = rc.receive();
            String r = new String(dataIn);
            System.out.println(r);
        } catch (IOException e) {

        }
    }
}
