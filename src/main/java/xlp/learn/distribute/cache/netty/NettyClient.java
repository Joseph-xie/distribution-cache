package xlp.learn.distribute.cache.netty;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.cache.Dcache;
import xlp.learn.distribute.cache.handler.netty.NettyByteToMessage;
import xlp.learn.distribute.cache.handler.netty.NettyClientHandler;
import xlp.learn.distribute.cache.handler.netty.NettyMessageToByte;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.route.ConsistentHashingWithVN;
import xlp.learn.distribute.cache.support.DefaultFuture;

public class NettyClient implements Dcache {
    
    Bootstrap bootstrap;
    
    Channel channel;
    
    DefaultThreadFactory factory = new DefaultThreadFactory("NettyServerWork", true);
    
    NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(10,factory);
    
    int timeOut = 3000;
    
    Logger logger = LoggerFactory.getLogger(NettyClient.class);
    
    
    Map<String,Channel> channelMap = new HashMap<>();
    
    String ips;
    
    ConsistentHashingWithVN consist = new ConsistentHashingWithVN();
    
    void doOpen(){
    
        bootstrap = new Bootstrap();
    
        bootstrap.group(nioEventLoopGroup)
            .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
            .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .channel(NioSocketChannel.class);
    
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,timeOut);
    
        bootstrap.handler(new ChannelInitializer() {
        
            NettyByteToMessage byteToMessage = new NettyByteToMessage();
            
            NettyMessageToByte messageToByte = new NettyMessageToByte();
            
            NettyClientHandler clientHandler = new NettyClientHandler();
            
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                    .addLast("decoder",byteToMessage)
                    .addLast("encoder", messageToByte)
                    .addLast("handler",clientHandler);
            }
        });
    }
    
   
    
    public NettyClient(String ips) throws IOException {
        
        this.ips = ips;
        
        String[] ipArray = ips.split(",");
        
        consist.init(ipArray);
    
        doOpen();
        
        for(String ip : ipArray){
            
            String[] ipPort = ip.split(":");
            
            String remoteIp = ipPort[0];
            
            int port = Integer.parseInt(ipPort[1]);
    
            //netty连接
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(remoteIp, port));
    
            boolean ret = channelFuture.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
    
            if(ret && channelFuture.isSuccess()){
        
                channel = channelFuture.channel();
            }
            
            channelMap.put(ip,channel);
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
    
        //这里线程安全问题，当多个线程同时调用这个方法，导致多个线程返回同一个handler，
        // handler必须同时只能被一个线程使用
        Channel channel = channelMap.get(server);
    
        if(!channel.isActive()){
            
            logger.error("网络连接异常");
            
            throw new IllegalStateException("网络连接异常");
        }
    
        InvokeResult request = new InvokeResult();
    
        request.setTypes(types);
    
        request.setMsg(JSON.toJSONString(data));
    
        channel.writeAndFlush(request);
    
        DefaultFuture future = new DefaultFuture(request, 1000);
    
        InvokeResult response = (InvokeResult)future.get();
    
        return (String) response.getMsg();
    
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
}
