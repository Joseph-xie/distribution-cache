package xlp.learn.distribute.cache.nettysample;

import xlp.learn.distribute.cache.netty.NettyServer;
import xlp.learn.distribute.cache.store.ManagerInstance;

public class NettyServerMain {
    
    public static void main(String[] args){
    
        NettyServer server = new NettyServer(2345);
    
        String key = "test";
    
        String value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        
        ManagerInstance.getManager().put(key, value);
    
        server.doOpen();
        
    }
}
