package xlp.learn.distribute.cache.handler;

import java.net.Socket;
import xlp.learn.distribute.cache.result.InvokeResult;

public interface OioMessageHandler {
    
    void process(InvokeResult invokeResult,Socket socket);
}
