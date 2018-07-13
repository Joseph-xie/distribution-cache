package xlp.learn.distribute.cache.protocol;

/**
 * Created by lpxie on 2016/8/26.
 */
public class OpType {
    
    public static final int typeLength = 2;
    
    public static final byte[] ping = {0, 0};
    
    public static final byte[] read = {1, 1};
    
    public static final byte[] write = {2, 1};
    
}
