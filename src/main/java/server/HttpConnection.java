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
    private HttpServer server;
    private BufferedReader clientData;
    private PrintWriter serverData;
    private BufferedOutputStream dataOut;
    private String requestedRoute;
    private ResponseHandler responseHandler;
    private Map<String, Command> commandMap;

    private Logger logger = Logger.getLogger(HttpConnection.class);
    private final String BAD_REQUEST_REGEX = "\\d+";

    public HttpConnection(HttpServer server, Socket socket) {
        this.server = server;
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

                    if (requestedRoute.substring(1).matches(BAD_REQUEST_REGEX)) {
                        responseHandler.setDataToResponse(Codes.BAD_REQUEST, "Bad Request.");
                        return;
                    }

                    if (checkAcceptability()) {
                        responseHandler.setDataToResponse(Codes.NOT_ACCEPTABLE, "Wrong format.");
                        return;
                    }

                    if (server.getMovedUrl(requestedRoute) != null) {
                        responseHandler.setDataToResponse(
                                Codes.MOVED,
                                server.getMovedUrl(requestedRoute)
                        );
                        return;
                    }

                    if (server.getFoundUrl(requestedRoute) != null) {
                        responseHandler.setDataToResponse(
                                Codes.FOUND,
                                "This page was temporarily moved to " + server.getFoundUrl(requestedRoute) + " page."
                        );
                        return;
                    }

                    if (headers.get("Authorization") == null &&
                            !Arrays.stream(PROTECTED_ROUTES).anyMatch(requestedRoute::equals)
                    ) {
                        responseHandler.setDataToResponse(Codes.UNAUTHORIZED, "You are now allowed. Log in, please.");
                        return;
                    }

                    commandMap.getOrDefault(method, new Command(responseHandler)).execute(requestedRoute, requestBody);
                }
            }
        } catch (FileNotFoundException fileException) {
            try {
                fileNotFound();
            } catch (IOException ioException) {
                logger.error("I/O error occurs while sending fileNotFound.", ioException);
            }
        } catch (IOException exception) {
            logger.error("I/O error occurs while waiting for request.", exception);
        }
    }

    public String getRoute() {
        return this.requestedRoute;
    }

    public void stop() {
        try {
            clientData.close();
            serverData.close();
            dataOut.close();
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing connection.", e);
        }

        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
    }

    private void initStreams() {
        try {
            clientData = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverData = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Error while creating stream.", e);
        }
    }

    private void composeResponseHandler() {
        responseHandler = new ResponseHandler();
        responseHandler.setClientData(clientData);
        responseHandler.setDataOut(dataOut);
        responseHandler.setServerData(serverData);
        responseHandler.setPort(server.getServerPort());
    }

    private void initCommands() {
        commandMap = new LinkedHashMap<>();
        commandMap.put(Methods.GET, new GetCommand(responseHandler));
        commandMap.put(Methods.POST, new PostCommand(responseHandler));
        commandMap.put(Methods.HEAD, new HeadCommand(responseHandler));
    }

    private void sendForbidden() throws IOException {
        responseHandler.setDataToResponse(Codes.FORBIDDEN, Blanks.FORBIDDEN_FILE);
    }

    private boolean checkForbiddenFile(String requestedRoute) {
        return requestedRoute.equals(Blanks.FORBIDDEN_FILE);
    }

    private void sendNotSupportedHttpVersion() throws IOException {
        responseHandler.setDataToResponse(Codes.HTTP_VERSION_NOT_SUPPORTED, Blanks.HTTP_VERSION_NOT_SUPPORTED);
    }

    private void fileNotFound() throws IOException {
        responseHandler.setDataToResponse(Codes.NOT_FOUND, Blanks.NOT_FOUND_PAGE);
    }

    private boolean checkHttpVersion(String httpVersion) {
        return httpVersion.equals(Blanks.HTTP_VERSION);
    }

    private boolean checkAcceptability() {
        return requestedRoute.endsWith(".pdf");
    }
}
