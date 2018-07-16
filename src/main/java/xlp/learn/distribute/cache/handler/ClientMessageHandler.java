package xlp.learn.distribute.cache.handler;

import java.nio.channels.SocketChannel;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.support.DefaultFuture;

public class ClientMessageHandler implements MessageHandler {
    
    @Override
    public void process(
        InvokeResult result, SocketChannel channel) {
    
        DefaultFuture.received(result);
    }
}
