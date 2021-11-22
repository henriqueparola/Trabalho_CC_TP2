package Client;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class FolderStruct {
    private Map<String, List<MetaData>> folder = new HashMap<>();

    public void addList(String ip, List<MetaData> foldersFromIp){
        folder.put(ip,foldersFromIp);
    }

    public Map<String,List<MetaData>> getStruct(){
        // F encapsulamento
        return folder;
    }
}
