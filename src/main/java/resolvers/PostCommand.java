package resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Blanks;
import constants.Codes;
import server.ResponseHandler;
import server.TimeoutBlock;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PostCommand extends Command {
    public PostCommand(ResponseHandler responseHandler) {
        super(responseHandler);
    }

    @Override
    public void execute(String requestedRoute, StringBuffer stringBuffer) throws IOException {
        HashMap<String, String> paramsHash = responseHandler.parseParams(stringBuffer);
        responseHandler.writeMap(paramsHash);

        String[] requestDestination = requestedRoute.substring(1).split("/");

        try {
            Path currentRelativePath = Paths.get(Blanks.SCRIPTS_DIRECTORY + requestDestination[0]);
            String methodName = requestDestination[1].replace('-', '_');
            String jsonParams = new ObjectMapper().writeValueAsString(paramsHash);

            ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
            jruby.eval(Files.newBufferedReader(currentRelativePath, StandardCharsets.UTF_8));

            Invocable invokableJrubyIns = (Invocable) jruby;
            AtomicReference<String> scriptResult = new AtomicReference<>("");

            try {
                TimeoutBlock timeoutBlock = new TimeoutBlock(5000);
                Runnable block = () -> {
                    try {
                        scriptResult.set((String) invokableJrubyIns.invokeFunction(methodName, jsonParams));
                    } catch (NoSuchMethodException | ScriptException e) {
                        e.printStackTrace();
                    }
                };
                timeoutBlock.addBlock(block);
                responseHandler.setDataToResponse(Codes.OK, scriptResult.get());
            } catch (Throwable e) {
                responseHandler.setDataToResponse(Codes.SERVICE_UNAVAILABLE, "Service unavailable.");
            }
        } catch (Exception e) {
            responseHandler.setDataToResponse(Codes.SERVER_ERROR, "Server cannot respond to this request. Try again later.");
        }
    }
}
