package Connection;

import java.io.*;
import java.util.Arrays;

public class SecurityFrame {
    public final byte[] hashMac;
    public final byte[] data;
    public final int size;
    public static final int MTU = 1332;

    public SecurityFrame(byte[] hashMac, int size, byte[] data) {
        this.hashMac = hashMac;
        this.size = size;
        this.data = data;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));

        dos.write(hashMac);
        dos.writeInt(size);
        if (size > 0)
            dos.write(data);

        baos.close();
        dos.close();

        return baos.toByteArray();
    }


    public static SecurityFrame deserialize(byte[] byteFrame) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(byteFrame);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(bais));
        byte[] hash = new byte[20];
        dis.read(hash);
        int size = dis.readInt();
        byte[] data = null;
        if (size > 0) {
              data = new byte[size];
              dis.read(data);
        }

        bais.close();
        dis.close();

        return new SecurityFrame(hash, size, data);
    }


}
