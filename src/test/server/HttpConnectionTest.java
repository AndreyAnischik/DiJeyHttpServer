package server;

import org.junit.Before;
import org.junit.Ignore;
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
        connection.handleResponse();

        String fileName = connection.getFileName();

        String initialContent = getInitialContent(fileName);
        String sendingContent = baos.toString();

        assertTrue(sendingContent.contains("HTTP/1.1 " + Constants.OK));
        assertTrue(sendingContent.contains("Content-type: text/html"));
        assertTrue(sendingContent.contains("Content-length: " + initialContent.length()));
        assertTrue(sendingContent.contains(initialContent));
    }

    @Test
    public void deleteTesting() throws IOException {
        final String REQUEST_CONTENT = "DELETE /some_path HTTP/1.1";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Mockito.doReturn(new ByteArrayInputStream(REQUEST_CONTENT.getBytes())).when(socket).getInputStream();
        Mockito.doReturn(baos).when(socket).getOutputStream();

        HttpConnection connection = new HttpConnection(httpServer, socket);
        connection.handleResponse();

        String initialContent = getInitialContent(Constants.NOT_IMPLEMENTED_PAGE);
        String sendingContent = baos.toString();

        assertTrue(sendingContent.contains("HTTP/1.1 " + Constants.NOT_IMPLEMENTED));
        assertTrue(sendingContent.contains("Content-type: text/html"));
        assertTrue(sendingContent.contains("Content-length: " + initialContent.length()));
        assertTrue(sendingContent.contains(initialContent));
    }

    @Test
    public void postTesting() throws IOException {
        final String REQUEST_CONTENT = "POST /post-change HTTP/1.1\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: en-US,en;q=0.9\r\n\r\n" +
                "team=real";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Mockito.doReturn(new ByteArrayInputStream(REQUEST_CONTENT.getBytes())).when(socket).getInputStream();
        Mockito.doReturn(baos).when(socket).getOutputStream();

        HttpConnection connection = new HttpConnection(httpServer, socket);
        connection.handleResponse();

        String sendingContent = baos.toString();
        String finalContent = "You have changed real team.";

        assertTrue(sendingContent.contains("HTTP/1.1 " + Constants.OK));
        assertTrue(sendingContent.contains("Content-type: text/plain"));
        assertTrue(sendingContent.contains("Content-length: " + finalContent.length()));
        assertTrue(sendingContent.contains(finalContent));
    }

    @Test
    public void getNotFound() throws IOException {
        final String REQUEST_CONTENT = "GET /not_existed_page.html HTTP/1.1";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Mockito.doReturn(new ByteArrayInputStream(REQUEST_CONTENT.getBytes())).when(socket).getInputStream();
        Mockito.doReturn(baos).when(socket).getOutputStream();

        HttpConnection connection = new HttpConnection(httpServer, socket);
        connection.handleResponse();

        String initialContent = getInitialContent(Constants.NOT_FOUND_PAGE);
        String sendingContent = baos.toString();

        assertTrue(sendingContent.contains("HTTP/1.1 " + Constants.NOT_FOUND));
        assertTrue(sendingContent.contains("Content-type: text/html"));
        assertTrue(sendingContent.contains("Content-length: " + initialContent.length()));
        assertTrue(sendingContent.contains(initialContent));
    }

    @Test
    public void reproduceServerUnavailable() throws IOException {
        final String REQUEST_CONTENT = "POST /fake-post HTTP/1.1\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: en-US,en;q=0.9\r\n\r\n" +
                "param=real";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Mockito.doReturn(new ByteArrayInputStream(REQUEST_CONTENT.getBytes())).when(socket).getInputStream();
        Mockito.doReturn(baos).when(socket).getOutputStream();

        HttpConnection connection = new HttpConnection(httpServer, socket);
        connection.handleResponse();

        String initialContent = "Service unavailable.";
        String sendingContent = baos.toString();

        assertTrue(sendingContent.contains("HTTP/1.1 " + Constants.SERVICE_UNAVAILABLE));
        assertTrue(sendingContent.contains("Content-type: text/plain"));
        assertTrue(sendingContent.contains("Content-length: " + initialContent.length()));
        assertTrue(sendingContent.contains(initialContent));
    }

    private String getInitialContent(String fileName){
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

        return contentBuilder.toString();
    }
}
