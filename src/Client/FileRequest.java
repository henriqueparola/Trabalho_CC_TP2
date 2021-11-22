package Client;

public class FileRequest implements Runnable{
    private String ipToSync;
    private MetaData fileMetaData;

    public FileRequest(String ipToSync, MetaData fileMetaData) {
        this.ipToSync = ipToSync;
        this.fileMetaData = fileMetaData;
    }

    @Override
    public void run() {
        // TODO início de conexão para porta 5000 do ipToSync
        // TODO após o resultado do início da conexão, enviar Ack's para nova porta
    }
}
