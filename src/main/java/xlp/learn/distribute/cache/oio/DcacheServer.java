package xlp.learn.distribute.cache.oio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.handler.Lifecycle;
import xlp.learn.distribute.cache.handler.OioMessageHandler;
import xlp.learn.distribute.cache.handler.OioServerMessageHandler;
import xlp.learn.distribute.cache.support.OioSocketAttach;

/**
 * Created by lpxie on 2016/8/23.
 */
public class DcacheServer implements Lifecycle {
    
    private static Logger logger = LoggerFactory.getLogger(DcacheServer.class);
    
    private static Map<String, OioSocketAttach> activeConnections = new HashMap<>();
    
    ServerSocket serverSocket;
    
    private int port;
    
    private boolean running = false;
    
    int cpunum = Runtime.getRuntime().availableProcessors();
    
    BlockingQueue queue = new ArrayBlockingQueue(1);
    
    RejectedExecutionHandler rejectedHandler = new DiscardPolicy();
    
    Executor executor = new ThreadPoolExecutor(cpunum,
                                               cpunum*2000,
                                               60,
                                               TimeUnit.SECONDS,
                                               queue,
                                               rejectedHandler);
    
    OioMessageHandler messageHandler = new OioServerMessageHandler();
    
    public DcacheServer(int port){
        
        this.port = port;
    }
    
    public void init() {
        
        try {
           
            serverSocket = new ServerSocket(port);
           
            running = true;
           
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    
                    logger.info("server star success");
                   
                    while (running) {
                        
                        try {
                            
                            Socket client = serverSocket.accept();
                            
                            client.setKeepAlive(true);
                            
                            client.setTcpNoDelay(true);
                            
                            OioSocketAttach handler = new OioSocketAttach(client,messageHandler);
                            
                            executor.execute(handler);
                            
                            String clientIp = client.getRemoteSocketAddress().toString()
                                .split(":")[0].substring(1);
                            
                            activeConnections.put(clientIp, handler);
                        
                        } catch (IOException e) {
                            logger.warn("server accept connection failed by " + ExceptionUtils
                                .getStackTrace(e));
                        }
                    }
                }
            }).start();
            
        } catch (IOException e) {
            logger.warn("server start failed by " + ExceptionUtils.getStackTrace(e));
        }
    }
    
    public void destroy() {
        
        try {
            logger.info("server stop ...");
            running = false;
            serverSocket.close();
            logger.info("server stop successfully");
        } catch (IOException e) {
            //nothing to do ...
        }
        
        Iterator<Map.Entry<String, OioSocketAttach>> iterator = activeConnections.entrySet().iterator();
        
        while (iterator.hasNext()) {
        
            Map.Entry<String, OioSocketAttach> entry = iterator.next();
            logger.info("server " + entry.getKey() + " stop ...");
            entry.getValue().setRunning(false);
            logger.info("server " + entry.getKey() + " stop successfully");
        }
    }
}
