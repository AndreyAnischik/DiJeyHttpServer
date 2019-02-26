package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import appenders.TextAreaAppender;
import org.apache.log4j.Logger;
import server.HttpServer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Communication implements Initializable {
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
                    logger.error("Error while starting server", e);
                }
            } else {
                logger.info("Server is already running.");
            }
        } else {
            logger.info("Port is not valid");
        }
    }

    @FXML
    private void stop() throws IOException {
        if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        } else {
            logger.info("Server has already stopped.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextAreaAppender areaAppender = new TextAreaAppender(logArea);
        Logger.getRootLogger().addAppender(areaAppender);
        logger = Logger.getLogger(Communication.class);
    }


    private boolean isValidPort() {
        Pattern pattern = Pattern.compile("(([0-9]{1,4})|([1-5][0-9]{4})|(6[0-4][0-9]{3})|(65[0-4][0-9]{2})|(655[0-2][0-9])|(6553[0-5]))");
        return pattern.matcher(serverPort.getText()).matches();
    }
}
