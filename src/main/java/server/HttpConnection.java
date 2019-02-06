package server;

import java.io.*;
import java.net.Socket;
import java.util.Date;
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
    }

    private void get(StringTokenizer parsedData) throws IOException {
        fileName = parsedData.nextToken().toLowerCase();
        setDataToResponse(Constants.OK, fileName);
    }

    private void sendNotImplemented() throws IOException {
        String notImplemented = Constants.NOT_IMPLEMENTED_PAGE;
        setDataToResponse(Constants.NOT_IMPLEMENTED, notImplemented);
    }

    private void setDataToResponse(String code, String file) throws IOException {
        File sendingFile = new File(Constants.CONTENT_DIRECTORY, file);
        int fileLength = (int) sendingFile.length();
        String content = getContentType(file);

        byte[] fileData = readFileData(sendingFile, fileLength);
        composeResponse(code, content, fileLength);

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    private void composeResponse(String code, String content, int fileLength) {
        serverData.println("HTTP/1.1 " + code);
        serverData.println("Server: Java http server by DiJey");
        serverData.println("Date: " + new Date());
        serverData.println("Content-type: " + content);
        serverData.println("Content-length: " + fileLength);
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
