package server;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.*;

public class HttpConnectionTest {
    private HttpServer httpServer;
    private Socket socket;

    @Before
    public void setServerSocket() {
        httpServer = Mockito.mock(HttpServer.class);
        socket = Mockito.mock(Socket.class);
    }

    @Test
    public void getTesting() throws IOException {
        final String REQUEST_CONTENT = "GET /index.html HTTP/1.1";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Mockito.doReturn(new ByteArrayInputStream(REQUEST_CONTENT.getBytes())).when(socket).getInputStream();
        Mockito.doReturn(baos).when(socket).getOutputStream();

        HttpConnection connection = new HttpConnection(httpServer, socket);
        connection.run();

        String fileName = connection.getFileName();

        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(Constants.CONTENT_DIRECTORY + fileName));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
                contentBuilder.append('\n');
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String initialContent = contentBuilder.toString();
        String sendingContent = baos.toString();

        assertTrue(sendingContent.contains("HTTP/1.1 " + Constants.OK));
        assertTrue(sendingContent.contains("Content-type: text/html"));
        assertTrue(sendingContent.contains("Content-length: " + initialContent.length()));
        assertTrue(sendingContent.contains(initialContent));
    }
}
