package Connection;

import java.io.*;

public class SecurityFrame {
    public final String hashMac;
    public final byte[] data;
    public static final int MTU = 1408;

    public SecurityFrame(String hashMac, byte[] data) {
        this.hashMac = hashMac;
        this.data = data;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));

        dos.writeUTF(hashMac);
        dos.write(data);

        dos.close();
        baos.close();

        return baos.toByteArray();
    }


    public static SecurityFrame deserialize(byte[] byteFrame) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(byteFrame);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(bais));
        String hash = dis.readUTF();
        byte[] data;

        data = new byte[MTU];
        dis.read(data);

        bais.close();
        dis.close();

        return new SecurityFrame(hash, data);
    }


}
