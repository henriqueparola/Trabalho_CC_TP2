package Client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;

public class MetaData {
    String filePath;
    long size;
    long creationDate;
    long modifiedDate;

    public MetaData(){}

    public MetaData(String filePath, long size, long creationDate, long modifiedDate) {
        this.filePath = filePath;
        this.size = size;
        this.creationDate = creationDate;
        this.modifiedDate = modifiedDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getSize() {
        return size;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public long getModified() {
        return modifiedDate;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(ba);

        dos.writeUTF(filePath);
        dos.writeLong(size);
        dos.writeLong(creationDate);
        dos.writeLong(modifiedDate);

        dos.close();

        return ba.toByteArray();
    }

    public void deserialize(DataInputStream dos){
        try {
            filePath = dos.readUTF();
            size = dos.readLong();
            creationDate = dos.readLong();
            modifiedDate = dos.readLong();
        }catch (IOException e){

        }
    }

    public boolean equals(MetaData metaData){
        if ((this.size == metaData.size) &&
                (this.modifiedDate == metaData.modifiedDate) &&
                (this.creationDate == metaData.creationDate) &&
                (this.filePath.equals(metaData.getFilePath()))) return true;
        return false;
    }
}
