package Client;

import Logger.ProtocolLogger;

import java.util.List;
import java.util.Map;

public class Client implements Runnable{
    private String[] ipsToSync;
    private String folderToSync;

    public Client(String[] ipsToSync, String folderToSync){
        this.ipsToSync = ipsToSync;
        this.folderToSync = folderToSync;
    }

    @Override
    public void run() {
        FolderStruct folderStruct = new FolderStruct();
        Thread[] srthreads = new Thread[ipsToSync.length];

        /*Pedido 1 - Estrutura de pasta*/
        int i = 0;
        for (String ip : ipsToSync){
            srthreads[i] = new Thread(new StructRequest(ip,folderStruct));
            srthreads[i].start();
            i++;
        }

        /* Espera pela estrutura completa */
        try {
            for (Thread srthread : srthreads)
                srthread.join();
        }catch (InterruptedException e){
            // TODO
        }


        /* Criação da estrutura de pastas */
        folderStruct.createFolderStruct(folderToSync);

        /*Pedido 2 - Conteúdos*/
        for (Map.Entry<String, List<MetaData>> entry : folderStruct.getStruct().entrySet()) {
            for (MetaData metadata : entry.getValue()){
                Thread t = new Thread(new FileRequest(entry.getKey(),metadata,folderToSync));
                t.start();
            }
        }
    }
}
