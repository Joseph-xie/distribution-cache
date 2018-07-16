package xlp.learn.distribute.cache.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.handler.Lifecycle;
import xlp.learn.distribute.cache.handler.MessageHandler;
import xlp.learn.distribute.cache.handler.ServerMessageHandler;
import xlp.learn.distribute.cache.support.NioSocketAttach;

public class NioServer implements Lifecycle {
    
    Logger logger = LoggerFactory.getLogger(NioServer.class);
    
    int port;
    
    boolean running = true;
    
    MessageHandler messageHandler = new ServerMessageHandler();
    
    public NioServer(int port){
        
        this.port = port;
    }
    
    /**
     * boss线程处理selectionKey
     * @throws IOException
     */
    @Override
    public void init() throws IOException {
    
        Selector selector = Selector.open();
        
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    
        serverSocketChannel.configureBlocking(false);
    
        serverSocketChannel.bind(new InetSocketAddress(port));
        
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    
        logger.info("server running......");
        
        while (running){
    
            int st = selector.select();
    
            if(st == 0){
                
                continue;
            }
            
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            
            for (SelectionKey key : selectionKeys){
                
                if((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT){
    
                    ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
    
                    SocketChannel socketChannel = serverChannel.accept();
    
                    //fixme 为啥这里会为null
                    if(socketChannel == null){
                        
                        continue;
                    }
                    
                    socketChannel.configureBlocking(false);
    
                    NioSocketAttach attach = new  NioSocketAttach(socketChannel,key,messageHandler);
    
                    socketChannel.register(selector,SelectionKey.OP_READ | SelectionKey.OP_WRITE,attach);
    
                    logger.info("server建立新的连接:"+socketChannel.socket().getRemoteSocketAddress().toString());
                    
                }else if(key.isReadable()){
    
                    NioSocketAttach attach = (NioSocketAttach)key.attachment();

                    //这里一次最多读取byteBuffer这么多的字节
                    attach.read();
                
                }else if(key.isWritable()){
                
                    //写事件不需要处理,在handler里面直接通过channel写出去了
                }
            }
        }
    }
    
    @Override
    public void destroy() {
    
    }
}
