package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import logger.Logger;
import server.HttpServer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UserUI implements Initializable {
    private HttpServer httpServer;

    @FXML
    private TextField serverPort;

    @FXML
    private TextArea logArea;

    private Logger logger;
    @FXML
    private void start() {
        if (isValidPort()) {
            if (httpServer == null) {
                try {
                    httpServer = new HttpServer(Integer.valueOf(serverPort.getText()));
                    new Thread(httpServer).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                writeLog("Server is already running.");
            }
        } else {
            writeLog("Port is not valid.");
        }
    }

    @FXML
    private void stop() throws IOException {
        if (httpServer.isRunning()) {
            httpServer.stop();
            httpServer = null;
        } else {
            writeLog("Server has already stopped.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.logger = Logger.getInstance(logArea);
    }


    private boolean isValidPort() {
        Pattern pattern = Pattern.compile("(([0-9]{1,4})|([1-5][0-9]{4})|(6[0-4][0-9]{3})|(65[0-4][0-9]{2})|(655[0-2][0-9])|(6553[0-5]))");
        return pattern.matcher(serverPort.getText()).matches();
    }

    private void writeLog(String message){
        logger.writeToLog(message);
    }
}
