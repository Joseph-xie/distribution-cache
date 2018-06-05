package xlp.learn.distribute.cache.net;

import java.net.InetSocketAddress;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.NetCommon;
import xlp.learn.distribute.cache.point.AbstractSocketPoint;
import xlp.learn.distribute.cache.protocol.AbstractType;
import xlp.learn.distribute.cache.protocol.SelfType;
import xlp.learn.distribute.cache.util.Utils;

/**
 * Created by lpxie on 2016/8/23.
 */
public class ClientSocketPoint extends AbstractSocketPoint {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientSocketPoint.class);
    
    private static final int timeout = 1;
    
    public ClientSocketPoint(String serverIp) {
        
        super();
        this.serverIp = serverIp;
        socket = new Socket();
    }
    
    public void init() {
        
        connect();
    }
    
    ;
    
    @Override
    public void process() {
        
        try {
            Thread.sleep(10 * 1000);//first
            if (!ping()) {
                logger.info(serverIp + " connect...");
                connect();
            } else {
                logger.info(serverIp + " server is ok");
            }
        } catch (Exception e) {
            logger.warn(serverIp + "  reconnect failed");
        }
    }
    
    private boolean connect() {
        
        try {
            socket
                .connect(new InetSocketAddress(serverIp, NetCommon.serverPort), 1 * 1000);//5seconds
            socket.setKeepAlive(true);
            //            socket.setSoTimeout(timeout*1000);
            socket.setTcpNoDelay(true);
            if (!socket.isConnected()) {
                return false;
            }
            //check if connect success
            if (ping()) {
                logger.info("connect to " + serverIp + " success");
                return true;
            }
        } catch (Exception exp) {
            logger.warn("connect to " + serverIp + " failed by :\n" + exp.getMessage());
            return false;
        }
        return false;
    }
    
    
    private boolean ping() {
        
        try {
            byte[] types = new byte[SelfType.typeLength];
            byte[] bytes = writeAndRead("ping", SelfType.ping, types);
            if (bytes.length < 0) {
                return false;
            }
            
            int uuidLength = AbstractType.uuidbyteslength;
            if (Utils.bytesEquals(types, SelfType.ping)) {
                String res = new String(bytes, uuidLength, bytes.length - uuidLength, "utf-8");
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
    
}
