package Client;

import Connection.ReliableConnection;
import Multiplex.ProtocolFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileRequest implements Runnable{
    private String ipToSync;
    private MetaData fileMetaData;
    private String folderToSync;

    public FileRequest(String ipToSync, MetaData fileMetaData,String folderToSync) {
        this.ipToSync = ipToSync;
        this.fileMetaData = fileMetaData;
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        try {
            System.out.println("vou pedir o: " + fileMetaData);
            ReliableConnection rb = new ReliableConnection(InetAddress.getByName(ipToSync), 5000);
            byte[] data = fileMetaData.getFilePath().getBytes(StandardCharsets.UTF_8);
            ProtocolFrame pf2 = new ProtocolFrame((byte) 0x1, data.length, data);
            rb.send(pf2.serialize());

            int port = rb.socket.getLocalPort();
            rb.close();

            ReliableConnection rb2 = new ReliableConnection(port);
            // TODO suportar um recebimento de ficheiro em mais que um receive
            byte[] dataReceive = rb2.receive();
            ProtocolFrame pf = ProtocolFrame.deserialize(dataReceive);

            System.out.println("Vou criar o :" + folderToSync + "/" + fileMetaData.getFilePath());
            File file = new File(folderToSync + "/" + fileMetaData.getFilePath());
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            os.write(pf.data);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
