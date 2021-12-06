package Client;

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

    public void addList(String ip, List<MetaData> foldersFromIp){
        folder.put(ip,foldersFromIp);
    }

    public Map<String,List<MetaData>> getStruct(){
        // F encapsulamento
        return folder;
    }

    public void sync(String folderToSync){
        Path path = Paths.get("./" + folderToSync);

        Stream<Path> walk = null;
        try {
            walk = Files.walk(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Path> paths;
        paths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
        List<MetaData> myMetaDataPaths = paths.stream().map(p -> getMetaData(p)).toList();

        /* Objetivo: verificar se algum dos Paths enviados corresponde a um que já possuo */
        /* TODO Ver se a forma melhor de fazer isto */
        for(List<MetaData> metasFromAnoter: folder.values()){
            for(MetaData metaDataFromAnother: metasFromAnoter){
                for(MetaData myMetaData: myMetaDataPaths){
                    if (myMetaData.equals(metaDataFromAnother)){
                        folder.remove(metaDataFromAnother);
                    }
                }
            }
        }
    }

    /* Repetição */
    private MetaData getMetaData(Path path){
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            return new MetaData(path.toString(),attr.size(),attr.creationTime().toMillis(),attr.lastModifiedTime().toMillis());
        }catch (IOException e){
        }
        return null;
    }
}
