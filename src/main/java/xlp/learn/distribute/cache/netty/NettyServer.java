package xlp.learn.distribute.cache.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.handler.netty.NettyByteToMessage;
import xlp.learn.distribute.cache.handler.netty.NettyMessageToByte;
import xlp.learn.distribute.cache.handler.netty.NettyServerHandler;

public class NettyServer {
    
    Logger logger = LoggerFactory.getLogger(NettyServer.class);
    
    int port;
    
    ServerBootstrap serverBootstrap;
    
    EventLoopGroup bossGroup;
    
    EventLoopGroup workGroup;
    
    Channel channel;
    
    public NettyServer(int port){
        
        this.port = port;
    }
    
    public void doOpen(){
        
        serverBootstrap = new ServerBootstrap();
    
        DefaultThreadFactory factory = new DefaultThreadFactory("NettyServerBoss", false);
        
        bossGroup = new NioEventLoopGroup(1,factory);
    
        factory = new DefaultThreadFactory("NettyServerWork", true);
        
        workGroup = new NioEventLoopGroup(10,factory);
        
        serverBootstrap.group(bossGroup,workGroup)
            .channel(NioServerSocketChannel.class)
            .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
            .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                NettyByteToMessage byteToMessage = new NettyByteToMessage();
    
                NettyMessageToByte messageToByte = new NettyMessageToByte();
    
                NettyServerHandler serverHandler = new NettyServerHandler();
                
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    
                    ch.pipeline()
                        .addLast("decoder",byteToMessage)
                        .addLast("encoder",messageToByte)
                        .addLast("handler",serverHandler);
                }
            });
        
        //bind
        ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(port));
    
        channelFuture.syncUninterruptibly();
        
        channel = channelFuture.channel();
    
        logger.info("nettyServer启动成功");
    }
}
