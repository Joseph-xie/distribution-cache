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
import xlp.learn.distribute.cache.handler.MessageHandler;
import xlp.learn.distribute.cache.result.InvokeResult;

public class NioSocketAttach {
    
    ByteBuffer cumulate = null;
    
    SelectableChannel selectableChannel;
    
    SelectionKey selectionKey;
    
    
    MessageHandler messageHandler;
    
    ByteToMessage byteToMessage = new ByteToMessage();
    
    
    public NioSocketAttach(SelectableChannel ch,SelectionKey key, MessageHandler messageHandler){
        
        this.selectableChannel = ch;
        
        this.selectionKey = key;
        
        this.messageHandler = messageHandler;
    }
    
    public void read() throws IOException {
        
        SocketChannel channel = (SocketChannel)selectableChannel;
    
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        
        int readnums = channel.read(byteBuffer);
        
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
    
        Result result = decode(cumulate, outs);
        
        if(result == Result.NORMAL){
            
            //交给业务端处理
            for(Object obj : outs){
    
                InvokeResult invokeResult = (InvokeResult)obj;
                
                messageHandler.process(invokeResult,channel);
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
                
                byteBuffer.flip();
    
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
}
