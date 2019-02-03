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
        } finally {
            try {
                clientData.close();
                serverData.close();
                dataOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void get(StringTokenizer parsedData) throws IOException {
        fileName = parsedData.nextToken().toLowerCase();

        File sendingFile = new File(Constants.CONTENT_DIRECTORY, fileName);
        int fileLength = (int) sendingFile.length();
        String content = getContentType(fileName);

        byte[] fileData = readFileData(sendingFile, fileLength);
        composeResponse(Constants.OK, content, fileLength);

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    private void sendNotImplemented() throws IOException {
        String notImplemented = Constants.NOT_IMPLEMENTED_PAGE;

        File sendingFile = new File(Constants.CONTENT_DIRECTORY, notImplemented);
        int fileLength = (int) sendingFile.length();
        String content = getContentType(notImplemented);

        byte[] fileData = readFileData(sendingFile, fileLength);
        composeResponse(Constants.NOT_IMPLEMENTED, content, fileLength);

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
}
