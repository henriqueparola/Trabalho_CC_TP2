package Client;

import Connection.ReliableConnection;
import Logger.ProtocolLogger;
import Logger.ProtocolLogger2;
import Multiplex.ProtocolFrame;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/** Represents an Client.
 * @author Henrique Costa
 * @author José Pedro
 * @author Marco Esperança
 * @version 1.0
 * @since 1.0
 */
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
        folderStruct.setStates();


        /* Criação da estrutura de pastas */
        folderStruct.createFolderStruct(folderToSync);
        folderStruct.initTime = System.currentTimeMillis();

        /*Pedido 2 - Conteúdos*/
        Integer numOfFilesToRequest = folderStruct.getStruct().values().stream().mapToInt(List::size).sum();
        Thread[] frthreads = new Thread[numOfFilesToRequest];
        ProtocolLogger2 pl = ProtocolLogger2.getInstance();
        i = 0;
        int aux = i;
        for (Map.Entry<String, List<MetaData>> entry : folderStruct.getStruct().entrySet()) {
            for (MetaData metadata : entry.getValue()){
                frthreads[i] = new Thread(new FileRequest(entry.getKey(),metadata,folderToSync));
                frthreads[i].start();
                i++;
            }
        }

        // special case when I do not have files to request to a ip
        for (String ip: ipsToSync){
            if (!folderStruct.getStruct().containsKey(ip)){
                try {
                    ReliableConnection rb3 = new ReliableConnection(InetAddress.getByName(ip), 5000);
                    ProtocolFrame pf3 = new ProtocolFrame((byte) 0x3, 0, null);
                    pl.loggerInfo("Enviando FIN ao " + ip);
                    rb3.send(pf3.serialize());
                    rb3.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    System.out.println("Send deu Timeout. Undefined Behaviour.");
                }

            }
        }

        /* Espera pelo recebimento dos ficheiros todos */
        try {
            for (Thread frthread : frthreads) {
                frthread.join();
            }
        }catch (InterruptedException e){
            // TODO
        }
    }
}
