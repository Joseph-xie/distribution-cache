package xlp.learn.distribute.cache.net;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.Lifecycle;
import xlp.learn.distribute.cache.monitor.MonitorReportWorker;
import xlp.learn.distribute.cache.point.Handler;
import xlp.learn.distribute.cache.protocol.SelfType;
import xlp.learn.distribute.cache.route.ConsistentHashingWithVN;
import xlp.learn.distribute.cache.thread.IdentityThread;

/**
 * Created by lpxie on 2016/8/27.
 */
public class Client implements Lifecycle {
    
    private final static Logger logger = LoggerFactory.getLogger(Client.class);
    
    private static final String ips = "";
    
    //    private static final String ips = GlobalConfig.getString("server.ips");
    
    private static Map<String, Handler> activeConnections = new HashMap<>();
    
    private static ClientProcessor processor;
    
    private IdentityThread monitorReportThread;
    
    public String read(String key, byte[] types) {
        
        Map.Entry simpleEntry = new HashMap.SimpleEntry<String, String>(key, key);
        return write(simpleEntry, types);
    }
    
    public String write(Map.Entry<String, String> data, byte[] types) {
        
        String server = ConsistentHashingWithVN.route(data.getKey());
        try {
            /*if(server.equals(NetCommon.selfIp)){
                String result = processor.processSelf(data.getKey(),data.getValue(), types);
                return result;
            }else*/
            {
                Handler handler = activeConnections.get(server);
                byte[] readTypes = new byte[SelfType.typeLength];
                byte[] bytes = handler.writeAndRead(JSON.toJSONString(data), types, readTypes);
                String result = processor.process(bytes, readTypes);
                return result;
            }
        } catch (IOException e) {
            logger.warn(data.getKey() + " to " + server + " failed by\n" + e.getMessage());
        }
        return "";
    }
    
    public void init() {
        
        ConsistentHashingWithVN.init(ips.split(","));//init router
        processor = new ClientProcessor();
        for (String ip : ips.split(",")) {
            Handler handler = new ClientSocketPoint(ip);
            activeConnections.put(ip, handler);
            new Thread(handler).start();//new thread for new client point
        }
        //start monitor report
        monitorReportThread = new MonitorReportWorker();
        monitorReportThread.start();
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
}
