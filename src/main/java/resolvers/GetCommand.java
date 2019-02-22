package resolvers;

import constants.Codes;
import server.ResponseHandler;

import java.io.IOException;

public class GetCommand implements Command {
    private ResponseHandler responseHandler;

    public GetCommand(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void execute(String requestedRoute, StringBuffer stringBuffer) throws IOException {
        responseHandler.setDataToResponse(Codes.OK, requestedRoute.toLowerCase());
    }
}
