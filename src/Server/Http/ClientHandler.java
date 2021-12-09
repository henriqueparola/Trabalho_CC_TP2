package Server.Http;

import Client.FolderStruct;
import Logger.ProtocolLogger2;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class ClientHandler implements Runnable{
    Socket socket;
    FolderStruct fd = FolderStruct.getInstance();

    public ClientHandler(Socket accept) {
        socket = accept;
    }

    @Override
    public void run() {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String htmlRouteRoot = "<html><body><h1>Route /</h1></body></html>";

            String htmlNotFound = "<html><body><h1>ERROR 404</h1><h2>Not Found</h2></body></html>";
            String htmlBadRequest = "<html><body><h1>ERROR 400 </h1><h2>Bad Request</h2></body></html>";

            String httpResponse = "";
            String httpResponseBody = "";

            String fromClient = in.readLine();
            String[] firstLine = fromClient.split(" ");
            boolean notAnswer = false;

            if (firstLine[0].compareTo(HttpSheet.GET_METHOD) == 0 &&
                firstLine[2].compareTo(HttpSheet.HTTP_VERSION) == 0){
                switch (firstLine[1]){
                    case HttpSheet.Route_Root:
                        httpResponse = HttpSheet.OK;
                        httpResponseBody = htmlRouteRoot;
                        break;
                    case HttpSheet.Route_Log:
                        httpResponse = HttpSheet.OK;
                        httpResponseBody = logReply();
                        break;
                    case HttpSheet.Route_State:
                        httpResponse = HttpSheet.OK;
                        httpResponseBody = stateReply();
                        break;
                    default:
                        httpResponse = HttpSheet.NOT_FOUND;
                        httpResponseBody = htmlNotFound;
                        break;
                }
            }else{
                httpResponse = HttpSheet.BAD_REQUEST;
                httpResponseBody = htmlBadRequest;
            }

            httpResponse += "\nServer: Http FTRapid Server"
                    + "\nDate: " + new Date()
                    + "\nContent-type: text/html"
                    + "\nContent-length: " + httpResponseBody.length()
                    + "\nConnection: Closed"
                    + "\n\n"
                    + httpResponseBody;

            this.socket.getOutputStream().write(httpResponse.getBytes(StandardCharsets.UTF_8));
            in.close();
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String logReply(){
        ProtocolLogger2 pl = ProtocolLogger2.getInstance();
        return pl.logToHtmlString();
    }

    public String stateReply(){
        String responseBody = "<style>\n" +
                "table, th, td {\n" +
                "  border:1px solid black;\n" +
                "}\n" +
                "</style><h2>Estado de recebimento de ficheiros</h2><br><table style=\"width:100%\"><tr><th>Nome</th><th>IP remetente</th><th>Sincronizado?</th></tr>";

        for (Map.Entry<String, List<Boolean>> fileState: fd.getStructState().entrySet()){
            int i = 0;
            for(Boolean bool: fileState.getValue()){
                responseBody += "<tr>";
                responseBody += ("<td style=\"color:blue\">" +  fd.getFileName(fileState.getKey(),i) + "</td>");
                responseBody += ("<td>" + fileState.getKey() + "</td>");
                if(bool == true) {
                    responseBody += ("<td>Sim</td>");
                }else{
                    responseBody += ("<td>Não</td>");
                }
                responseBody += "</tr>";
                i++;
            }
        }

        return responseBody;
    }
}
