package Client;

import java.time.LocalDate;

public class MetaData {
    String filePath;
    int size;
    LocalDate creationDate;
    LocalDate modified;

    public MetaData(String filePath, int size, LocalDate creationDate, LocalDate modified) {
        this.filePath = filePath;
        this.size = size;
        this.creationDate = creationDate;
        this.modified = modified;
    }
}
