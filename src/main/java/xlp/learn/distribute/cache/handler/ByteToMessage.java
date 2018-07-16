package xlp.learn.distribute.cache.handler;

import java.nio.ByteBuffer;
import xlp.learn.distribute.cache.result.InvokeResult;

public class ByteToMessage extends Codec {
    
    public Object decode(ByteBuffer byteBuffer){
    
        byte[] typeBytes = new byte[2];
    
        byte[] idBytes = new byte[8];
        
        byte[] startOrEnd = new byte[3];
    
        //length is 4 byte
        byte[] lenByte = new byte[4];
    
        InvokeResult result = new InvokeResult();
        
        //check start
         byteBuffer.get(startOrEnd);
        
        boolean check = checkStart(startOrEnd);
        
        if(!check){
        
            return Result.NEED_MORE_INPUT;
        }
        
        //check type
        byteBuffer.get(typeBytes);
    
        result.setTypes(typeBytes);
        
        byteBuffer.get(idBytes);
        
        long msgId = bytes2long(idBytes);
    
        result.setId(msgId);
        
        //read data
        byteBuffer.get(lenByte);
    
        int realLength = byteArrayToInt(lenByte);
    
        String msg = "";
        
        if (realLength > 0) {
        
            byte[] messByte = new byte[realLength];
    
            byteBuffer.get(messByte, 0, realLength);//first is type
    
            msg = new String(messByte);
    
            result.setMsg(msg);
        }
    
        //check end ,reuse sync
        byteBuffer.get(startOrEnd);
        
        check = checkEnd(startOrEnd);
        
        if(check){
        
            return result;
        }
        
        return Result.NEED_MORE_INPUT;
    }
    
    public int byteArrayToInt(byte[] bytes) {
        
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0x000000ff) << shift;
        }
        return value;
    }
}
