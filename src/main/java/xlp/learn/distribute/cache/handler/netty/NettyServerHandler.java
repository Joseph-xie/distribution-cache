package xlp.learn.distribute.cache.handler.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.protocol.OpType;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.store.ManagerInstance;
import xlp.learn.distribute.cache.util.Utils;

public class NettyServerHandler extends ChannelDuplexHandler {
    
    Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    
        if(msg instanceof InvokeResult){
    
            InvokeResult result = (InvokeResult)msg;
    
            byte[] types = result.getTypes();
    
            if (Utils.bytesEquals(types, OpType.ping)) {
        
                InvokeResult returnResult = new InvokeResult(result.getId());
        
                returnResult.setMsg("pong");
        
                returnResult.setTypes(OpType.ping);
        
                writeResult(returnResult,ctx.channel());
        
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
        
                writeResult(returnResult,ctx.channel());
        
            } else if (Utils.bytesEquals(types, OpType.write)){
        
                ManagerInstance.getManager().put(srcKey, srcValue);
        
                InvokeResult returnResult = new InvokeResult(result.getId());
        
                returnResult.setMsg("200");
        
                returnResult.setTypes(OpType.write);
        
                writeResult(returnResult,ctx.channel());
        
            }
        }
        
    }
    
    void writeResult(InvokeResult result,Channel ch){
        
        ch.writeAndFlush(result);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        
        logger.info("active:"+ctx.channel().remoteAddress().toString());
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        
        logger.info("异常:"+cause.getMessage());
    }
}
