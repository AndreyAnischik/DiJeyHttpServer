package resolvers;

import constants.Blanks;
import constants.Codes;
import server.ResponseHandler;

import java.io.File;
import java.io.IOException;

public class HeadCommand implements Command {
    private ResponseHandler responseHandler;

    public HeadCommand(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void execute(String requestedRoute, StringBuffer stringBuffer) throws IOException {
        String fileName = requestedRoute.toLowerCase();
        String contentType = responseHandler.getContentType(fileName);

        int contentLength;
        if (contentType.equals("text/plain")) {
            contentLength = requestedRoute.length();
        } else {
            contentLength = (int) new File(Blanks.CONTENT_DIRECTORY, requestedRoute).length();
        }

        responseHandler.composeResponse(Codes.OK, contentType, contentLength);
    }
}
