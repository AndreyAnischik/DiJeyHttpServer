package server;

import javax.script.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class HttpConnection implements Runnable {
    private HttpServer httpServer;
    private Socket socket;

    private BufferedReader clientData = null;
    private PrintWriter serverData = null;
    private BufferedOutputStream dataOut = null;
    private String fileName = null;

    public HttpConnection(HttpServer server, Socket socket) {
        this.httpServer = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        while(true) {
            handleResponse();
        }
    }

    public void handleResponse() {
        try {
            clientData = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverData = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());

            String input = clientData.readLine();
            StringTokenizer parsedData = new StringTokenizer(input);
            String method = parsedData.nextToken().toUpperCase();

            switch (method) {
                case Constants.GET:
                    get(parsedData);
                    break;
                case Constants.POST:
                    post();
                    break;
                case Constants.HEAD:
                    break;
                default:
                    sendNotImplemented();
                    break;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void get(StringTokenizer parsedData) throws IOException {
        fileName = parsedData.nextToken().toLowerCase();
        setDataToResponse(Constants.OK, fileName);
    }

    private void sendNotImplemented() throws IOException {
        String notImplemented = Constants.NOT_IMPLEMENTED_PAGE;
        setDataToResponse(Constants.NOT_IMPLEMENTED, notImplemented);
    }

    private void post() throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(clientData);
        StringBuffer bodySb = new StringBuffer();
        char[] bodyChars = new char[1024];
        int length;

        while (lineNumberReader.ready() && (length = lineNumberReader.read(bodyChars)) > 0) {
            bodySb.append(bodyChars, 0, length);
        }

        String paramsArray = bodySb.toString().split("\r\n\r\n")[1];

        HashMap<String, String> paramsHash = new HashMap<>();
        for (String singleParamSet : paramsArray.split("&")) {
            String[] params = singleParamSet.split("=");
            paramsHash.put(params[0], params[1]);
        }

        try {
            Path currentRelativePath = Paths.get(Constants.SCRIPTS_DIRECTORY + "change_team.rb");

            ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
            jruby.eval(Files.newBufferedReader(currentRelativePath, StandardCharsets.UTF_8));

            Invocable invokableJrubyIns = (Invocable) jruby;
            String scriptResult = (String) invokableJrubyIns.invokeFunction("change", paramsHash.get("team"));
            setDataToResponse(Constants.OK, scriptResult);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDataToResponse(String code, String content) throws IOException {
        byte[] byteData;
        int contentLength;

        String contentType = getContentType(content);

        if(contentType.equals("text/plain")){
            byteData = content.getBytes();
            contentLength = content.length();
        } else {
            File sendingFile = new File(Constants.CONTENT_DIRECTORY, content);
            contentLength = (int) sendingFile.length();

            byteData = readFileData(sendingFile, contentLength);
        }

        composeResponse(code, contentType, contentLength);

        dataOut.write(byteData, 0, contentLength);
        dataOut.flush();
    }

    private void composeResponse(String code, String contentType, int contentLength) {
        serverData.println("HTTP/1.1 " + code);
        serverData.println("Server: Java http server by DiJey");
        serverData.println("Date: " + new Date());
        serverData.println("Content-type: " + contentType);
        serverData.println("Content-length: " + contentLength);
        serverData.println();
        serverData.flush();
    }

    public void stop() {
        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }

        try {
            clientData.close();
            serverData.close();
            dataOut.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }

        return fileData;
    }

    private String getContentType(String file) {
        if (file.endsWith(".htm") || file.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    public String getFileName() {
        return this.fileName;
    }
}
