package xlp.learn.distribute.cache.protocol;

/**
 * Created by lpxie on 2016/8/26.
 */
public class SelfType extends AbstractType {
    
    public static final int typeLength = 2;
    
    public static final byte[] ping = {0, 0};
    
    public static final byte[] readDetail = {1, 1};
    
    public static final byte[] readRelation = {1, 2};
    
    public static final byte[] writeDetail = {2, 1};
    
    public static final byte[] writeRelation = {2, 2};
    
    public static final byte[] disableDetail = {3, 1};
    
    public static final byte[] disableRelation = {3, 2};
}
