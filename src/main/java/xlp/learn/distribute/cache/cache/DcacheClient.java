package xlp.learn.distribute.cache.cache;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.handler.Lifecycle;
import xlp.learn.distribute.cache.handler.ClientSocketHandler;
import xlp.learn.distribute.cache.monitor.MonitorReportWorker;
import xlp.learn.distribute.cache.handler.Handler;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.route.ConsistentHashingWithVN;
import xlp.learn.distribute.cache.monitor.thread.IdentityThread;

/**
 * Created by lpxie on 2016/8/27.
 */
public class DcacheClient implements Dcache, Lifecycle {
    
    private final static Logger logger = LoggerFactory.getLogger(DcacheClient.class);
    
    public String ipPorts = "";
    
    private static Map<String, Handler> activeConnections = new HashMap<>();
    
    private IdentityThread monitorReportThread;
    
    public DcacheClient(String ipPorts){
        
        this.ipPorts = ipPorts;
    }
    
    public String read(String key, byte[] types) {
        
        Map.Entry simpleEntry = new HashMap.SimpleEntry<String, String>(key, key);
        
        return write(simpleEntry, types);
    }
    
    public String read(String key,String value, byte[] types) {
        
        Map.Entry simpleEntry = new HashMap.SimpleEntry<String, String>(key, value);
        
        return write(simpleEntry, types);
    }
    
    public String write(Map.Entry<String, String> data, byte[] types) {
        
        String server = ConsistentHashingWithVN.route(data.getKey());
        
        try {
            
            //这里线程安全问题，当多个线程同时调用这个方法，导致多个线程返回同一个handler，
            // handler必须同时只能被一个线程使用
            Handler handler = activeConnections.get(server);
    
            if(!handler.available()){
                
                logger.error("网络连接异常");
                
                throw new IllegalStateException("网络连接异常");
            }
            
            byte[] readTypes = new byte[OpType.typeLength];
    
            byte[] bytes = handler.writeAndRead(JSON.toJSONString(data), types, readTypes);

            String result = new String(bytes, "utf-8");

            return result;
        
        } catch (IOException e) {
            
            logger.warn(data.getKey() + " to " + server + " failed by\n" + e.getMessage());
        }
        
        return "";
    }
    
    
    public void init() {
    
        String[] ipPortArray = ipPorts.split(",");
        
        ConsistentHashingWithVN.init(ipPortArray);//init router
        
        for (String ip : ipPortArray) {
        
            Handler handler = new ClientSocketHandler(ip);
        
            activeConnections.put(ip, handler);
            
            //
            new Thread(handler).start();//new thread for new client point
        }
        
        //start monitor report
        monitorReportThread = new MonitorReportWorker();
        
//        monitorReportThread.start();
    }
    
    public void destroy() {
        
        logger.info("monitorReport stop ...");
        
        monitorReportThread.setRunning(false);
        
        logger.info("monitorReport stop successfully");
        
        Iterator<Map.Entry<String, Handler>> iterator = activeConnections.entrySet().iterator();
        
        while (iterator.hasNext()) {
        
            Map.Entry<String, Handler> entry = iterator.next();
        
            logger.info("client " + entry.getKey() + " stop ...");
        
            entry.getValue().destroy();
        
            logger.info("client " + entry.getKey() + " stop successfully");
        }
    }
    
    @Override
    public boolean put(String key, String value) {
    
        String rstr =  read(key,value, OpType.write);
        
        return rstr.equals("200") ? true : false;
    }
    
    @Override
    public String get(String key) {
    
        return read(key, OpType.read);
    }
}
