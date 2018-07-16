package xlp.learn.distribute.cache.handler;

import java.nio.ByteBuffer;
import xlp.learn.distribute.cache.result.InvokeResult;

public class MessageToByte extends Codec{
    
    public ByteBuffer encode(InvokeResult result){
        
        long id = result.getId();
        
        byte[] idBytes = long2bytes(id);
        
        String msg = (String) result.getMsg();
        
        byte[] types = result.getTypes();
        
        byte[] msgBytes = msg.getBytes();
    
        int length = msgBytes.length;
    
        byte[] lengthByte = intToBytes(length);
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(start.length*2+msgBytes.length+
                                                        lengthByte.length+types.length
                                                        +idBytes.length);
    
        byteBuffer.put(start);
    
        byteBuffer.put(types);
    
        byteBuffer.put(idBytes);
        
        byteBuffer.put(lengthByte);
    
        byteBuffer.put(msgBytes);
        
        byteBuffer.put(end);
        
        return byteBuffer;
    }
}
