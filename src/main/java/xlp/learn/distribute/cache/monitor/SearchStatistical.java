package xlp.learn.distribute.cache.monitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lpxie on 2016/8/24.
 */
public class SearchStatistical {
    
    private static final AtomicLong detailTotal = new AtomicLong(1L);
    
    private static final AtomicLong detailCache = new AtomicLong(0L);
    
    private static final AtomicLong relationTotal = new AtomicLong(1L);
    
    private static final AtomicLong relationCache = new AtomicLong(0L);
    
    public static AtomicLong getDetailTotal() {
        
        return detailTotal;
    }
    
    public static AtomicLong getDetailCache() {
        
        return detailCache;
    }
    
    public static AtomicLong getRelationTotal() {
        
        return relationTotal;
    }
    
    public static AtomicLong getRelationCache() {
        
        return relationCache;
    }
    
    public static String getRateStr() {
        
        StringBuilder stringBuilder = new StringBuilder();
        float detailRate = (float) (detailCache.get()) / detailTotal.get();
        float relationRate = (float) (relationCache.get()) / relationTotal.get();
        stringBuilder.append("search statistical :\n").
            append("detailTotal :" + detailTotal.get() + "\n").
            append("detailRate :" + detailRate + "\n").
            append("relationTotal :" + relationTotal.get() + "\n").
            append("relationRate:" + relationRate + "\n");
        return stringBuilder.toString();
    }
}
