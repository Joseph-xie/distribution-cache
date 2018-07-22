package xlp.learn.distribute.cache.handler;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.protocol.MyProtocol;
import xlp.learn.distribute.cache.protocol.Protocol;
import xlp.learn.distribute.cache.result.InvokeResult;

/**
 * Created by lpxie on 2016/8/23.
 */
public abstract class AbstractSocketHandler implements Handler {
    
    public Socket socket;
    
    public Protocol protocol = new MyProtocol();
    
    public String selfIp ;
    
    
    private Lock lock = new ReentrantLock();
    
    private Logger logger = LoggerFactory.getLogger(AbstractSocketHandler.class);
    
    MessageToByte messageToByte = new MessageToByte();
    
    private volatile boolean running = true;
    
    public AbstractSocketHandler() {
    
    }
    
    public void setRunning(boolean running) {
        
        this.running = running;
    }
    
    public void init() {
    
    }
    
    
    public void run() {
        
        while (running) {
        
            process();
        }
    }
    
    public abstract void process();
    
    /**
     * 一个同步操作,写入一个请求,返回一个结果
     * @param message
     * @param writeType
     * @param readType
     * @return
     * @throws IOException
     */
    public byte[] writeAndRead(
        final String message, final byte[] writeType, final byte[] readType) throws IOException {
        
        lock.lock();
        
        try {
    
            InvokeResult invokeResult = new InvokeResult();
            invokeResult.setMsg(message);
            invokeResult.setTypes(writeType);
    
            ByteBuffer byteBuffer = messageToByte.encode(invokeResult);
    
            byteBuffer.flip();
            
            protocol.write(socket.getOutputStream(), message, writeType);
        
            byte[] bytes = protocol.read(socket.getInputStream(), readType);
            
            return bytes;
        
        }finally {
        
            //关闭socket,短链接的实现方式
            //或者加个keepAliveTimeOut
//            socket.close();
            
            lock.unlock();
        }
    }
    
    public void destroy() {
        
        logger.info(this.selfIp + " stop ...");
        
        this.setRunning(false);
        
        logger.info(this.selfIp + " stop successfully");
    }
}
