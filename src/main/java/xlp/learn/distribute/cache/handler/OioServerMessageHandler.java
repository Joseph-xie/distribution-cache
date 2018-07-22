package xlp.learn.distribute.cache.handler;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.store.ManagerInstance;
import xlp.learn.distribute.cache.util.Utils;

public class OioServerMessageHandler implements OioMessageHandler {
    
    MessageToByte messageToByte = new MessageToByte();
    
    @Override
    public void process(InvokeResult result,Socket socket) {
        
        byte[] types = result.getTypes();
        
        if (Utils.bytesEquals(types, OpType.ping)) {
            
            InvokeResult returnResult = new InvokeResult(result.getId());
            
            returnResult.setMsg("pong");
            
            returnResult.setTypes(OpType.ping);
            
            writeResult(returnResult,socket);
            
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
            
            writeResult(returnResult,socket);
            
        } else if (Utils.bytesEquals(types, OpType.write)){
            
            ManagerInstance.getManager().put(srcKey, srcValue);
            
            InvokeResult returnResult = new InvokeResult(result.getId());
            
            returnResult.setMsg("200");
            
            returnResult.setTypes(OpType.write);
            
            writeResult(returnResult,socket);
            
        }
    }
    
    void writeResult(InvokeResult result,Socket socket){
        
        ByteBuffer buffer = messageToByte.encode(result);
        
        try {
            
            buffer.flip();
    
            socket.getOutputStream().write(buffer.array());
            
            socket.getOutputStream().flush();
            
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }
}
