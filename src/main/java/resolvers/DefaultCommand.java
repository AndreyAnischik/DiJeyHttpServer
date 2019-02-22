package resolvers;

import constants.Blanks;
import constants.Codes;
import server.ResponseHandler;

import java.io.IOException;

public class DefaultCommand implements Command {
    private ResponseHandler responseHandler;

    public DefaultCommand(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void execute(String requestedRoute, StringBuffer stringBuffer) throws IOException {
        String notImplemented = Blanks.NOT_IMPLEMENTED_PAGE;
        responseHandler.setDataToResponse(Codes.NOT_IMPLEMENTED, notImplemented);
    }
}
