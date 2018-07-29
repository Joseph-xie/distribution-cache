package xlp.learn.distribute.cache.oio;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.cache.Dcache;
import xlp.learn.distribute.cache.handler.Lifecycle;
import xlp.learn.distribute.cache.handler.MessageToByte;
import xlp.learn.distribute.cache.handler.OioClientMessageHandler;
import xlp.learn.distribute.cache.monitor.MonitorReportWorker;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.route.ConsistentHashingWithVN;
import xlp.learn.distribute.cache.monitor.thread.IdentityThread;
import xlp.learn.distribute.cache.support.DefaultFuture;
import xlp.learn.distribute.cache.support.OioSocketAttach;

/**
 * Created by lpxie on 2016/8/27.
 */
public class DcacheClient implements Dcache, Lifecycle {
    
    private final static Logger logger = LoggerFactory.getLogger(DcacheClient.class);
    
    public String ipPorts = "";
    
    Map<String,Socket> channelMap = new HashMap<>();
    
    private IdentityThread monitorReportThread;
    
    ConsistentHashingWithVN consist = new ConsistentHashingWithVN();
    
    RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor.DiscardPolicy();
    
    BlockingQueue queue = new SynchronousQueue();
    
    int cpunum= Runtime.getRuntime().availableProcessors();
    
    int corenum = 1000;
    
    MessageToByte messageToByte = new MessageToByte();
    
    Executor executor = new ThreadPoolExecutor(corenum, corenum, 60, TimeUnit.SECONDS,queue,rejectHandler);
    
    OioClientMessageHandler messageHandler = new OioClientMessageHandler();
    
    
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
        
        String server = consist.route(data.getKey());
        
        try {
    
            Socket socket = channelMap.get(server);
            
    
            if(!socket.isConnected()){
        
                logger.error("网络连接异常");
        
                throw new IllegalStateException("网络连接异常");
            }
    
            InvokeResult request = new InvokeResult();
    
            request.setTypes(types);
    
            request.setMsg(JSON.toJSONString(data));
    
            ByteBuffer byteBuffer = messageToByte.encode(request);
    
            byteBuffer.flip();
    
            socket.getOutputStream().write(byteBuffer.array());
    
            socket.getOutputStream().flush();
            
            DefaultFuture future = new DefaultFuture(request, 1000);
    
            InvokeResult response = (InvokeResult)future.get();
    
            return (String) response.getMsg();
            
        } catch (IOException e) {
            
            logger.warn(data.getKey() + " to " + server + " failed by\n" + e.getMessage());
        }
        
        return "";
    }
    
    public void init() throws IOException {
    
        String[] ipPortArray = ipPorts.split(",");
    
        int tn = ipPortArray.length;
    
        BlockingQueue queue = new SynchronousQueue();
    
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        
        Executor executor = new ThreadPoolExecutor(tn,tn,60L,TimeUnit.SECONDS,queue,handler);
        
        consist.init(ipPortArray);//init router
        
        for (String ip : ipPortArray) {
    
            Socket socket = new Socket();
    
            socket.setKeepAlive(true);
    
            //是否开启读取超时设置,开启就是设置大于的timeout,这样就会中止线程一直等待
            //                        socket.setSoTimeout(timeout*1000);
            socket.setTcpNoDelay(true);
    
            //5seconds
            String[] ipAndPort = ip.split(":");
    
            socket.connect(new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1])), 10 * 1000);
    
            OioSocketAttach attach = new OioSocketAttach(socket,messageHandler);
    
            executor.execute(attach);
            
            channelMap.put(ip,socket);
        }
        
        //start monitor report
        monitorReportThread = new MonitorReportWorker();
        
//        monitorReportThread.start();
    }
    
    public void destroy() {
        
        logger.info("monitorReport stop ...");
        
        monitorReportThread.setRunning(false);
        
        logger.info("monitorReport stop successfully");
        
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
