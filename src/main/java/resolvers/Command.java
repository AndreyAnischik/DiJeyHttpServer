package resolvers;

import java.io.IOException;

public interface Command {
    void execute(String requestedRoute, StringBuffer stringBuffer) throws IOException;
}
