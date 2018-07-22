package xlp.learn.distribute.cache.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by lpxie on 2016/8/23.
 */
public interface Protocol {
    
    /**
     * 只返回消息内容的字节
     * @param inputStream
     * @param bytes
     * @return
     * @throws IOException
     */
    byte[] read(InputStream inputStream, byte[] bytes) throws IOException;
    
    ByteBuffer read(InputStream inputStream) throws  IOException;
    
    boolean write(OutputStream outputStream, String message, byte[] types);
    
    boolean write(OutputStream outputStream, byte[] bytes, byte[] types);
    
}
