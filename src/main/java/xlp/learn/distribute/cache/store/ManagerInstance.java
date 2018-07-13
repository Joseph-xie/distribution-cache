package xlp.learn.distribute.cache.store;

/**
 * Created by lpxie on 2016/8/23.
 */
public class ManagerInstance extends LruManager {
    
    private static ManagerInstance instance = new ManagerInstance();
    
    private ManagerInstance() {
    
    }
    
    public static Manager getManager() {
        
        return instance;
    }
}
