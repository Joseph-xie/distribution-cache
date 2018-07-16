package xlp.learn.distribute.cache.handler;

import java.io.IOException;

/**
 * Created by lpxie on 2016/8/27.
 */
public interface Lifecycle {
    
    public void init() throws IOException;
    
    public void destroy();
}
