package xlp.learn.distribute.cache.niosample;

import java.io.IOException;
import xlp.learn.distribute.cache.nio.NioServer;
import xlp.learn.distribute.cache.oio.DcacheServer;
import xlp.learn.distribute.cache.store.ManagerInstance;

public class NioServerMain {
    
    public static void main(String[] args){
    
        NioServer server = new NioServer(2345);
    
        String key = "test";
    
        String value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        
        ManagerInstance.getManager().put(key, value);
    
        try {
            server.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
