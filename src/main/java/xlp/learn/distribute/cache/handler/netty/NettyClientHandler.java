package xlp.learn.distribute.cache.handler.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.result.InvokeResult;
import xlp.learn.distribute.cache.support.DefaultFuture;

public class NettyClientHandler extends ChannelDuplexHandler {
    
    Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    
        if(msg instanceof InvokeResult){
    
            InvokeResult result = (InvokeResult)msg;
    
            DefaultFuture.received(result);
        }
    }
    
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    
        logger.info("client异常,"+cause.getMessage());
    }
}
