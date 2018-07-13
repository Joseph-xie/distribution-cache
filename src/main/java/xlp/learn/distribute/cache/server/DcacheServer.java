package xlp.learn.distribute.cache.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.handler.Lifecycle;
import xlp.learn.distribute.cache.handler.Handler;
import xlp.learn.distribute.cache.handler.ServerSocketHandler;

/**
 * Created by lpxie on 2016/8/23.
 */
public class DcacheServer implements Lifecycle {
    
    private static Logger logger = LoggerFactory.getLogger(DcacheServer.class);
    
    private static Map<String, Handler> activeConnections = new HashMap<>();
    
    ServerSocket serverSocket;
    
    private int port = 23456;//server port
    
    private boolean running = false;
    
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
                            Handler handler = new ServerSocketHandler(client);
                            //这里每次新一个线程，客服端一直端口然后连接的话，线程数量会非常多
                            //使用线程池
                            new Thread(handler).start();
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
        
        Iterator<Map.Entry<String, Handler>> iterator = activeConnections.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Handler> entry = iterator.next();
            logger.info("server " + entry.getKey() + " stop ...");
            entry.getValue().destroy();
            logger.info("server " + entry.getKey() + " stop successfully");
        }
    }
}
