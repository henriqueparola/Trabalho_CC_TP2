package Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.System.out;

public class ProtocolLogger2 {
    private boolean activate;

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

    public void setActive(){
        activate = true;
    }

    public void loggerInfo(String message){
        if (activate == false) return;
        lock.lock();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String str = "[INFO - " + dtf.format(now) + " ] " + message + ".\n";
        String strSout = "\u001B[33m" +  "[INFO - " + dtf.format(now) + " ] " + "\u001B[0m" + message + ".\n";
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("log.txt",true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        System.out.println(strSout);
    }

    public static ProtocolLogger2 getInstance() {
        if (single_instance == null)
            single_instance = new ProtocolLogger2();

        return single_instance;
    }

    public String logToHtmlString(){
        lock.lock();
        String logHtml = "<style>\n" +
                "table, th, td {\n" +
                "  border:1px solid black;\n" +
                "}\n" +
                "</style><h2>Registo de Logs</h2><br><table style=\"width:100%\"><tr><th>Tipo</th><th>Horario</th><th>Mensagem</th></tr>";

        try {
            BufferedReader rd = new BufferedReader(new FileReader("log.txt"));
            String logLine;
            while ((logLine = rd.readLine()) != null) {
                String[] strs = logLine.split("[\\-|\\[|\\]]");
                logHtml += "<tr>";
                logHtml += ("<td style=\"color:blue\">" + strs[1] + "</td>");
                logHtml += ("<td>" + strs[2] + "</td>");
                logHtml += ("<td>" + strs[3] + "</td>");
                logHtml += "</tr>";
            }

            logHtml += "</table>";
            rd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return logHtml;
    }
}
