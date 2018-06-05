package xlp.learn.distribute.cache.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.thread.IdentityThread;

/**
 * Created by lpxie on 2016/8/24.
 */
public class MonitorReportWorker extends IdentityThread {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitorReportWorker.class);
    
    public void setRunning(boolean running) {
        
        this.running = running;
    }
    
    @Override
    public void run() {
        
        try {
            Thread.sleep(10000);//lazy init
        } catch (InterruptedException e) {
            //nothing
        }
        while (running) {
            try {
                logger.info(SearchStatistical.getRateStr());
                Thread.sleep(15000);
            } catch (Exception exp) {
                //nothing
            }
        }
    }
}
