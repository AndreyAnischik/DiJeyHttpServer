package resolvers;

import constants.Blanks;
import constants.Codes;
import server.ResponseHandler;

import java.io.IOException;

public class Command {
    protected ResponseHandler responseHandler;

    public Command(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public void execute(String requestedRoute, StringBuffer stringBuffer) throws IOException {
        String notImplemented = Blanks.NOT_IMPLEMENTED_PAGE;
        responseHandler.setDataToResponse(Codes.NOT_IMPLEMENTED, notImplemented);
    }
}
