package xlp.learn.distribute.cache.sample;

import java.util.Random;
import xlp.learn.distribute.cache.cache.DcacheClient;

public class ClientMain {
    
    public static void main(String[] args){
    
        DcacheClient client = new DcacheClient("127.0.0.1:2345");
    
        client.init();
        
        String key = "test";
    
    
        Random random = new Random();
    
        
        //从远程读数据
        while (true){
            
            try {
                
                Thread.sleep(1000);
                
                String readValue = client.get(key);
    
                System.out.println("读取的值:" + readValue);
    
                int seed = random.nextInt();
                
                client.put(key, "a"+seed);
            
            }catch (Exception exp){
                
                exp.printStackTrace();
            }
        }
        
    
//        client.destroy();
    }
}
