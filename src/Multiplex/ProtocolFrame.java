package Multiplex;

import java.io.*;

public class ProtocolFrame {
    public byte opcode;
    public int datLength;
    public byte[] data;

    public ProtocolFrame(byte opcode,int datLength,byte[] data){
        this.opcode = opcode;
        this.datLength = datLength;
        this.data = data;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));

        dos.writeByte(opcode);
        dos.writeInt(datLength);

        if (datLength > 0) {
            dos.write(data);
        }

        baos.close();
        dos.close();

        return baos.toByteArray();
    }

    public static ProtocolFrame deserialize(byte[] bytes) throws IOException {
        byte[] data = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(bais));
        byte opcode = dis.readByte();
        int dataLength = dis.readInt();

        if (dataLength > 0) {
            data = new byte[dataLength];
            dis.read(data);
        }
        bais.close();
        dis.close();

        return new ProtocolFrame(opcode,dataLength,data);
    }
}
