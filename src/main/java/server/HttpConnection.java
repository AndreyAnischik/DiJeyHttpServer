package server;

import constants.Blanks;
import constants.Codes;
import constants.Methods;
import org.apache.log4j.Logger;
import resolvers.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static constants.Blanks.PROTECTED_ROUTES;

public class HttpConnection implements Runnable {
    private Socket socket;
    private BufferedReader clientData;
    private PrintWriter serverData;
    private BufferedOutputStream dataOut;
    private String requestedRoute;
    private ResponseHandler responseHandler;
    private Map<String, Command> commandMap;

    private Logger logger = Logger.getLogger(HttpConnection.class);

    public HttpConnection(Socket socket) {
        this.socket = socket;
        initStreams();
        composeResponseHandler();
        initCommands();
    }

    @Override
    public void run() {
        while (true) {
            handleResponse();
        }
    }

    private void initStreams() {
        try {
            clientData = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverData = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Creating stream.");
        }
    }

    private void composeResponseHandler() {
        responseHandler = new ResponseHandler();
        responseHandler.setClientData(clientData);
        responseHandler.setDataOut(dataOut);
        responseHandler.setServerData(serverData);
    }

    private void initCommands() {

        commandMap = new LinkedHashMap<>();
        commandMap.put(Methods.GET, new GetCommand(responseHandler));
        commandMap.put(Methods.POST, new PostCommand(responseHandler));
        commandMap.put(Methods.HEAD, new HeadCommand(responseHandler));
        commandMap.put(Methods.DEFAULT, new DefaultCommand(responseHandler));
    }

    public void handleResponse() {
        try {
            if (!socket.isClosed()) {
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

                    StringBuffer requestBody = responseHandler.parse();
                    HashMap<String, String> headers = responseHandler.parseHeaders(requestBody);
                    responseHandler.writeMap(headers);

                    if (checkForbiddenFile(requestedRoute)) {
                        sendForbidden();
                        return;
                    }

                    if (headers.get("Authorization") == null &&
                            !Arrays.stream(PROTECTED_ROUTES).anyMatch(requestedRoute::equals)
                    ) {
                        responseHandler.setDataToResponse(Codes.UNAUTHORIZED, "You are now allowed. Log in, please.");
                        return;
                    }

                    if (commandMap.containsKey(method)) {
                        commandMap.get(method).execute(requestedRoute, requestBody);
                    } else {
                        commandMap.get(Methods.DEFAULT).execute(requestedRoute, requestBody);
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

    private void sendForbidden() throws IOException {
        String forbidden = Blanks.FORBIDDEN_FILE;
        responseHandler.setDataToResponse(Codes.FORBIDDEN, forbidden);
    }

    private boolean checkForbiddenFile(String requestedRoute) {
        return requestedRoute.equals(Blanks.FORBIDDEN_FILE);
    }

    private void sendNotSupportedHttpVersion() throws IOException {
        String notSupportedHttpVersion = Blanks.HTTP_VERSION_NOT_SUPPORTED;
        responseHandler.setDataToResponse(Codes.HTTP_VERSION_NOT_SUPPORTED, notSupportedHttpVersion);
    }

    private void fileNotFound() throws IOException {
        String notFound = Blanks.NOT_FOUND_PAGE;
        responseHandler.setDataToResponse(Codes.NOT_FOUND, notFound);
    }

    private boolean checkHttpVersion(String httpVersion) {
        return httpVersion.equals(Blanks.HTTP_VERSION);
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
}
