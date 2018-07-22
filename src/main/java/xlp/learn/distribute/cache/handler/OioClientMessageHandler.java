package xlp.learn.distribute.cache.handler;

import java.net.Socket;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.support.DefaultFuture;

public class OioClientMessageHandler implements OioMessageHandler {
    
    @Override
    public void process(
        InvokeResult invokeResult, Socket socket) {
    
        DefaultFuture.received(invokeResult);
    }
}
