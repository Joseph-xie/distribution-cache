package xlp.learn.distribute.cache.thread;

/**
 * Created by lpxie on 2016/8/25.
 */
public abstract class IdentityThread extends Thread {
    
    public volatile boolean running = true;
    
    public void setRunning(boolean running) {
        
        this.running = running;
    }
}
