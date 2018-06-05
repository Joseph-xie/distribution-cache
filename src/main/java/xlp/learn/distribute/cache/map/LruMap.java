package xlp.learn.distribute.cache.map;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lpxie on 2016/8/23.
 */
public class LruMap<K, V> {
    
    private static Logger logger = LoggerFactory.getLogger(LruMap.class);
    
    private int maxSize = 30000;//按照10kb/key 大致300MB
    
    private long millisUntilExpiration = 10 * 60 * 1000;
    
    private LinkedHashMap map;
    
    public LruMap(int maxSize, long millisUntilExpiration) {
        
        this.maxSize = maxSize;
        this.millisUntilExpiration = millisUntilExpiration;
        init();
    }
    
    public LruMap() {
        
        init();
    }
    
    private void init() {
        
        int capacity = (int) Math.ceil(maxSize / 0.75f) + 1;
        map = new LinkedHashMap(capacity, maxSize, true) {
            
            protected boolean removeEldestEntry(Map.Entry eldest) {
                
                return size() > maxSize;
            }
        };
    }
    
    public V put(K k, V v) {
        
        return (V) internalPut((String) k, (String) v);
    }
    
    private String internalPut(String key, String val) {
        
        Entry entry = entryFor(key);
        if (entry != null) {
            entry.setTimestamp(System.currentTimeMillis());
            entry.setVal(val);
        } else {
            map.put(key, new Entry(System.currentTimeMillis(), val));
        }
        return val;
    }
    
    private String internalPut(String key, String val, Map map) {
        
        Entry entry = entryFor(key);
        if (entry != null) {
            entry.setTimestamp(System.currentTimeMillis());
            entry.setVal(val);
        } else {
            map.put(key, new Entry(System.currentTimeMillis(), val));
        }
        return val;
    }
    
    public V get(K k) {
        
        return (V) internalGet((String) k);
    }
    
    private String internalGet(String key) {
        
        Entry entry = entryFor(key);
        if (entry != null) {
            return entry.getVal();
        }
        return null;
    }
    
    
    private synchronized Entry entryFor(String key) {
        
        Entry entry = (Entry) map.get(key);
        if (entry != null) {
            long delta = System.currentTimeMillis() - entry.getTimestamp();
            if (delta < 0 || delta >= millisUntilExpiration) {
                map.remove(key);
                logger.info(key + " is expired");
                entry = null;
            }
        }
        return entry;
    }
    
    public synchronized void remove(K k) {
        
        map.remove(k);
    }
    
    public void putMap(Map.Entry<K, V> entry) {
        
        put(entry.getKey(), entry.getValue());
    }
    
    public Map getMap() {
        
        return this.map;
    }
    
    public static class Entry {
        
        long timestamp;
        
        String val;
        
        public Entry() {
        
        }
        
        public Entry(long timeStamp, String val) {
            
            this.timestamp = timeStamp;
            this.val = val;
        }
        
        public long getTimestamp() {
            
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            
            this.timestamp = timestamp;
        }
        
        public String getVal() {
            
            return val;
        }
        
        public void setVal(String val) {
            
            this.val = val;
        }
    }
}
