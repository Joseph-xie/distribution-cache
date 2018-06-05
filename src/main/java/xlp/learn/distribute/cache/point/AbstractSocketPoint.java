package xlp.learn.distribute.cache.point;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.protocol.MyProtocol;
import xlp.learn.distribute.cache.protocol.Protocol;

/**
 * Created by lpxie on 2016/8/23.
 */
public abstract class AbstractSocketPoint implements Handler {
    
    public Socket socket;//this protocol must be static
    
    public Protocol protocol = new MyProtocol();
    
    public String serverIp = "127.0.0.1";
    
    FutureTask<byte[]> future;
    
    private Lock lock = new ReentrantLock();
    
    private Logger logger = LoggerFactory.getLogger(AbstractSocketPoint.class);
    
    private volatile boolean running = true;
    
    public AbstractSocketPoint() {
    
    }
    
    public void setRunning(boolean running) {
        
        this.running = running;
    }
    
    public void init() {
    
    }
    
    ;
    
    public void run() {
        
        init();
        while (running) {
            process();
        }
    }
    
    ;
    
    public abstract void process();
    
    public byte[] writeAndRead(
        final String message, final byte[] writeTypes, final byte[] readTypes) throws IOException {
        
        lock.lock();
        try {
            future = new FutureTask<byte[]>(new Callable<byte[]>() {
                
                @Override
                public byte[] call() throws Exception {
                    
                    protocol.write(socket.getOutputStream(), message, writeTypes);
                    byte[] bytes = protocol.read(socket.getInputStream(), readTypes);
                    return bytes;
                }
            });
            new Thread(future).start();
            byte[] bytes = future.get(500, TimeUnit.MILLISECONDS);
            return bytes;
        } catch (InterruptedException e) {
            logger.warn("writeAndRead interrupt by :\n" + e.getMessage());
        } catch (ExecutionException e) {
            logger.warn("writeAndRead execution by :\n" + e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("writeAndRead timeout by :\n" + e.getMessage());
        } finally {
            lock.unlock();
        }
        return new byte[0];
    }
    
    public void destroy() {
        
        {
            logger.info(this.serverIp + " stop ...");
            this.setRunning(false);
            logger.info(this.serverIp + " stop successfully");
        }
    }
}
