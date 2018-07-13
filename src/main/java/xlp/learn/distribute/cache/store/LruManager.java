package xlp.learn.distribute.cache.store;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.store.mem.LruMap;

/**
 * Created by lpxie on 2016/8/23.
 */
public abstract class LruManager implements Manager {
    
    private static final Logger logger = LoggerFactory.getLogger(LruManager.class);
    
    LruMap map = new LruMap();//use default size :3w
    
    public LruManager() {
    
    }
    
    public String get(String key) {
        
        String value = (String) map.get(key);
        return value;
    }
    
    public String put(String key, String value) {
        
        String value1 = (String) map.put(key, value);
        return value1;
    }
    
    public void putMap(Map.Entry map) {
        
        this.map.putMap(map);
    }
    
    public Map getMap() {
        
        return map.getMap();
    }
}
