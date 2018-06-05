package xlp.learn.distribute.cache.manager;

/**
 * Created by lpxie on 2016/8/23.
 */
public class RelationLruManager extends LruManager {
    
    private static RelationLruManager instance = new RelationLruManager();
    
    private RelationLruManager() {
    
    }
    
    public static Manager getManager() {
        
        return instance;
    }
}
