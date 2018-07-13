package xlp.learn.distribute.cache.cache;

public interface Dcache {
    
    boolean put(String key,String value);
    
    String get(String key);
}
