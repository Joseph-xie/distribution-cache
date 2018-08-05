package xlp.learn.distribute.cache.handler.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.ByteBuffer;
import java.util.List;
import xlp.learn.distribute.cache.handler.ByteToMessage;
import xlp.learn.distribute.cache.handler.Codec.Result;

public class NettyByteToMessage extends ByteToMessageDecoder {
    
    ByteToMessage byteToMessage = new ByteToMessage();
    
    @Override
    protected void decode(
        ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        
        while (in.isReadable()){
    
            in.markReaderIndex();
    
            Object obj = byteToMessage.decode(in);
    
            if(obj == Result.NEED_MORE_INPUT){
        
                in.resetReaderIndex();
        
                break;
            }
            
            out.add(obj);
        }
        
    }
}
