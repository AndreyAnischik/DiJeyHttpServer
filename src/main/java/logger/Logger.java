package logger;

import javafx.scene.control.TextArea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
    private static volatile Logger instance;
    private TextArea logArea;

    private Logger(TextArea logArea) {
        this.logArea = logArea;
    }

    public static Logger getInstance(TextArea logArea) {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger(logArea);
                }
            }
        }
        return instance;
    }

    public synchronized void writeToLog(String message){
        logArea.appendText(String.format(
            "%s%3s%3s\n",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "- ",
            message
        ));
    }
}
