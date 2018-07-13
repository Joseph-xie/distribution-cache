package xlp.learn.distribute.cache.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lpxie on 2016/8/23.
 */
public interface Protocol {
    
    byte[] read(InputStream inputStream, byte[] bytes) throws IOException;
    
    boolean write(OutputStream outputStream, String message, byte[] types);
    
    boolean write(OutputStream outputStream, byte[] bytes, byte[] types);
    
}
