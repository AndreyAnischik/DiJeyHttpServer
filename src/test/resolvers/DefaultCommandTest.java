package resolvers;

import constants.Blanks;
import constants.Codes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import server.HttpConnection;
import server.HttpServer;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.*;

public class DefaultCommandTest {
    private HttpServer server;
    private Socket socket;

    @Before
    public void setServerSocket() {
        server = Mockito.mock(HttpServer.class);
        socket = Mockito.mock(Socket.class);
    }

    @Test
    public void deleteTesting() throws IOException {
        final String REQUEST_CONTENT = "DELETE /some_path HTTP/1.1\r\n" +
                "Authorization: secrets-secrets-secrets\r\n";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Mockito.doReturn(new ByteArrayInputStream(REQUEST_CONTENT.getBytes())).when(socket).getInputStream();
        Mockito.doReturn(baos).when(socket).getOutputStream();

        HttpConnection connection = new HttpConnection(server, socket);
        connection.handleResponse();

        String initialContent = getInitialContent(Blanks.NOT_IMPLEMENTED_PAGE);
        String sendingContent = baos.toString();

        assertTrue(sendingContent.contains("HTTP/1.1 " + Codes.NOT_IMPLEMENTED));
        assertTrue(sendingContent.contains("Content-type: text/html"));
        assertTrue(sendingContent.contains("Content-length: " + initialContent.length()));
        assertTrue(sendingContent.contains(initialContent));
    }

    private String getInitialContent(String fileName){
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(Blanks.CONTENT_DIRECTORY + fileName));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
                contentBuilder.append('\n');
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}