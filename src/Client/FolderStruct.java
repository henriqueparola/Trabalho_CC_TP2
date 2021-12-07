package Client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderStruct {
    private Map<String, List<MetaData>> folder = new HashMap<>();

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

    public void addList(String ip, List<MetaData> foldersFromIp){
        folder.put(ip,foldersFromIp);
    }

    public Map<String,List<MetaData>> getStruct(){
        return folder;
    }
}
