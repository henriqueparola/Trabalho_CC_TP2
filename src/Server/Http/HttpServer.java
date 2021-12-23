package Server.Http;

import Client.FolderStruct;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer implements Runnable{
    FolderStruct fd = FolderStruct.getInstance();

    class ShutDownHttp implements Runnable{
        ServerSocket ss;
        public ShutDownHttp(ServerSocket ss){
            this.ss = ss;
        }

        @Override
        public void run() {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        boolean running = true;
        try{
            ServerSocket ssocket = new ServerSocket(8080);
            Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownHttp(ssocket)));
            while(running){
                ClientHandler ch = new ClientHandler(ssocket.accept());
                Thread t = new Thread(ch);
                t.start();
            }
            ssocket.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
