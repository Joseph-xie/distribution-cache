package xlp.learn.distribute.cache.handler;

import java.net.InetSocketAddress;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.util.Utils;

/**
 * Created by lpxie on 2016/8/23.
 */
public class ClientSocketHandler extends AbstractSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientSocketHandler.class);
    
    boolean available = false;
    
    public ClientSocketHandler(String serverIp) {
        
        super();
        
        this.selfIp = serverIp;
    }
    
    public void init() {
        
        connect();
    }
    
    
    @Override
    public void process() {
        
        try {
    
            //first
            Thread.sleep(3000);
            
            if (!ping()) {
    
                available = false;
                
                logger.info(selfIp + " reconnecting...");
        
                connect();
                
            } else {
        
                logger.info(selfIp + " server is ok");
            }
        
        } catch (Exception e) {
            
            logger.warn(selfIp + "  重连失败,"+e.getMessage());
        }
    }
    
    private boolean connect() {
        
        try {
    
            socket = new Socket();
            
            socket.setKeepAlive(true);
            
            //            socket.setSoTimeout(timeout*1000);
            socket.setTcpNoDelay(true);
    
            //5seconds
            String[] ipAndPort = selfIp.split(":");
            
            socket.connect(new InetSocketAddress(ipAndPort[0],Integer.parseInt(ipAndPort[1])), 1 * 1000);
            
            if (!socket.isConnected()) {
               
                return false;
            }
            
            //check if connect success
            if (ping()) {
            
                logger.info("connect to " + selfIp + " success");
    
                available = true;
                
                return true;
            }
            
        } catch (Exception exp) {
           
            logger.warn("connect to " + selfIp + " failed by :\n" + exp.getMessage());
           
            return false;
        }
        
        return false;
    }
    
    
    private boolean ping() {
        
        try {
           
            byte[] types = new byte[OpType.typeLength];
          
            byte[] bytes = writeAndRead("ping", OpType.ping, types);
           
            if (bytes.length < 0) {
             
                return false;
            }
            
            if (Utils.bytesEquals(types, OpType.ping)) {
            
                String res = new String(bytes, "utf-8");
            
                if (res.equals("pong")) {
            
                    return true;
                }
            }
        } catch (Exception exp) {
            //nothing to do...
            return false;
        }
        return false;
    }
    
    @Override
    public boolean available() {
        
        return available;
    }
}
