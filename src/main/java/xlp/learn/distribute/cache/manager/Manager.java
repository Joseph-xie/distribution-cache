package xlp.learn.distribute.cache.manager;

import java.util.Map;

/**
 * Created by lpxie on 2016/8/23.
 */
public interface Manager {
    
    public String get(String key);
    
    public String put(String key, String value);
    
    public void putMap(Map.Entry map);
    
    public Map getMap();
}
