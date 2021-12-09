package Server.Http;

import Client.FolderStruct;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer implements Runnable{
    FolderStruct fd = FolderStruct.getInstance();

    @Override
    public void run() {
        boolean running = true;
        try{
            ServerSocket ssocket = new ServerSocket(8080);
            while(running){
                ClientHandler ch = new ClientHandler(ssocket.accept());
                Thread t = new Thread(ch);
                t.start();
            }
            ssocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
