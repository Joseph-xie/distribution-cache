package xlp.learn.distribute.cache.nettysample;

import java.io.IOException;
import java.util.Random;
import xlp.learn.distribute.cache.netty.NettyClient;
import xlp.learn.distribute.cache.nio.NioClient;

public class NettyClientMain {
    
    public static void main(String[] args){
    
    
        for(int i = 0;i<1;i++){
            Thread thread = new Thread(new Runnable() {
    
                @Override
                public void run() {
                    test();
    
                }
            });
            thread.setName("test-"+i);
            thread.start();
        }
        
        
    }
    
    static void test(){
    
        NettyClient client = null;
        try {
            client = new NettyClient("127.0.0.1:2345");
        } catch (IOException e) {
            e.printStackTrace();
        }
    
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
