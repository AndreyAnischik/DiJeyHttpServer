package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Blanks;
import constants.Codes;
import constants.Methods;
import org.apache.log4j.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static constants.Blanks.PROTECTED_ROUTES;

public class HttpConnection implements Runnable {
    private HttpServer httpServer;
    private Socket socket;

    private BufferedReader clientData = null;
    private PrintWriter serverData = null;
    private BufferedOutputStream dataOut = null;
    private String requestedRoute = null;
    private Logger logger = Logger.getLogger(HttpConnection.class);

    public HttpConnection(HttpServer server, Socket socket) {
        this.httpServer = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            handleResponse();
        }
    }

    public void handleResponse() {
        try {
            if (!socket.isClosed()) {
                clientData = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                serverData = new PrintWriter(socket.getOutputStream());
                dataOut = new BufferedOutputStream(socket.getOutputStream());

                if (clientData.ready()) {
                    String input = clientData.readLine();
                    StringTokenizer parsedData = new StringTokenizer(input);
                    String method = parsedData.nextToken().toUpperCase();

                    requestedRoute = parsedData.nextToken();

                    if (!checkHttpVersion(parsedData.nextToken().toUpperCase())) {
                        sendNotSupportedHttpVersion();
                        return;
                    }

                    logger.info(input);

                    StringBuffer requestBody = parse();
                    HashMap<String, String> headers = parseHeaders(requestBody);
                    writeMap(headers);

                    if (headers.get("Authorization") == null &&
                            !Arrays.stream(PROTECTED_ROUTES).anyMatch(requestedRoute::equals)
                    ) {
                        setDataToResponse(Codes.UNAUTHORIZED, "You are now allowed. Log in, please.");
                        return;
                    }

                    switch (method) {
                        case Methods.GET:
                            get(requestedRoute);
                            break;
                        case Methods.POST:
                            post(requestedRoute, requestBody);
                            break;
                        case Methods.HEAD:
                            head(requestedRoute);
                            break;
                        default:
                            sendNotImplemented();
                            break;
                    }
                }
            }
        } catch (FileNotFoundException fileException) {
            try {
                fileNotFound();
            } catch (IOException ioException) {
                logger.error("I/O error occurs while sending fileNotFound");
            }
        } catch (IOException exception) {
            logger.error("I/O error occurs while waiting for request");
        }
    }

    private void get(String requestedRoute) throws IOException {
        setDataToResponse(Codes.OK, requestedRoute.toLowerCase());
    }

    private void sendNotImplemented() throws IOException {
        String notImplemented = Blanks.NOT_IMPLEMENTED_PAGE;
        setDataToResponse(Codes.NOT_IMPLEMENTED, notImplemented);
    }

    private void sendNotSupportedHttpVersion() throws IOException {
        String notSupportedHttpVersion = Blanks.HTTP_VERSION_NOT_SUPPORTED;
        setDataToResponse(Codes.HTTP_VERSION_NOT_SUPPORTED, notSupportedHttpVersion);
    }

    private void post(String requestedRoute, StringBuffer stringBuffer) throws IOException {
        HashMap<String, String> paramsHash = parseParams(stringBuffer);
        writeMap(paramsHash);

        String[] requestDestination = requestedRoute.substring(1).split("/");

        try {
            Path currentRelativePath = Paths.get(Blanks.SCRIPTS_DIRECTORY + requestDestination[0]);
            String methodName = requestDestination[1].replace('-', '_');
            String jsonParams = new ObjectMapper().writeValueAsString(paramsHash);

            ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
            jruby.eval(Files.newBufferedReader(currentRelativePath, StandardCharsets.UTF_8));

            Invocable invokableJrubyIns = (Invocable) jruby;
            AtomicReference<String> scriptResult = new AtomicReference<>("");

            try {
                TimeoutBlock timeoutBlock = new TimeoutBlock(5000);
                Runnable block = () -> {
                    try {
                        scriptResult.set((String) invokableJrubyIns.invokeFunction(methodName, jsonParams));
                    } catch (NoSuchMethodException | ScriptException e) {
                        e.printStackTrace();
                    }
                };
                timeoutBlock.addBlock(block);
                setDataToResponse(Codes.OK, scriptResult.get());
            } catch (Throwable e) {
                setDataToResponse(Codes.SERVICE_UNAVAILABLE, "Service unavailable.");
            }
        } catch (Exception e) {
            setDataToResponse(Codes.SERVER_ERROR, "Server cannot respond to this request. Try again later.");
        }
    }

    private void fileNotFound() throws IOException {
        String notFound = Blanks.NOT_FOUND_PAGE;
        setDataToResponse(Codes.NOT_FOUND, notFound);
    }

    private void head(String requestedRoute) throws IOException {
        String fileName = requestedRoute.toLowerCase();
        String contentType = getContentType(fileName);

        int contentLength;
        if (contentType.equals("text/plain")) {
            contentLength = requestedRoute.length();
        } else {
            contentLength = (int) new File(Blanks.CONTENT_DIRECTORY, requestedRoute).length();
        }

        composeResponse(Codes.OK, contentType, contentLength);
    }

    private void setDataToResponse(String code, String content) throws IOException {
        byte[] byteData;
        int contentLength;

        String contentType = getContentType(content);

        if (contentType.equals("text/plain")) {
            byteData = content.getBytes();
            contentLength = content.length();
        } else {
            File sendingFile = new File(Blanks.CONTENT_DIRECTORY, content);
            contentLength = (int) sendingFile.length();

            if(!sendingFile.exists()) { throw new FileNotFoundException(); }

            byteData = readFileData(sendingFile, contentLength);
        }

        composeResponse(code, contentType, contentLength);

        logger.info(new String(byteData));
        dataOut.write(byteData, 0, contentLength);
        dataOut.flush();
    }

    private void composeResponse(String code, String contentType, int contentLength) {
        List<String> headers = new ArrayList<>();
        headers.add("HTTP/1.1 " + code);
        headers.add("Server: Java http server by DiJey");
        headers.add("Date: " + new Date());
        headers.add("Content-type: " + contentType);
        headers.add("Content-length: " + contentLength);
        for (String header : headers) {
            logger.info(header);
            serverData.println(header);
        }
        serverData.println();
        serverData.flush();
    }

    public void stop() {
        try {
            clientData.close();
            serverData.close();
            dataOut.close();
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing connection");
        }

        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
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

    private StringBuffer parse() throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(clientData);
        StringBuffer bodySb = new StringBuffer();
        char[] bodyChars = new char[1024];
        int length;

        while (lineNumberReader.ready() && (length = lineNumberReader.read(bodyChars)) > 0) {
            bodySb.append(bodyChars, 0, length);
        }

        return bodySb;
    }

    private HashMap<String, String> parseHeaders(StringBuffer bodySb) {
        String headersArray = bodySb.toString().split("\r\n\r\n")[0];

        HashMap<String, String> headersHash = new HashMap<>();
        for (String header : headersArray.split("\r\n")) {
            String[] headers = header.split(": ");
            headersHash.put(headers[0], headers[1]);
        }

        return headersHash;
    }

    private HashMap<String, String> parseParams(StringBuffer bodySb) {
        String paramsArray = bodySb.toString().split("\r\n\r\n")[1];

        HashMap<String, String> paramsHash = new HashMap<>();
        for (String singleParamSet : paramsArray.split("&")) {
            String[] params = singleParamSet.split("=");
            paramsHash.put(params[0], params[1]);
        }

        return paramsHash;
    }

    private String getContentType(String file) {
        if (file.endsWith(".htm") || file.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    private boolean checkHttpVersion(String httpVersion) {
        return httpVersion.equals(Blanks.HTTP_VERSION);
    }

    private void writeMap(Map<String, String> headers) {
        for (Map.Entry<String, String> pair : headers.entrySet()) {
            logger.info(pair.getKey() + ": " + pair.getValue());
        }
    }

    public String getFileName() {
        return this.requestedRoute;
    }
}
