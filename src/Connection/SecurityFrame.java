package Connection;

import java.io.*;

public class SecurityFrame {
    public final String hashMac;
    public final byte[] data;

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
}
