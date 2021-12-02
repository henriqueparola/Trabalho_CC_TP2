package Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ConnectionFrame {
    public final int tag;
    public final byte[] data;
    public static final int MTU = 1404;


    public ConnectionFrame(int tag, byte[] data) {
        this.tag = tag;
        this.data = data;
    }

    public byte[] serialize() throws IOException {

        ByteBuffer bb = ByteBuffer.allocate(data.length + 4);
        bb.putInt(this.tag);
        bb.put(data);

        return bb.array();
    }

    public static ConnectionFrame deserealize(byte[] byteFrame) {
        ByteBuffer bb = ByteBuffer.wrap(byteFrame);
        int tag = bb.getInt();
        // Alloca-se a mais
        byte[] data = new byte[MTU];
        bb.get(data);
        return new ConnectionFrame(tag, data);
    }

}
