package logger;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;


public final class TextAreaAppender extends AppenderSkeleton {

    private static final String DEFAULT_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n";

    private volatile TextArea logArea;

    public TextAreaAppender(TextArea logArea){
        super();
        this.logArea = logArea;
    }

    public void setLogArea(TextArea logArea){
        this.logArea = logArea;
    }

    @Override
    public Layout getLayout() {
        if (layout == null) {
            layout = new PatternLayout(DEFAULT_PATTERN);
        }

        return super.getLayout();
    }


    @Override
    public void append(LoggingEvent logEvent) {
        String message = getLayout().format(logEvent);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                logArea.appendText(message);
            }
        });
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
