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

public class PostCommandTest {
    private HttpServer server;
    private Socket socket;

    @Before
    public void setServerSocket() {
        server = Mockito.mock(HttpServer.class);
        socket = Mockito.mock(Socket.class);
    }

    @Test
    public void postTesting() throws IOException {
        final String REQUEST_CONTENT = "POST /ruby_helper.rb/post-change HTTP/1.1\r\n" +
                "Accept-Encoding: gzip, deflate, br\r\n" +
                "Authorization: secrets-secrets-secrets\r\n" +
                "Accept-Language: en-US,en;q=0.9\r\n\r\n" +
                "team=real";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Mockito.doReturn(new ByteArrayInputStream(REQUEST_CONTENT.getBytes())).when(socket).getInputStream();
        Mockito.doReturn(baos).when(socket).getOutputStream();

        HttpConnection connection = new HttpConnection(server, socket);
        connection.handleResponse();

        String sendingContent = baos.toString();
        String finalContent = "You have changed real team.";

        assertTrue(sendingContent.contains("HTTP/1.1 " + Codes.OK));
        assertTrue(sendingContent.contains("Content-type: text/plain"));
        assertTrue(sendingContent.contains("Content-length: " + finalContent.length()));
        assertTrue(sendingContent.contains(finalContent));
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