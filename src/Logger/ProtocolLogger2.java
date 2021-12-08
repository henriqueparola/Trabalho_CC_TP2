package Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ProtocolLogger2 {
    private static ProtocolLogger2 single_instance = null;
    Lock lock = new ReentrantLock();
    static File file = new File("log.txt");
    static{
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void loggerInfo(String message){
        lock.lock();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String str = "[INFO - " + dtf.format(now) + " ] " + message + "\n";
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("log.txt",true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(str);
        lock.unlock();
    }

    public static ProtocolLogger2 getInstance()
    {
        if (single_instance == null)
            single_instance = new ProtocolLogger2();

        return single_instance;
    }
}
