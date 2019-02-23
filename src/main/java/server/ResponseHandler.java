package server;

import constants.Blanks;
import constants.Codes;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class ResponseHandler {
    private Dotenv dotenv = Dotenv.configure().directory("./").load();

    private int port;
    private BufferedReader clientData;
    private PrintWriter serverData;
    private BufferedOutputStream dataOut;
    private static Logger logger = Logger.getLogger(ResponseHandler.class);

    public void setDataToResponse(int code, String content) throws IOException {
        byte[] byteData;
        int contentLength;

        String contentType = getContentType(content);

        if (contentType.equals("text/plain")) {
            byteData = content.getBytes();
            contentLength = content.length();
        } else {
            File sendingFile = new File(Blanks.CONTENT_DIRECTORY, content);
            contentLength = (int) sendingFile.length();

            if (!sendingFile.exists()) {
                throw new FileNotFoundException();
            }

            byteData = readFileData(sendingFile, contentLength);
        }

        composeResponse(content, code, contentType, contentLength);

        logger.info(new String(byteData));
        dataOut.write(byteData, 0, contentLength);
        dataOut.flush();
    }

    public void composeResponse(String content, int code, String contentType, int contentLength) {
        List<String> headers = new ArrayList<>();
        headers.add("HTTP/1.1 " + code);
        headers.add("Server: Java http server by DiJey");
        headers.add("Date: " + new Date());
        headers.add("Content-type: " + contentType);
        headers.add("Content-length: " + contentLength);
        if (code == Codes.MOVED) {
            headers.add("Location: " +
                    dotenv.get("BASE_URL") + ":" +
                    port +
                    content
            );
        }
        for (String header : headers) {
            logger.info(header);
            serverData.println(header);
        }
        serverData.println();
        serverData.flush();
    }

    private byte[] readFileData(File file, int fileLength) {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } catch (IOException e) {
            logger.info("Error reading file");
        } finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (IOException e) {
                    logger.error("Error closing file");
                }
            }
        }

        return fileData;
    }

    public void writeMap(Map<String, String> mapValues) {
        for (Map.Entry<String, String> pair : mapValues.entrySet()) {
            logger.info(pair.getKey() + ": " + pair.getValue());
        }
    }

    public StringBuffer parse() throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(clientData);
        StringBuffer bodySb = new StringBuffer();
        char[] bodyChars = new char[1024];
        int length;

        while (lineNumberReader.ready() && (length = lineNumberReader.read(bodyChars)) > 0) {
            bodySb.append(bodyChars, 0, length);
        }

        return bodySb;
    }

    public HashMap<String, String> parseHeaders(StringBuffer bodySb) {
        String headersArray = bodySb.toString().split("\r\n\r\n")[0];

        HashMap<String, String> headersHash = new HashMap<>();
        for (String header : headersArray.split("\r\n")) {
            String[] headers = header.split(": ");
            headersHash.put(headers[0], headers[1]);
        }

        return headersHash;
    }

    public HashMap<String, String> parseParams(StringBuffer bodySb) {
        String paramsArray = bodySb.toString().split("\r\n\r\n")[1];

        HashMap<String, String> paramsHash = new HashMap<>();
        for (String singleParamSet : paramsArray.split("&")) {
            String[] params = singleParamSet.split("=");
            paramsHash.put(params[0], params[1]);
        }

        return paramsHash;
    }

    public String getContentType(String file) {
        if (file.endsWith(".htm") || file.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    public void setClientData(BufferedReader clientData) {
        this.clientData = clientData;
    }

    public void setServerData(PrintWriter serverData) {
        this.serverData = serverData;
    }

    public void setDataOut(BufferedOutputStream dataOut) {
        this.dataOut = dataOut;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
