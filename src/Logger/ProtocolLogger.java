package Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ProtocolLogger {
    public Logger logger;
    FileHandler fh;
    Lock lock = new ReentrantLock();

    public ProtocolLogger(String file_name) throws SecurityException, IOException{
        File f = new File(file_name);
        if (!f.exists()){
            f.createNewFile();
        }

        fh = new FileHandler(file_name,true);
        logger = Logger.getLogger("test");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }

    public void loggerInfo(String message){
        // não preciso de lock (é safe)
        lock.lock();
        logger.info(message);
        lock.unlock();
    }
}