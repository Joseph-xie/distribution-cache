package xlp.learn.distribute.cache.handler;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.store.ManagerInstance;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.util.Utils;

/**
 * Created by lpxie on 2016/8/23.
 */
public class ServerSocketHandler extends AbstractSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerSocketHandler.class);
    
    public ServerSocketHandler() {
        
        super();
    }
    
    public ServerSocketHandler(Socket socket) {
        
        this();
        
        this.socket = socket;
    }
    
    @Override
    public void process() {
        
        try {
            
            if(socket.isClosed()){
                
                setRunning(false);
                
                return;
            }
            
            byte[] types = new byte[OpType.typeLength];
            
            byte[] bytes = protocol.read(socket.getInputStream(), types);
            
            //流结束
            if(bytes.length == 1 && bytes[0] == -1){
                
                setRunning(false);
    
                return;
            }
            
            //通常读取的数据错误，直接丢弃
            if (bytes.length < 1) {
            
                return;
            }
            
            if (Utils.bytesEquals(types, OpType.ping)) {
               
                protocol.write(socket.getOutputStream(), "pong", OpType.ping);
               
                return;
            }
            
            String byteStr = new String(bytes,  "utf-8");
           
            Map entry = JSON.parseObject(byteStr, Map.class);
            
            String srcKey = entry.get("key").toString();
            
            String srcValue = entry.get("value").toString();
            
            if (Utils.bytesEquals(types, OpType.read)) {
            
                String value = ManagerInstance.getManager().get(srcKey);
            
                //没有查询到对应的值,返回空
                if (value == null) {
            
                    value = "";
                }
            
                protocol.write(socket.getOutputStream(), value, OpType.read);
            
            } else if (Utils.bytesEquals(types, OpType.write)){
    
                ManagerInstance.getManager().put(srcKey, srcValue);
    
                protocol.write(socket.getOutputStream(), "200", OpType.write);
            }
            
        } catch (IOException exp) {
            
            logger.warn("网络错误,关闭连接,错误日志:" + exp.getMessage());
    
            try {
                
                socket.close();
                
                setRunning(false);
                
            } catch (IOException e) {
                
                //nothing to do
            }
        }
    }
    
    @Override
    public boolean available() {
        
        return true;
    }
}
