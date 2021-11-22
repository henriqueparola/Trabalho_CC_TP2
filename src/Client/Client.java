package Client;

import java.util.List;
import java.util.Map;

public class Client implements Runnable{
    private String[] ipsToSync;

    public Client(String[] ipsToSync){this.ipsToSync = ipsToSync;}

    @Override
    public void run() {
        FolderStruct folderStruct = new FolderStruct();
        Thread[] srthreads = new Thread[ipsToSync.length];

        /*
            Pedido 1 - Estrutura de pasta
        */
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

        /*
            Pedido 2 - Conteúdos
        */
        for (Map.Entry<String, List<MetaData>> entry : folderStruct.getStruct().entrySet()) {
            // TODO Por enquanto cada Thread corresponde a uma conexão
            // Cada Thread será responsável por fazer upload de um ficheiro
            // ESTAMOS A ASSUMIR INICIALMENTE QUE NÂO EXISTEM FICHEIROS REPITIDO (FRUTO DA OTIMIZAÇÂO INTERMÉDIA DE ANÁLISE DA ESTRUTURA)
            for (MetaData metadata : entry.getValue()){
                Thread t = new Thread(new FileRequest(entry.getKey(),metadata));
                t.start();
            }
        }

    }
}
