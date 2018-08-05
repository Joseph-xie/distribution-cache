package xlp.learn.distribute.cache.handler.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.handler.MessageToByte;
import xlp.learn.distribute.cache.result.InvokeResult;

public class NettyMessageToByte extends MessageToByteEncoder {
    
    MessageToByte messageToByte = new MessageToByte();
    
    Logger logger = LoggerFactory.getLogger(NettyMessageToByte.class);
    
    @Override
    protected void encode(
        ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
    
        if(msg instanceof InvokeResult){
    
            InvokeResult result = (InvokeResult)msg;
    
            ByteBuffer byteBuffer = messageToByte.encode(result);
    
            byteBuffer.flip();
            
            out.writeBytes(byteBuffer);
            
        }else{
    
            logger.warn("msg不是InvokeResult类型");
        }
    }
}
