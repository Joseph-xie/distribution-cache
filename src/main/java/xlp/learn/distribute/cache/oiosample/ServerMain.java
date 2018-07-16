package xlp.learn.distribute.cache.oiosample;

import xlp.learn.distribute.cache.store.ManagerInstance;
import xlp.learn.distribute.cache.oio.DcacheServer;

public class ServerMain {
    
    public static void main(String[] args){
    
        DcacheServer server = new DcacheServer(2345);
    
        String key = "test";
    
        String value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        
        ManagerInstance.getManager().put(key, value);
        
        server.init();
    }
}
