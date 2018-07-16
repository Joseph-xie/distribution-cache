package xlp.learn.distribute.cache.handler;

import java.nio.channels.SocketChannel;
import xlp.learn.distribute.cache.result.InvokeResult;

public interface MessageHandler {
    
    void process(InvokeResult result,SocketChannel channel);
}
