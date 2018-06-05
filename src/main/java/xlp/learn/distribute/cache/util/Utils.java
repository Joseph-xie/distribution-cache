package xlp.learn.distribute.cache.util;

/**
 * Created by lpxie on 2016/8/26.
 */
public class Utils {
    
    public static boolean bytesEquals(byte[] src, byte[] dst) {
        
        if (src == null || dst == null) {
            return false;
        }
        if (src.length != dst.length) {
            return false;
        }
        if (src.length == 0 || dst.length == 0) {
            return false;
        }
        for (int i = 0; i < src.length; i++) {
            if (src[i] == dst[i]) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
