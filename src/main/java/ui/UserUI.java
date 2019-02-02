package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import server.FTPServer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UserUI implements Initializable {
    private FTPServer ftpServer;

    @FXML
    private TextField serverPort;


    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @FXML
    private void run() {
        if (isValidPort()) {
            System.out.println("Is valid");
            if (ftpServer == null) {
                try {
                    ftpServer = new FTPServer();
                    new Thread(ftpServer).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {

            }
        } else {
            //сервер уже запущен
        }
    }

    @FXML
    private void stop() {
        if(FTPServer.isRunning()) {
            ftpServer.stop();
            ftpServer = null;
        } else {
            //сервер уже остановлен
        }
    }

    private boolean isValidPort(){
        Pattern pattern = Pattern.compile("(([0-9]{1,4})|([1-5][0-9]{4})|(6[0-4][0-9]{3})|(65[0-4][0-9]{2})|(655[0-2][0-9])|(6553[0-5]))");
        return pattern.matcher(serverPort.getText()).matches();
    }
}
