package xlp.learn.distribute.cache.support;

import java.nio.ByteBuffer;
import java.util.List;
import xlp.learn.distribute.cache.handler.ByteToMessage;
import xlp.learn.distribute.cache.handler.Codec.Result;

public abstract class BaseAttach {
    
    ByteToMessage byteToMessage = new ByteToMessage();
    
    Result decodeNio(ByteBuffer byteBuffer,List<Object> outs){
        
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
    
    Result decodeOio(ByteBuffer byteBuffer,List<Object> outs){
        
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
}
