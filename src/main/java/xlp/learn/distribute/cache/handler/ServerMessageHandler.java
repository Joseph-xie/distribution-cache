package xlp.learn.distribute.cache.handler;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.store.ManagerInstance;
import xlp.learn.distribute.cache.util.Utils;

public class ServerMessageHandler implements MessageHandler {
    
    MessageToByte messageToByte = new MessageToByte();
    
    @Override
    public void process(InvokeResult result,SocketChannel channel) {
    
        byte[] types = result.getTypes();
        
        if (Utils.bytesEquals(types, OpType.ping)) {
    
            InvokeResult returnResult = new InvokeResult(result.getId());
    
            returnResult.setMsg("pong");
    
            returnResult.setTypes(OpType.ping);
    
            writeResult(returnResult,channel);
            
            return;
        }
    
        String byteStr = (String)result.getMsg();
    
        Map entry = JSON.parseObject(byteStr, Map.class);
    
        String srcKey = entry.get("key").toString();
    
        String srcValue = entry.get("value").toString();
    
        if (Utils.bytesEquals(types, OpType.read)) {
        
            String value = ManagerInstance.getManager().get(srcKey);
        
            //没有查询到对应的值,返回空
            if (value == null) {
            
                value = "";
            }
    
            InvokeResult returnResult = new InvokeResult(result.getId());
    
            returnResult.setMsg(value);
    
            returnResult.setTypes(OpType.read);
    
            writeResult(returnResult,channel);
            
        } else if (Utils.bytesEquals(types, OpType.write)){
        
            ManagerInstance.getManager().put(srcKey, srcValue);
    
            InvokeResult returnResult = new InvokeResult(result.getId());
    
            returnResult.setMsg("200");
    
            returnResult.setTypes(OpType.write);
    
            writeResult(returnResult,channel);
            
        }
    }
    
    void writeResult(InvokeResult result,SocketChannel ch){
    
        ByteBuffer buffer = messageToByte.encode(result);
    
        try {
          
            buffer.flip();
            
            ch.write(buffer);
        
        } catch (IOException e) {
        
            e.printStackTrace();
        }
    }
}
