package Server.Http;

import Client.FolderStruct;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer implements Runnable{
    FolderStruct fd;

    public HttpServer(FolderStruct fd) {
        this.fd = fd;
    }

    @Override
    public void run() {
        boolean running = true;
        try{
            ServerSocket ssocket = new ServerSocket(8080);
            while(running){
                ClientHandler ch = new ClientHandler(ssocket.accept(),fd);
                Thread t = new Thread(ch);
                t.start();
            }
            ssocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
