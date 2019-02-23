package resolvers;

import constants.Codes;
import server.ResponseHandler;

import java.io.IOException;

public class GetCommand extends Command {

    public GetCommand(ResponseHandler responseHandler) {
        super(responseHandler);
    }

    @Override
    public void execute(String requestedRoute, StringBuffer stringBuffer) throws IOException {
        responseHandler.setDataToResponse(Codes.OK, requestedRoute.toLowerCase());
    }
}
