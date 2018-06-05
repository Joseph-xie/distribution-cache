package xlp.learn.distribute.cache.manager;

/**
 * Created by lpxie on 2016/8/23.
 */
public class DetailLruManager extends LruManager {
    
    private static DetailLruManager instance = new DetailLruManager();
    
    private DetailLruManager() {
    
    }
    
    public static Manager getManager() {
        
        return instance;
    }
}
