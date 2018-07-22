package xlp.learn.distribute.cache.support;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import xlp.learn.distribute.cache.handler.ByteToMessage;
import xlp.learn.distribute.cache.handler.Codec.Result;
import xlp.learn.distribute.cache.handler.OioMessageHandler;
import xlp.learn.distribute.cache.result.InvokeResult;

public class OioSocketAttach implements Runnable{
    
    ByteBuffer cumulate = null;
    
    OioMessageHandler messageHandler;
    
    ByteToMessage byteToMessage = new ByteToMessage();
    
    Socket socket;
    
    public OioSocketAttach(Socket ch, OioMessageHandler messageHandler){
        
        this.socket = ch;
        
        
        this.messageHandler = messageHandler;
    }
    
    public void read() throws IOException {
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        
        int readnums = socket.getInputStream().read(byteBuffer.array());
    
//        byteBuffer.position(readnums+1);
        
        byteBuffer.limit(readnums);
        
        if(readnums == -1){
    
            socket.close();
        }
        
        if(readnums == 0){
            
            //直接返回
            return;
        }
        
        List<Object> outs = new ArrayList<>();
        
        boolean first = cumulate == null;
        
        if(first){
            
            cumulate = byteBuffer;
        }else{
            
            cumulate = cumulate(cumulate, byteBuffer);
        }
        
        Result result = decode(cumulate, outs);
        
        if(result == Result.NORMAL){
            
            //交给业务端处理
            for(Object obj : outs){
                
                InvokeResult invokeResult = (InvokeResult)obj;
                
                messageHandler.process(invokeResult, socket);
            }
            
            //没有字节数据就清空
            if(!cumulate.hasRemaining()){
                
                cumulate = null;
            }
            
        }else{
            
            //没有完整的数据包，返回，等待下一次有消息到了再处理
            return;
        }
    }
    
    
    Result decode(ByteBuffer byteBuffer,List<Object> outs){
        
        
        while (byteBuffer.hasRemaining()){
            
            try {
                
                byteBuffer.mark();
                
                Object obj = byteToMessage.decode(byteBuffer);
                
                if(obj == Result.NEED_MORE_INPUT){
                    
                    byteBuffer.reset();
                    
                    return Result.NEED_MORE_INPUT;
                }
                
                
                outs.add(obj);
                
            }catch (Exception exp){
                
                exp.printStackTrace();
                
                byteBuffer.reset();
                
                return Result.NEED_MORE_INPUT;
                
            }
        }
        
        return Result.NORMAL;
    }
    
    ByteBuffer cumulate(ByteBuffer cumulate,ByteBuffer dst){
        
        ByteBuffer newCumulate = ByteBuffer.allocate(cumulate.capacity()+dst.capacity());
        
        newCumulate.put(cumulate);
        
        newCumulate.put(dst);
        
        return newCumulate;
    }
    
    boolean running = true;
    
    @Override
    public void run() {
    
        while (running){
    
            try {
                read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
