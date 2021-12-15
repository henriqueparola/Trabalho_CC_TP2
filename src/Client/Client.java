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
        FolderStruct folderStruct = FolderStruct.getInstance();
        Thread[] srthreads = new Thread[ipsToSync.length];

        /*Pedido 1 - Estrutura de pasta*/
        int i = 0;
        for (String ip : ipsToSync){
            srthreads[i] = new Thread(new StructRequest(ip));
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
        Integer numOfFilesToRequest = folderStruct.getStruct().values().stream().mapToInt(List::size).sum();
        Thread[] frthreads = new Thread[numOfFilesToRequest];
        i = 0;
        for (Map.Entry<String, List<MetaData>> entry : folderStruct.getStruct().entrySet()) {
            for (MetaData metadata : entry.getValue()){
                System.out.println("I: " + i);
                frthreads[i] = new Thread(new FileRequest(entry.getKey(),metadata,folderToSync));
                frthreads[i].start();
                i++;
            }
        }

        /* Espera pelo recebimento dos ficheiros todos */
        try {
            for (Thread frthread : frthreads) {
                frthread.join();
            }
            System.out.println( "> sync "+"\u001B[32m"+ "complete!" + "\u001B[0m");
        }catch (InterruptedException e){
            // TODO
        }
    }
}
