package xlp.learn.distribute.cache.point;

import java.io.IOException;
import xlp.learn.distribute.cache.Lifecycle;

/**
 * Created by lpxie on 2016/8/26.
 */
public interface Handler extends Runnable, Lifecycle {
    
    public byte[] writeAndRead(String message, byte[] writeTypes, byte[] readTypes)
        throws IOException;
    
    public void setRunning(boolean running);
}
