package Connection;

import java.io.*;
import java.nio.ByteBuffer;

public class ConnectionFrame {
    public final int tag;
    public final int dataLen;
    public final byte[] data;
    public static final int MTU = 1404;


    public ConnectionFrame(int tag, int dataLen, byte[] data) {
        this.tag = tag;
        this.dataLen = dataLen;
        this.data = data;
    }

    public byte[] serialize() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
        dos.writeInt(this.tag);
        dos.writeInt(this.dataLen);
        dos.write(this.data);

        return baos.toByteArray();
    }

    public static ConnectionFrame deserealize(byte[] byteFrame) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(byteFrame);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(bais));

        int tag = dis.readInt();
        int dataLen = dis.readInt();
        byte[] data = null;

        if (dataLen > 0) {
            data =  new byte[dataLen];
            dis.read(data);
        }

        return new ConnectionFrame(tag, dataLen, data);
    }

}
