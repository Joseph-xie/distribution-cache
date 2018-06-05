package xlp.learn.distribute.cache.protocol;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by lpxie on 2016/8/26.
 */
public class AbstractType {
    
    public static byte[] uuid;
    
    public static String uuidStr;
    
    public static int uuidbyteslength;
    
    static {
        try {
            uuidStr = UUID.randomUUID().toString();
            uuid = uuidStr.getBytes("utf-8");//just once at start
            uuidbyteslength = uuid.length;
        } catch (UnsupportedEncodingException e) {
            //nothing to do...
        }
    }
}
