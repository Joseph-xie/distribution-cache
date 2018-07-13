package xlp.learn.distribute.cache.handler;

import java.io.IOException;

/**
 * Created by lpxie on 2016/8/26.
 */
public interface Handler extends Runnable, Lifecycle {
    
    boolean available();
    
    byte[] writeAndRead(String message, byte[] writeType, byte[] readType)
        throws IOException;
    
    void setRunning(boolean running);
}
