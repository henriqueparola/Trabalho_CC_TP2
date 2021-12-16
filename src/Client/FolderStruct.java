package Client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderStruct {
    private static FolderStruct single_instance = null;
    private List<MetaData> myFolder = new ArrayList<>();
    private String folderToSync;
    private Map<String, List<MetaData>> folder = new HashMap<>();
    private Map<String, List<Boolean>> folderState = new HashMap<>();
    private Lock l = new ReentrantLock();

    public List<MetaData> getMyFolder(){
        return myFolder;
    }

    public void addMyList(String folderToSync) {
        l.lock();
        this.folderToSync = folderToSync;
        Path path = Paths.get("./" + folderToSync);
        Stream<Path> walk = null;
        try {
            walk = Files.walk(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Path> paths;
        paths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
        List<MetaData> tmp = paths.stream().map(p -> getMetaData(p)).toList();
        for (MetaData mt : tmp)
            this.myFolder.add(mt);
        l.unlock();
    }

    public void createFolderStruct(String folderToSync) {

        for (Map.Entry<String, List<MetaData>> entry : folder.entrySet()) {
            for (MetaData metadata : entry.getValue()){
                int lastSeparator = metadata.getFilePath().lastIndexOf("/");
                if (lastSeparator != -1){
                    String directoryName = metadata.getFilePath().substring(0,lastSeparator);
                    File folderPath = new File(folderToSync + "/" + directoryName);
                    folderPath.mkdirs();
                }
            }
        }
    }

    public void setStates() {
        l.lock();
        for (Map.Entry<String, List<MetaData>> entry : this.folder.entrySet()) {
            List<Boolean> lb = new ArrayList<>();
            for (int i = 0; i < entry.getValue().size(); i++)
                lb.add(false);
            this.folderState.put(entry.getKey(), lb);
        }
        l.unlock();
    }

    public void addList(String ip, List<MetaData> foldersFromIp){
        l.lock();
        List<MetaData> toAdd = new ArrayList<>();
        int i = 0, j = 0;
        /*
        Objetivo algoritmo, a partir das entradas do folder, atualizar mapa
        Por cada entrada do folder verificar se na lista de metadados da entrada existe
        algum tipo metadado do folderFromIp inferior a um metadato do da entrada. Se for
        é necessário atualizar o folder (removendo o metadato da entrada) e atualizar o
        folderState(removendo da Lista da chave ipIt o indice j, que corresponde ao
        metadado removido)


         */

        updateMetaLists(this.myFolder, foldersFromIp);

        for(List<MetaData> ltm : this.folder.values())
            updateMetaLists(ltm, foldersFromIp);

        if(foldersFromIp.size() > 0)
            folder.put(ip, foldersFromIp);


        // debug
        //System.out.println("ADDED " + foldersFromIp.size() + "FILES");
        for(MetaData l: foldersFromIp)
            System.out.println("Precido do: " + l.getFilePath());
        l.unlock();
    }

    private void updateMetaLists(List<MetaData> current, List<MetaData> toAdd) {
        l.lock();
        MetaDataComparator mtc = new MetaDataComparator();
        List<MetaData> toAddAux = toAdd.stream().map(MetaData::clone).toList();
        List<MetaData> currentAux = current.stream().map(MetaData::clone).toList();

        for (MetaData mt : toAddAux) {
            Iterator<MetaData> it = currentAux.iterator();
            boolean found = false;
            while(it.hasNext() && !found) {
                MetaData mtCompare = it.next();
                int cmp;
                if ((cmp = mtc.compare(mt, mtCompare)) > 0) {
                    found = true;
                    current.remove(mtCompare);
                }
                else if (cmp == 0) {
                    found = true;
                    toAdd.remove(mt);
                    // debug
                    // System.out.println("Res from remove " + toAdd.remove(mt));
                }
            }
        }
        l.unlock();
    }

    public Map<String,List<MetaData>> getStruct(){
        return folder;
    }

    public Map<String,List<Boolean>> getStructState(){
        return folderState;
    }

    public String getFileName(String key, int index) {
        return folder.get(key).get(index).getFilePath();
    }

    public void changeState(String key, String fileName){
        List<MetaData> list = folder.get(key);
        int index = 0;
        for(int i = 0; i < list.size(); i++){
            if (list.get(i).getFilePath().equals(fileName)){
                index = i;
                break;
            }
        }
        List<Boolean> listState = folderState.get(key);
        listState.set(index,true);
        folderState.put(key,listState);
    }

    public static FolderStruct getInstance() {
        if (single_instance == null)
            single_instance = new FolderStruct();

        return single_instance;
    }

    private MetaData getMetaData(Path path){
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            return new MetaData(
                    path.toString().substring(this.folderToSync.length() + 3),
                    attr.size(),
                    attr.creationTime().toMillis(),
                    attr.lastModifiedTime().toMillis()
            );
        }catch (IOException e){
        }
        return null;
    }
}
