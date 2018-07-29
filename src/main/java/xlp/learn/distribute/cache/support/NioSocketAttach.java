package xlp.learn.distribute.cache.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import xlp.learn.distribute.cache.handler.ByteToMessage;
import xlp.learn.distribute.cache.handler.Codec.Result;
import xlp.learn.distribute.cache.handler.NioMessageHandler;
import xlp.learn.distribute.cache.result.InvokeResult;

public class NioSocketAttach extends BaseAttach{
    
    ByteBuffer cumulate = null;
    
    SelectableChannel selectableChannel;
    
    SelectionKey selectionKey;
    
    
    NioMessageHandler messageHandler;
    
  
    
    
    public NioSocketAttach(SelectableChannel ch,SelectionKey key, NioMessageHandler messageHandler){
        
        this.selectableChannel = ch;
        
        this.selectionKey = key;
        
        this.messageHandler = messageHandler;
    }
    
    //这个必须同一时刻这能一个线程处理
    public synchronized void read() throws IOException {
        
        SocketChannel channel = (SocketChannel)selectableChannel;
    
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        
        int readnums = channel.read(byteBuffer);
        
        //流结束
        if(readnums == -1){
            
            selectionKey.cancel();
            
            selectableChannel.close();
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
    
        Result result = decodeNio(cumulate, outs);
        
        //这里可能解析了一个或多个obj,但是后面的还有部分数据不完整
        //交给业务端处理
        //如果有解析成功数据就直接处理
        for(Object obj : outs){
        
            InvokeResult invokeResult = (InvokeResult)obj;
        
            messageHandler.process(invokeResult,channel);
        }
        
        if(result == Result.NORMAL){
            
            //没有字节数据就清空,避免下次处理重复数据
            if(!cumulate.hasRemaining()){
                
                cumulate = null;
            }
            
        }else{
            
            //没有完整的数据包，返回，等待下一次有消息到了再处理
            return;
        }
    }
}
