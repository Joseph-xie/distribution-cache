package xlp.learn.distribute.cache.nio;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.cache.Dcache;
import xlp.learn.distribute.cache.handler.ClientMessageHandler;
import xlp.learn.distribute.cache.handler.MessageHandler;
import xlp.learn.distribute.cache.handler.MessageToByte;
import xlp.learn.distribute.cache.support.NioSocketAttach;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.route.ConsistentHashingWithVN;
import xlp.learn.distribute.cache.support.DefaultFuture;

public class NioClient implements Dcache,Runnable{
    
    Logger logger = LoggerFactory.getLogger(NioClient.class);
    
    boolean running = true;
    
    Map<String,SocketChannel> channelMap = new HashMap<>();
    
    static Selector selector;
    
    String ips;
    
    ConsistentHashingWithVN consist = new ConsistentHashingWithVN();
    
    MessageToByte messageToByte = new MessageToByte();
    
    MessageHandler messageHandler = new ClientMessageHandler();
    
    public NioClient(String ips) throws IOException {
        
        this.ips = ips;
    
        selector = Selector.open();
    
        String[] ipArray = ips.split(",");
    
        consist.init(ipArray);
        
        for(String ip : ipArray){
    
            String[] ipPort = ip.split(":");
            
            String remoteIp = ipPort[0];
            
            int port = Integer.parseInt(ipPort[1]);
            
            SocketChannel channel = SocketChannel.open();
    
            channel.configureBlocking(false);
    
            channel.register(selector, SelectionKey.OP_CONNECT);
    
            channel.connect(new InetSocketAddress(remoteIp,port));
    
            channelMap.put(ip,channel);
        }
    }
    
    /**
     * 相当于一个boss线程负责select
     */
    void processSelectKey() throws IOException {
        
        while (running){
        
            int select = selector.select();
            
            if(select == 0){
                
                continue;
            }
    
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            
            for(SelectionKey key : selectionKeys){
                
                if(key.isConnectable()){
                    
                    SocketChannel ch = (SocketChannel)key.channel();
                    
                    if(ch.isConnectionPending()){
    
                        ch.finishConnect();
                    }
                    
                    NioSocketAttach attach = new  NioSocketAttach(key.channel(),key,messageHandler);
    
                    ch.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,attach);
    
                    logger.info("连接server成功:"+ch.getRemoteAddress().toString());
                }
                
                if(key.isReadable()){
    
                    NioSocketAttach attach = (NioSocketAttach)key.attachment();
    
                    //这里一次最多读取byteBuffer这么多的字节
                    attach.read();
                }
            }
        }
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
            
            //这里线程安全问题，当多个线程同时调用这个方法，导致多个线程返回同一个handler，
            // handler必须同时只能被一个线程使用
            SocketChannel channel = channelMap.get(server);
            
            if(!channel.isConnected()){
    
                logger.error("网络连接异常");
    
                throw new IllegalStateException("网络连接异常");
            }
            
            InvokeResult request = new InvokeResult();
            
            request.setTypes(types);
            
            request.setMsg(JSON.toJSONString(data));
            
            ByteBuffer byteBuffer = messageToByte.encode(request);
            
            byteBuffer.flip();
            
            int wn = channel.write(byteBuffer);
    
            DefaultFuture future = new DefaultFuture(request, 1000);
    
            InvokeResult response = (InvokeResult)future.get();
            
            return (String) response.getMsg();
            
        } catch (IOException e) {
            
            logger.warn(data.getKey() + " to " + server + " failed by\n" + e.getMessage());
        }
        
        return "";
    }
    
    @Override
    public boolean put(String key, String value) {
    
        String rstr =  read(key, value, OpType.write);
    
        return rstr.equals("200") ? true : false;
    }
    
    @Override
    public String get(String key) {
    
        return read(key, OpType.read);
    }
    
    @Override
    public void run() {
    
        try {
            
            processSelectKey();
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }
}
